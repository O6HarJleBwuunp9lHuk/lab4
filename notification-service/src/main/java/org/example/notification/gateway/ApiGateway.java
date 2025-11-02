package org.example.notification.gateway;

import org.example.notification.discovery.ServiceInstance;
import org.example.notification.discovery.ServiceRegistry;
import org.example.notification.circuitbreaker.CircuitBreakerImpl;
import org.example.notification.circuitbreaker.CircuitBreakerRegistry;
import org.example.notification.ratelimit.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

@Component
public class ApiGateway implements Filter {

    private final ServiceRegistry serviceRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiter rateLimiter;
    private final RestTemplate restTemplate;
    private final List<Route> routes;

    @Autowired
    public ApiGateway(ServiceRegistry serviceRegistry,
                      CircuitBreakerRegistry circuitBreakerRegistry,
                      RateLimiter rateLimiter,
                      RestTemplate restTemplate) {
        this.serviceRegistry = serviceRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.rateLimiter = rateLimiter;
        this.restTemplate = restTemplate;
        this.routes = initializeRoutes();
    }

    private List<Route> initializeRoutes() {
        return List.of(
            new Route("/api/users/.*", "user-service"),
            new Route("/health/.*", "health-service"),
            new Route("/actuator/health", "health-service")
        );
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        if (shouldSkip(path)) {
            chain.doFilter(request, response);
            return;
        }

        String clientId = getClientId(httpRequest);
        if (!rateLimiter.allowRequest(clientId)) {
            sendErrorResponse(httpResponse, 429, "Rate limit exceeded");
            return;
        }

        Optional<Route> routeOpt = findMatchingRoute(path);
        if (routeOpt.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        Route route = routeOpt.get();

        CircuitBreakerImpl circuitBreaker = circuitBreakerRegistry.getCircuitBreaker(route.serviceName());
        if (!circuitBreaker.allowRequest()) {
            handleFallback(httpResponse, route.serviceName());
            return;
        }

        try {
            Optional<ServiceInstance> instanceOpt = serviceRegistry.getService(route.serviceName());
            if (instanceOpt.isEmpty()) {
                sendErrorResponse(httpResponse, 503, "Service unavailable: " + route.serviceName());
                return;
            }

            ServiceInstance instance = instanceOpt.get();

            boolean success = forwardRequest(httpRequest, httpResponse, instance, route, path);

            if (success) {
                circuitBreaker.recordSuccess();
            } else {
                circuitBreaker.recordFailure();
            }

        } catch (Exception e) {
            circuitBreaker.recordFailure();
            handleFallback(httpResponse, route.serviceName());
        }
    }

    private boolean shouldSkip(String path) {
        return path.startsWith("/css/") ||
            path.startsWith("/js/") ||
            path.startsWith("/webjars/") ||
            path.startsWith("/favicon.ico") ||
            path.equals("/") ||
            path.equals("/gateway/status");
    }

    private boolean forwardRequest(HttpServletRequest request, HttpServletResponse response,
                                   ServiceInstance instance, Route route, String originalPath) {
        try {
            String targetUrl = buildTargetUrl(instance, route, originalPath);
            HttpHeaders headers = extractHeaders(request);
            HttpEntity<byte[]> requestEntity = prepareRequestEntity(request, headers);
            HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                targetUrl, httpMethod, requestEntity, byte[].class);

            copyResponse(responseEntity, response);

            return responseEntity.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("Error forwarding request to " + route.serviceName() + ": " + e.getMessage());
            return false;
        }
    }

    private HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.add(headerName, headerValues.nextElement());
            }
        }

        headers.remove("host");
        headers.remove("content-length");

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
        return "transfer-encoding".equalsIgnoreCase(headerName) ||
            "connection".equalsIgnoreCase(headerName);
    }

    private String buildTargetUrl(ServiceInstance instance, Route route, String originalPath) {
        String baseUrl = instance.getBaseUrl();

        String routePattern = route.path().replace(".*", "");
        String servicePath = originalPath.replaceFirst(routePattern, "");

        String queryString = getQueryString(originalPath);

        return baseUrl + servicePath + queryString;
    }

    private String getQueryString(String path) {
        return "";
    }

    private boolean hasBody(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equalsIgnoreCase(method) ||
            "PUT".equalsIgnoreCase(method) ||
            "PATCH".equalsIgnoreCase(method);
    }

    private void handleFallback(HttpServletResponse response, String serviceName) throws IOException {
        response.setStatus(503);
        response.setContentType("application/json");
        response.getWriter().write(
            String.format("{\"error\": \"Service %s unavailable\", \"timestamp\": %d, \"fallback\": true}",
                serviceName, System.currentTimeMillis())
        );
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(
            String.format("{\"error\": \"%s\", \"timestamp\": %d}", message, System.currentTimeMillis())
        );
    }

    private Optional<Route> findMatchingRoute(String path) {
        return routes.stream()
            .filter(route -> path.matches(route.path()))
            .findFirst();
    }

    private String getClientId(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            return "auth-" + authHeader.hashCode();
        }
        return request.getRemoteAddr();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("API Gateway initialized with routes: " + routes);
    }

    @Override
    public void destroy() {
        System.out.println("API Gateway destroyed");
    }
}
