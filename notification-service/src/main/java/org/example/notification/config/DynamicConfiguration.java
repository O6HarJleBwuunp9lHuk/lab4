package org.example.notification.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class DynamicConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DynamicConfiguration.class);
    private final Map<String, String> config = new ConcurrentHashMap<>();
    private final String configFile = "application-config.properties";
    private final List<ConfigurationListener> listeners = new CopyOnWriteArrayList<>();
    private long lastModified = 0;

    @PostConstruct
    public void init() {
        loadConfiguration();
        startConfigWatcher();
        logger.info("DynamicConfiguration initialized with {} configuration entries", config.size());
    }

    public String getProperty(String key, String defaultValue) {
        String value = config.getOrDefault(key, defaultValue);
        logger.debug("Getting property: key={}, value={}, defaultValue={}", key, value, defaultValue);
        return value;
    }

    public int getIntProperty(String key, int defaultValue) {
        try {
            int value = Integer.parseInt(config.get(key));
            logger.debug("Getting int property: key={}, value={}", key, value);
            return value;
        } catch (Exception e) {
            logger.debug("Failed to parse int property: key={}, using defaultValue={}", key, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = config.get(key);
        boolean result = value != null ? Boolean.parseBoolean(value) : defaultValue;
        logger.debug("Getting boolean property: key={}, value={}, result={}", key, value, result);
        return result;
    }

    public void setProperty(String key, String value) {
        String oldValue = config.put(key, value);
        logger.info("Setting property: key={}, oldValue={}, newValue={}", key, oldValue, value);

        if (!Objects.equals(oldValue, value)) {
            notifyListeners(key, oldValue, value);
        }
        saveConfiguration();
    }

    private void loadConfiguration() {
        File file = new File(configFile);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(input);
                props.forEach((k, v) -> config.put((String) k, (String) v));
                lastModified = file.lastModified();
                logger.info("Configuration loaded from file: {} entries", config.size());
            } catch (IOException e) {
                logger.error("Failed to load configuration from file: {}", configFile, e);
            }
        } else {
            logger.warn("Configuration file not found: {}", configFile);
        }
    }

    private void saveConfiguration() {
        try (OutputStream output = new FileOutputStream(configFile)) {
            Properties props = new Properties();
            props.putAll(config);
            props.store(output, "Dynamic configuration");
            logger.debug("Configuration saved to file: {} entries", config.size());
        } catch (IOException e) {
            logger.error("Failed to save configuration to file: {}", configFile, e);
        }
    }

    private void startConfigWatcher() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            File file = new File(configFile);
            if (file.exists() && file.lastModified() > lastModified) {
                logger.info("Configuration file changed, reloading...");
                loadConfiguration();
            }
        }, 5, 5, TimeUnit.SECONDS);

        logger.debug("Configuration file watcher started");
    }

    public void addListener(ConfigurationListener listener) {
        listeners.add(listener);
        logger.debug("Configuration listener added: {}", listener.getClass().getSimpleName());
    }

    private void notifyListeners(String key, String oldValue, String newValue) {
        logger.info("Notifying {} listeners about config change: key={}, oldValue={}, newValue={}",
            listeners.size(), key, oldValue, newValue);

        listeners.forEach(listener -> {
            try {
                listener.onConfigChange(key, oldValue, newValue);
                logger.debug("Successfully notified listener: {}", listener.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("Error notifying configuration listener: {}", listener.getClass().getSimpleName(), e);
            }
        });
    }
}
