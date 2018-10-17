/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;

public class GatewayExportPluginConfig {
    private final DirectoryProperty solutionDir;
    private final RegularFileProperty rawBundle;
    private final RegularFileProperty sanitizedBundle;
    private final GatewayExportEntities gatewayExportEntities;

    public GatewayExportPluginConfig(Project project) {
        solutionDir = project.getLayout().directoryProperty();
        rawBundle = project.getLayout().fileProperty();
        sanitizedBundle = project.getLayout().fileProperty();
        gatewayExportEntities = project.getObjects().newInstance(GatewayExportEntities.class, project);
    }

    DirectoryProperty getSolutionDir() {
        return solutionDir;
    }

    RegularFileProperty getRawBundle() {
        return rawBundle;
    }

    RegularFileProperty getSanitizedBundle() {
        return sanitizedBundle;
    }

    GatewayExportEntities getGatewayExportEntities() {
        return gatewayExportEntities;
    }

    void exportEntities(Action<? super GatewayExportEntities> action) {
        action.execute(gatewayExportEntities);
    }
}
