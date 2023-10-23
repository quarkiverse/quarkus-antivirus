package io.quarkiverse.antivirus.deployment;

import java.util.Map;

import io.quarkus.builder.item.SimpleBuildItem;

/**
 * Build item used to carry running values to Dev UI.
 */
public final class ClamAVDevServicesConfigBuildItem extends SimpleBuildItem {

    private final Map<String, String> config;

    public ClamAVDevServicesConfigBuildItem(Map<String, String> config) {
        this.config = config;
    }

    public Map<String, String> getConfig() {
        return config;
    }

}