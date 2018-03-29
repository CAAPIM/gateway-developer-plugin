/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;

public class GatewayExportPluginConfig {
    private final DirectoryProperty solutionDir;
    private final RegularFileProperty rawBundle;
    private final RegularFileProperty sanitizedBundle;

    public GatewayExportPluginConfig(Project project) {
        solutionDir = project.getLayout().directoryProperty();
        rawBundle = project.getLayout().fileProperty();
        sanitizedBundle = project.getLayout().fileProperty();
    }

    public DirectoryProperty getSolutionDir() {
        return solutionDir;
    }

    public RegularFileProperty getRawBundle() {
        return rawBundle;
    }

    public RegularFileProperty getSanitizedBundle() {
        return sanitizedBundle;
    }
}
