/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

import java.util.Map;

public class GatewayExportPluginConfig {
    private final DirectoryProperty solutionDir;
    private final RegularFileProperty rawBundle;
    private final RegularFileProperty sanitizedBundle;
    private final Property<Map> exportEntities;
    private Property<String> folderPath;

    public GatewayExportPluginConfig(Project project) {
        solutionDir = project.getLayout().directoryProperty();
        rawBundle = project.getLayout().fileProperty();
        sanitizedBundle = project.getLayout().fileProperty();
        exportEntities = project.getObjects().property(Map.class);
        folderPath = project.getObjects().property(String.class);
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

    Property<Map> getExportEntities() {
        return exportEntities;
    }

    /**
     * The path of the folder to export.
     *
     * @return the folder to export from the gateway
     */
    @Input
    public Property<String> getFolderPath() {
        return folderPath;
    }
}
