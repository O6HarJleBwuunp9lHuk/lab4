package com.example.apigateway.gateway;

import org.example.common.event.*;
import com.example.apigateway.kafka.RateLimitProducer;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class ApiGateway implements Filter {

    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RateLimitProducer rateLimitProducer;
    private final List<Route> routes;
    private final ConcurrentHashMap<String, ServiceInstance> serviceInstances = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> serviceCacheTime = new ConcurrentHashMap<>();
    private static final long SERVICE_CACHE_TTL_MS = 30000;
    private final ConcurrentHashMap<String, Boolean> circuitBreakerCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> circuitBreakerCacheTime = new ConcurrentHashMap<>();
    private static final long CIRCUIT_BREAKER_CACHE_TTL_MS = 5000;

    public ApiGateway(RestTemplate restTemplate,
                      KafkaTemplate<String, Object> kafkaTemplate,
                      RateLimitProducer rateLimitProducer) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.rateLimitProducer = rateLimitProducer;
        this.routes = initializeRoutes();
    }

    private List<Route> initializeRoutes() {
        return List.of(
            new Route("/api/users/.*", "user-service"),
            new Route("/api/notifications/.*", "notification-service"),
            new Route("/api/circuit-breaker/.*", "circuit-breaker-service"),
            new Route("/api/rate-limit/.*", "rate-limiter-service"),
            new Route("/api/discovery/.*", "service-discovery")
        );
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String clientId = extractClientId(httpRequest);

        if (shouldSkip(path)) {
            chain.doFilter(request, response);
            return;
        }

        Optional<Route> routeOpt = findMatchingRoute(path);
        if (routeOpt.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        Route route = routeOpt.get();
        String serviceName = route.serviceName();

        try {
            Optional<ServiceInstance> serviceInstanceOpt = getServiceInstance(serviceName);
            if (serviceInstanceOpt.isEmpty()) {
                handleFallback(httpResponse, serviceName, "Service not found");
                return;
            }

            ServiceInstance serviceInstance = serviceInstanceOpt.get();
            boolean circuitAllowed = checkCircuitBreaker(serviceName);
            if (!circuitAllowed) {
                handleFallback(httpResponse, serviceName, "Circuit Breaker blocked");
                sendCircuitBreakerEvent(serviceName, "REQUEST_BLOCKED", path, method);
                return;
            }

            boolean rateLimitAllowed = checkRateLimit(clientId, serviceName, path);
            if (!rateLimitAllowed) {
                handleRateLimitExceeded(httpResponse, clientId);
                return;
            }

            sendApiGatewayEvent(serviceName, "REQUEST_STARTED", path, method);
            String targetUrl = serviceInstance.getBaseUrl() + path;
            RestTemplate simpleRestTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = simpleRestTemplate.getForEntity(targetUrl, String.class);
            sendCircuitBreakerEvent(serviceName, "SUCCESS_RECORDED", path, method);
            sendApiGatewayEvent(serviceName, "REQUEST_SUCCESS", path, method);
            httpResponse.setStatus(responseEntity.getStatusCodeValue());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(responseEntity.getBody());

        } catch (Exception e) {

            sendCircuitBreakerEvent(serviceName, "FAILURE_RECORDED", path, method);
            sendApiGatewayEvent(serviceName, "REQUEST_FAILED", path, method);

            handleFallback(httpResponse, serviceName, "Proxy error: " + e.getMessage());
        }
    }

    private Optional<ServiceInstance> getServiceInstance(String serviceName) {
        // Проверяем кэш
        Long lastCacheTime = serviceCacheTime.get(serviceName);
        ServiceInstance cachedInstance = serviceInstances.get(serviceName);

        if (lastCacheTime != null && cachedInstance != null &&
            (System.currentTimeMillis() - lastCacheTime) < SERVICE_CACHE_TTL_MS) {
            return Optional.of(cachedInstance);
        }

        try {
            String discoveryUrl = "http://localhost:8084/api/discovery/service/" + serviceName;
            ResponseEntity<ServiceInstance> response = restTemplate.getForEntity(
                discoveryUrl, ServiceInstance.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ServiceInstance instance = response.getBody();
                serviceInstances.put(serviceName, instance);
                serviceCacheTime.put(serviceName, System.currentTimeMillis());
                return Optional.of(instance);
            }
        } catch (Exception e) {

        }

        if (cachedInstance != null) {
            return Optional.of(cachedInstance);
        }
        return getFallbackServiceInstance(serviceName);
    }

    private Optional<ServiceInstance> getFallbackServiceInstance(String serviceName) {
        ServiceInstance fallbackInstance = new ServiceInstance();
        fallbackInstance.setServiceName(serviceName);

        switch (serviceName) {
            case "user-service":
                fallbackInstance.setHost("localhost");
                fallbackInstance.setPort(8080);
                break;
            case "notification-service":
                fallbackInstance.setHost("localhost");
                fallbackInstance.setPort(8081);
                break;
            case "circuit-breaker-service":
                fallbackInstance.setHost("localhost");
                fallbackInstance.setPort(8082);
                break;
            case "rate-limiter-service":
                fallbackInstance.setHost("localhost");
                fallbackInstance.setPort(8085);
                break;
            case "service-discovery":
                fallbackInstance.setHost("localhost");
                fallbackInstance.setPort(8084);
                break;
            default:
                return Optional.empty();
        }

        fallbackInstance.setHealthCheckUrl("http://" + fallbackInstance.getHost() + ":" + fallbackInstance.getPort() + "/health");
        return Optional.of(fallbackInstance);
    }

    private boolean checkCircuitBreaker(String serviceName) {
        Long lastCheck = circuitBreakerCacheTime.get(serviceName);
        Boolean cachedResult = circuitBreakerCache.get(serviceName);

        if (lastCheck != null && cachedResult != null &&
            (System.currentTimeMillis() - lastCheck) < CIRCUIT_BREAKER_CACHE_TTL_MS) {
            return cachedResult;
        }

        try {
            CircuitBreakerEvent checkEvent = new CircuitBreakerEvent();
            checkEvent.setBreakerName(serviceName);
            checkEvent.setServiceName(serviceName);
            checkEvent.setEventType("CHECK_REQUEST");
            checkEvent.setTimestamp(java.time.LocalDateTime.now());

            CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send("circuit-breaker-check-requests", serviceName, checkEvent);

            future.get(2, TimeUnit.SECONDS);
            boolean allowed = true;
            circuitBreakerCache.put(serviceName, allowed);
            circuitBreakerCacheTime.put(serviceName, System.currentTimeMillis());

            return allowed;

        } catch (Exception e) {
            boolean allowed = true;
            circuitBreakerCache.put(serviceName, allowed);
            circuitBreakerCacheTime.put(serviceName, System.currentTimeMillis());
            return allowed;
        }
    }

    private boolean checkRateLimit(String clientId, String serviceName, String path) {
        try {
            CompletableFuture<RateLimitResultEvent> future =
                rateLimitProducer.checkRateLimit(clientId, serviceName, path);
            RateLimitResultEvent result = future.get(5, TimeUnit.SECONDS);

            if (!result.isAllowed()) {
                sendApiGatewayEvent(serviceName, "RATE_LIMIT_EXCEEDED", path, "GET");
                return false;
            }
            return true;

        } catch (Exception e) {
            return true;
        }
    }

    private String extractClientId(HttpServletRequest request) {
        String clientId = request.getHeader("X-Client-ID");
        if (clientId == null || clientId.trim().isEmpty()) {
            clientId = request.getRemoteAddr(); // Используем IP как fallback
        }
        return clientId;
    }

    private void sendCircuitBreakerEvent(String serviceName, String eventType, String path, String method) {
        try {
            CircuitBreakerEvent event = new CircuitBreakerEvent();
            event.setBreakerName(serviceName);
            event.setServiceName(serviceName);
            event.setEventType(eventType);
            event.setPath(path);
            event.setMethod(method);
            event.setTimestamp(java.time.LocalDateTime.now());

            kafkaTemplate.send("circuit-breaker-events", serviceName, event)
                .whenComplete((result, exception) -> {
                });

        } catch (Exception e) {
            System.err.println("❌ Failed to create Circuit Breaker event: " + e.getMessage());
        }
    }

    private void sendApiGatewayEvent(String serviceName, String eventType, String path, String method) {
        try {
            ApiGatewayEvent event = new ApiGatewayEvent();
            event.setServiceName(serviceName);
            event.setEventType(eventType);
            event.setPath(path);
            event.setMethod(method);
            event.setTimestamp(java.time.LocalDateTime.now());

            kafkaTemplate.send("api-gateway-events", serviceName, event)
                .whenComplete((result, exception) -> {
                });

        } catch (Exception e) {
            System.err.println("❌ Failed to create API Gateway event: " + e.getMessage());
        }
    }

    private boolean forwardRequest(HttpServletRequest request, HttpServletResponse response,
                                   ServiceInstance serviceInstance, String originalPath) {
        try {
            String targetUrl = buildTargetUrl(serviceInstance, originalPath);

            HttpHeaders headers = extractHeaders(request);
            HttpEntity<byte[]> requestEntity = prepareRequestEntity(request, headers);

            HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());

            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                targetUrl, httpMethod, requestEntity, byte[].class);

            copyResponse(responseEntity, response);

            return responseEntity.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            return false;
        }
    }

    private String buildTargetUrl(ServiceInstance serviceInstance, String originalPath) {
        String baseUrl = serviceInstance.getBaseUrl();
        Optional<Route> routeOpt = routes.stream()
            .filter(route -> route.serviceName().equals(serviceInstance.getServiceName()))
            .findFirst();

        if (routeOpt.isPresent()) {
            Route route = routeOpt.get();
            String routePattern = route.path().replace(".*", "");
            String servicePath = originalPath.replaceFirst(routePattern, "");
            return baseUrl + servicePath;
        }

        return baseUrl + originalPath;
    }

    private HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (shouldSkipHeader(headerName)) {
                continue;
            }
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.add(headerName, headerValues.nextElement());
            }
        }

        return headers;
    }

    private HttpEntity<byte[]> prepareRequestEntity(HttpServletRequest request, HttpHeaders headers)
        throws IOException {

        if (hasBody(request)) {
            byte[] body = request.getInputStream().readAllBytes();
            return new HttpEntity<>(body, headers);
        } else {
            return new HttpEntity<>(headers);
        }
    }

    private void copyResponse(ResponseEntity<byte[]> responseEntity, HttpServletResponse response)
        throws IOException {

        response.setStatus(responseEntity.getStatusCode().value());

        responseEntity.getHeaders().forEach((headerName, headerValues) -> {
            if (!shouldSkipHeader(headerName)) {
                headerValues.forEach(headerValue ->
                    response.addHeader(headerName, headerValue));
            }
        });

        if (responseEntity.getBody() != null && responseEntity.getBody().length > 0) {
            response.getOutputStream().write(responseEntity.getBody());
        }
    }

    private boolean shouldSkipHeader(String headerName) {
        return "host".equalsIgnoreCase(headerName) ||
            "content-length".equalsIgnoreCase(headerName) ||
            "transfer-encoding".equalsIgnoreCase(headerName) ||
            "connection".equalsIgnoreCase(headerName);
    }

    private boolean hasBody(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method) ||
            "PUT".equalsIgnoreCase(method) ||
            "PATCH".equalsIgnoreCase(method);
    }

    private void handleRateLimitExceeded(HttpServletResponse response, String clientId) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        String jsonResponse = String.format(
            "{\"error\": \"Rate limit exceeded\", \"clientId\": \"%s\", \"timestamp\": %d}",
            clientId, System.currentTimeMillis()
        );
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    private void handleFallback(HttpServletResponse response, String serviceName, String reason) throws IOException {
        response.setStatus(503);
        response.setContentType("application/json");
        String jsonResponse = String.format(
            "{\"error\": \"Service %s unavailable\", \"reason\": \"%s\", \"timestamp\": %d, \"fallback\": true}",
            serviceName, reason, System.currentTimeMillis()
        );
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    private Optional<Route> findMatchingRoute(String path) {
        return routes.stream()
            .filter(route -> path.matches(route.path()))
            .findFirst();
    }

    private boolean shouldSkip(String path) {
        return path.startsWith("/actuator") ||
            path.startsWith("/health") ||
            path.startsWith("/favicon.ico") ||
            path.equals("/") ||
            path.startsWith("/static/") ||
            path.startsWith("/css/") ||
            path.startsWith("/js/");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
        serviceInstances.clear();
        serviceCacheTime.clear();
        circuitBreakerCache.clear();
        circuitBreakerCacheTime.clear();
    }
}
