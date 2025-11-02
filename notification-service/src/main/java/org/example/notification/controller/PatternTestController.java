package org.example.notification.controller;

import org.example.notification.circuitbreaker.CircuitBreaker;
import org.example.notification.discovery.ServiceInstance;
import org.example.notification.discovery.ServiceRegistry;
import org.example.notification.circuitbreaker.CircuitBreakerRegistry;
import org.example.notification.ratelimit.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/test")
public class PatternTestController {

    private final ServiceRegistry serviceRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiter rateLimiter;
    private final AtomicInteger requestCount = new AtomicInteger(0);

    public PatternTestController(ServiceRegistry serviceRegistry,
                                 CircuitBreakerRegistry circuitBreakerRegistry,
                                 RateLimiter rateLimiter) {
        this.serviceRegistry = serviceRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.rateLimiter = rateLimiter;
    }

    @CircuitBreaker(
        name = "test-circuit-breaker",
        failureThreshold = 3,
        timeout = 30000,
        fallbackMethod = "circuitBreakerFallback"
    )
    @GetMapping("/circuit-breaker")
    public ResponseEntity<String> testCircuitBreaker(@RequestParam(defaultValue = "false") boolean fail) {
        int count = requestCount.incrementAndGet();
        System.out.println("Circuit Breaker Test - Request #" + count + ", fail: " + fail);

        if (fail) {
            throw new RuntimeException("Simulated failure for Circuit Breaker test!");
        }

        return ResponseEntity.ok("Circuit Breaker working! Request #" + count);
    }

    public ResponseEntity<String> circuitBreakerFallback(boolean fail) {
        return ResponseEntity.status(503)
            .body("Circuit Breaker OPEN - Using fallback");
    }

    @GetMapping("/service-discovery")
    public ResponseEntity<Map<String, Object>> testServiceDiscovery() {
        System.out.println("Testing Service Discovery...");

        ServiceInstance testInstance = new ServiceInstance();
        testInstance.setInstanceId("test-instance-" + System.currentTimeMillis());
        testInstance.setServiceName("test-service");
        testInstance.setHost("localhost");
        testInstance.setPort(9090);
        testInstance.setLoad(5);

        serviceRegistry.registerService(testInstance);

        List<ServiceInstance> allServices = serviceRegistry.getAllServices();
        List<ServiceInstance> aliveServices = serviceRegistry.getAliveServices();

        return ResponseEntity.ok(Map.of(
            "registeredTestService", testInstance.getInstanceId(),
            "totalServices", serviceRegistry.getRegisteredServicesCount(),
            "aliveServices", serviceRegistry.getAliveServicesCount(),
            "allServices", allServices,
            "aliveServicesList", aliveServices
        ));
    }

    @GetMapping("/rate-limiting")
    public ResponseEntity<String> testRateLimiting(@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        String clientId = apiKey != null ? apiKey : "test-client";

        boolean allowed = rateLimiter.allowRequest(clientId);

        if (allowed) {
            return ResponseEntity.ok("Rate Limiting: Request allowed for client: " + clientId);
        } else {
            return ResponseEntity.status(429)
                .body("Rate Limiting: Too many requests for client: " + clientId);
        }
    }

    @GetMapping("/api-gateway")
    public ResponseEntity<String> testApiGateway() {
        return ResponseEntity.ok("API Gateway: This request passed through gateway successfully");
    }

    @CircuitBreaker(
        name = "comprehensive-test",
        failureThreshold = 2,
        timeout = 30000,
        fallbackMethod = "comprehensiveFallback"
    )
    @GetMapping("/comprehensive")
    public ResponseEntity<Map<String, Object>> comprehensiveTest(
        @RequestParam(defaultValue = "false") boolean fail,
        @RequestHeader(value = "X-API-Key", required = false) String apiKey) {

        String clientId = apiKey != null ? apiKey : "comprehensive-client";
        if (!rateLimiter.allowRequest(clientId)) {
            throw new RuntimeException("Rate limit exceeded");
        }

        List<ServiceInstance> services = serviceRegistry.getAllServices();

        if (fail) {
            throw new RuntimeException("Comprehensive test failure");
        }

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "pattern", "Comprehensive test passed",
            "clientId", clientId,
            "registeredServices", serviceRegistry.getRegisteredServicesCount(),
            "services", services.stream().map(ServiceInstance::getInstanceId).toList(),
            "timestamp", System.currentTimeMillis()
        ));
    }

    public ResponseEntity<Map<String, Object>> comprehensiveFallback(boolean fail, String apiKey) {
        return ResponseEntity.status(503).body(Map.of(
            "status", "fallback",
            "pattern", "Circuit Breaker activated",
            "message", "Service unavailable - using fallback",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getPatternsStatus() {
        return ResponseEntity.ok(Map.of(
            "circuitBreakers", Map.of(
                "test-circuit-breaker", circuitBreakerRegistry.getCircuitBreakerState("test-circuit-breaker").toString(),
                "comprehensive-test", circuitBreakerRegistry.getCircuitBreakerState("comprehensive-test").toString(),
                "emailService", circuitBreakerRegistry.getCircuitBreakerState("emailService").toString()
            ),
            "serviceDiscovery", Map.of(
                "totalServices", serviceRegistry.getRegisteredServicesCount(),
                "aliveServices", serviceRegistry.getAliveServicesCount()
            ),
            "rateLimiting", "Active",
            "apiGateway", "Active",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
