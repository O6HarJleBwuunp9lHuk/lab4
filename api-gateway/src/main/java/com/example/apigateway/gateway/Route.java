package com.example.apigateway.gateway;

public record Route(String path, String serviceName) {
    @Override
    public String toString() {
        return "Route{path='" + path + "', serviceName='" + serviceName + "'}";
    }
}
