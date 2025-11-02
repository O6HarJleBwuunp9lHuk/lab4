package org.example.notification.config;

public interface ConfigurationListener {
    void onConfigChange(String key, String oldValue, String newValue);
}
