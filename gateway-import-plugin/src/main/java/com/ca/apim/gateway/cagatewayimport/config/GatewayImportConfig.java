package com.ca.apim.gateway.cagatewayimport.config;

import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;

import java.io.File;

public class GatewayImportConfig {
    private ListProperty<File> bundles;
    public GatewayImportConfig(Project project) {
        bundles = project.getObjects().listProperty(File.class);
    }

    public ListProperty<File> getBundles() {
        return bundles;
    }

    public void setBundles(ListProperty<File> bundles) {
        this.bundles = bundles;
    }
}
