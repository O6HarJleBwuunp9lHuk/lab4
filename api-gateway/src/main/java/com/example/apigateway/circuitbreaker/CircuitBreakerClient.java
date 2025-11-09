package com.example.apigateway.circuitbreaker;

import com.example.apigateway.kafka.CircuitBreakerEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class CircuitBreakerClient {

    private final RestTemplate restTemplate;
    private final CircuitBreakerEventProducer eventProducer;
    private final String circuitBreakerServiceUrl;

    public CircuitBreakerClient(RestTemplate restTemplate,
                                CircuitBreakerEventProducer eventProducer) {
        this.restTemplate = restTemplate;
        this.eventProducer = eventProducer;
        this.circuitBreakerServiceUrl = "http://circuit-breaker-service:8083";
    }

    public boolean allowRequest(String breakerName) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                circuitBreakerServiceUrl + "/api/circuit-breaker/" + breakerName + "/allow",
                Map.class
            );

            if (response.getBody() != null) {
                return Boolean.TRUE.equals(response.getBody().get("allowed"));
            }
        } catch (Exception e) {
            System.err.println("Error checking circuit breaker: " + e.getMessage());
        }
        return false;
    }

    public void recordSuccess(String breakerName, String serviceName) {
        eventProducer.recordSuccess(breakerName, serviceName);
    }

    public void recordFailure(String breakerName, String serviceName) {
        eventProducer.recordFailure(breakerName, serviceName);
    }
}
