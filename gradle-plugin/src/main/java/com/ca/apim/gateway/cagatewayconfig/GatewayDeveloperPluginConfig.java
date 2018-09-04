/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;

public class GatewayDeveloperPluginConfig {
    private final DirectoryProperty solutionDir;
    private DirectoryProperty builtBundleDir;

    public GatewayDeveloperPluginConfig(Project project) {
        solutionDir = project.getLayout().directoryProperty();
        builtBundleDir = project.getLayout().directoryProperty();
    }

    DirectoryProperty getSolutionDir() {
        return solutionDir;
    }

    DirectoryProperty getBuiltBundleDir() {
        return builtBundleDir;
    }
}
