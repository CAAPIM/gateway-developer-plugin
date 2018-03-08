/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.tasks.explode.ExplodeBundleTask;
import com.ca.apim.gateway.cagatewayconfig.tasks.export.BuildExportQueryTask;
import com.ca.apim.gateway.cagatewayconfig.tasks.export.ExportTask;
import com.ca.apim.gateway.cagatewayconfig.tasks.export.GatewayConnectionProperties;
import com.ca.apim.gateway.cagatewayconfig.tasks.sanitize.SanitizeBundleTask;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.ZipBundleTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Delete;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CAGatewayConfig implements Plugin<Project> {
    @Override
    public void apply(@NotNull final Project project) {
        // Add the base plugin to add standard lifecycle tasks: https://docs.gradle.org/current/userguide/standard_plugins.html#sec:base_plugins
        project.getPlugins().apply("base");

        // define capabilities

        // define conventions
        final GatewayConnectionProperties extension = project.getExtensions().create("GatewayConnection", GatewayConnectionProperties.class, project);

        final File buildDir = new File(project.getBuildDir(), "gateway");
        final File exportDir = new File(project.getProjectDir(), "src/main/gateway");
        final File rawExportBundleFile = new File(buildDir, "raw-export.bundle");
        final File sanitizedExportBundleFile = new File(buildDir, "sanitized-export.bundle");
        final File zippedBundleFile = new File(buildDir, project.getName() + ".bundle");


        // Set Defaults
        project.afterEvaluate(p -> setDefaults(extension));

        BuildExportQueryTask buildExportQueryTask = project.getTasks().create("build-export-query", BuildExportQueryTask.class, t -> t.setGatewayConnectionProperties(extension));

        ExportTask exportTask = project.getTasks().create("export-raw", ExportTask.class, t -> {
            t.setGatewayConnectionProperties(extension);
            t.getExportFile().set(rawExportBundleFile);
            t.getExportQuery().set(buildExportQueryTask.getExportQuery());
        });
        exportTask.dependsOn(buildExportQueryTask);

        SanitizeBundleTask sanitizeTask = project.getTasks().create("sanitize-export", SanitizeBundleTask.class, sanitizeBundleTask -> {
            sanitizeBundleTask.getInputBundleFile().set(rawExportBundleFile);
            sanitizeBundleTask.getOutputBundleFile().set(sanitizedExportBundleFile);
        });
        sanitizeTask.dependsOn(exportTask);

        ExplodeBundleTask explodeBundleTask = project.getTasks().create("export", ExplodeBundleTask.class, t -> {
            t.getInputBundleFile().set(sanitizedExportBundleFile);
            t.getExportDir().set(exportDir);
        });
        explodeBundleTask.dependsOn(sanitizeTask);

        project.getTasks().create("clean-export", Delete.class, t -> t.delete(exportDir));

        ZipBundleTask zipBundleTask = project.getTasks().create("zip", ZipBundleTask.class, t -> {
            t.getInputDir().set(exportDir);
            t.getOutputBundleFile().set(zippedBundleFile);
        });

        project.getTasks().maybeCreate("build").dependsOn(zipBundleTask);

        project.artifacts(artifactHandler -> artifactHandler.add("archives", zippedBundleFile, configurablePublishArtifact -> {
            configurablePublishArtifact.builtBy(zipBundleTask);
            configurablePublishArtifact.setExtension("bundle");
            configurablePublishArtifact.setName(project.getName());
            configurablePublishArtifact.setType("bundle");
        }));

    }

    private static void setDefaults(final GatewayConnectionProperties extension) {
        if (!extension.getUrl().isPresent()) {
            extension.getUrl().set("https://localhost:8443/restman");
        }
        if (!extension.getUserName().isPresent()) {
            extension.getUserName().set("admin");
        }
        if (!extension.getUserPass().isPresent()) {
            extension.getUserPass().set("password");
        }
    }
}

