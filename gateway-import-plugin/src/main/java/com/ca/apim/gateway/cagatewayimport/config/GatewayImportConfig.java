package com.ca.apim.gateway.cagatewayimport.config;

import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;

public class GatewayImportConfig {
    private ConfigurableFileCollection bundles;
    public GatewayImportConfig(Project project) {
        bundles = project.files();
    }

    public ConfigurableFileCollection getBundles() {
        return bundles;
    }

    public void setBundles(ConfigurableFileCollection bundles) {
        this.bundles = bundles;
    }
}
