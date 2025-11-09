package com.example.servicediscovery.kafka;

import com.example.servicediscovery.event.ServiceRegistrationEvent;
import com.example.servicediscovery.event.ServiceHeartbeatEvent;
import com.example.servicediscovery.event.ServiceUnregistrationEvent;
import com.example.servicediscovery.service.ServiceRegistryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ServiceRegistrationConsumer {

    private final ServiceRegistryService registryService;

    public ServiceRegistrationConsumer(ServiceRegistryService registryService) {
        this.registryService = registryService;
    }

    @KafkaListener(topics = "service-registration-events")
    public void handleRegistration(ServiceRegistrationEvent event) {
        System.out.println("Received registration event: " + event.getInstanceId());
        registryService.registerService(event);
    }

    @KafkaListener(topics = "service-heartbeat-events")
    public void handleHeartbeat(ServiceHeartbeatEvent event) {
        System.out.println("Received heartbeat event: " + event.getInstanceId());
        registryService.updateHeartbeat(event);
    }

    @KafkaListener(topics = "service-unregistration-events")
    public void handleUnregistration(ServiceUnregistrationEvent event) {
        System.out.println("Received unregistration event: " + event.getInstanceId());
        registryService.unregisterService(event);
    }
}
