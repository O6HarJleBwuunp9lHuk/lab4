package org.example.notification.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class EmailConfigManager implements ConfigurationListener {
    private static final Logger logger = LoggerFactory.getLogger(EmailConfigManager.class);

    @Autowired
    private DynamicConfiguration config;

    @PostConstruct
    public void init() {
        config.addListener(this);
        loadInitialConfig();
        logger.info("EmailConfigManager initialized and registered as configuration listener");
    }

    private void loadInitialConfig() {
        config.setProperty("email.enabled", "true");
        config.setProperty("email.timeout", "5000");
        config.setProperty("email.retry.count", "3");
        config.setProperty("circuitbreaker.failure.threshold", "5");
        logger.debug("Initial email configuration loaded");
    }

    @Override
    public void onConfigChange(String key, String oldValue, String newValue) {
        logger.info("Configuration changed: key={}, oldValue={}, newValue={}", key, oldValue, newValue);

        switch (key) {
            case "email.enabled":
                boolean enabled = Boolean.parseBoolean(newValue);
                logger.info("Email service {}: {}", enabled ? "enabled" : "disabled", enabled);
                break;
            case "email.timeout":
                logger.info("Email timeout changed from {}ms to {}ms", oldValue, newValue);
                break;
            case "email.retry.count":
                logger.info("Email retry count changed from {} to {}", oldValue, newValue);
                break;
            case "circuitbreaker.failure.threshold":
                logger.info("Circuit breaker failure threshold changed from {} to {}", oldValue, newValue);
                break;
            default:
                logger.debug("Unhandled configuration change: key={}", key);
                break;
        }
    }
}
