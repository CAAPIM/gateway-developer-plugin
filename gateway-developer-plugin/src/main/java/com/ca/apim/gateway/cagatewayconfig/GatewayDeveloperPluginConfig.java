/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

import java.util.Map;

public class GatewayDeveloperPluginConfig {

    private final DirectoryProperty solutionDir;
    private DirectoryProperty builtBundleDir;
    private final Property<Map> environmentConfig;
    private DirectoryProperty builtEnvironmentBundleDir;
    private final Property<Boolean> detemplatizeDeploymentBundles;
    private final Property<String> configFolder;
    private final Property<String> configName;

    public GatewayDeveloperPluginConfig(Project project) {
        solutionDir = project.getLayout().directoryProperty();
        builtBundleDir = project.getLayout().directoryProperty();
        environmentConfig = project.getObjects().property(Map.class);
        builtEnvironmentBundleDir = project.getLayout().directoryProperty();
        detemplatizeDeploymentBundles = project.getObjects().property(Boolean.class);
        configFolder = project.getObjects().property(String.class);
        configName = project.getObjects().property(String.class);
    }

    DirectoryProperty getSolutionDir() {
        return solutionDir;
    }

    DirectoryProperty getBuiltBundleDir() {
        return builtBundleDir;
    }

    Property<Map> getEnvironmentConfig() {
        return environmentConfig;
    }

    DirectoryProperty getBuiltEnvironmentBundleDir() {
        return builtEnvironmentBundleDir;
    }

    public Property<Boolean> getDetemplatizeDeploymentBundles() {
        return detemplatizeDeploymentBundles;
    }

    Property<String> getConfigFolder() {
        return configFolder;
    }

    Property<String> getConfigName() {
        return configName;
    }
}
