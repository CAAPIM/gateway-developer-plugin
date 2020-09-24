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
    private Property<String> targetFolderPath;
    private DirectoryProperty builtBundleDir;
    private DirectoryProperty builtEnvironmentBundleDir;
    private final Property<Boolean> detemplatizeDeploymentBundles;
    private final EnvironmentConfig envConfig;
    //for backward compatibility
    private final Property<Map> environmentConfig;

    public GatewayDeveloperPluginConfig(Project project, EnvironmentConfig environmentConfig) {
        solutionDir = project.getLayout().directoryProperty();
        targetFolderPath = project.getObjects().property(String.class);
        builtBundleDir = project.getLayout().directoryProperty();
        builtEnvironmentBundleDir = project.getLayout().directoryProperty();
        detemplatizeDeploymentBundles = project.getObjects().property(Boolean.class);
        this.environmentConfig = project.getObjects().property(Map.class);
        this.envConfig = environmentConfig;
    }

    DirectoryProperty getSolutionDir() {
        return solutionDir;
    }

    DirectoryProperty getBuiltBundleDir() {
        return builtBundleDir;
    }

    DirectoryProperty getBuiltEnvironmentBundleDir() {
        return builtEnvironmentBundleDir;
    }

    public Property<Boolean> getDetemplatizeDeploymentBundles() {
        return detemplatizeDeploymentBundles;
    }

    public EnvironmentConfig getEnvConfig() {
        return envConfig;
    }

    public Property<Map> getEnvironmentConfig() {
        return environmentConfig;
    }

    public Property<String> getTargetFolderPath() {
        return targetFolderPath;
    }
}
