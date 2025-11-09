package com.example.servicediscovery.controller;

import com.example.servicediscovery.service.ServiceRegistryService;
import org.example.common.event.ServiceInstance;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discovery")
public class ServiceDiscoveryController {

    private final ServiceRegistryService registryService;

    public ServiceDiscoveryController(ServiceRegistryService registryService) {
        this.registryService = registryService;
    }

    @GetMapping("/services/{serviceName}")
    public List<ServiceInstance> getServices(@PathVariable String serviceName) {
        return registryService.getServices(serviceName);
    }

    @GetMapping("/service/{serviceName}")
    public ServiceInstance getService(@PathVariable String serviceName) {
        return registryService.getService(serviceName)
            .orElseThrow(() -> new RuntimeException("Service not found: " + serviceName));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "service-discovery");
    }
}
