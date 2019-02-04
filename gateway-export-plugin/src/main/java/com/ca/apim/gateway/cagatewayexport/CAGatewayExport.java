/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport;

import com.ca.apim.gateway.cagatewayexport.config.GatewayConnectionProperties;
import com.ca.apim.gateway.cagatewayexport.config.GatewayExportPluginConfig;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.ExplodeBundleTask;
import com.ca.apim.gateway.cagatewayexport.tasks.export.BuildExportQueryTask;
import com.ca.apim.gateway.cagatewayexport.tasks.export.ExportTask;
import com.ca.apim.gateway.cagatewayexport.tasks.sanitize.SanitizeBundleTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Delete;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Supplier;

public class CAGatewayExport implements Plugin<Project> {
    @Override
    public void apply(@NotNull final Project project) {
        // This plugin builds on the CAGatewayExportBase plugin to define conventions
        project.getPlugins().apply(CAGatewayExportBase.class);
        // Add the base plugin to add standard lifecycle tasks: https://docs.gradle.org/current/userguide/standard_plugins.html#sec:base_plugins
        project.getPlugins().apply("base");

        final GatewayExportPluginConfig pluginConfig = project.getExtensions().create("GatewayExportConfig", GatewayExportPluginConfig.class, project);
        // Set Defaults
        project.afterEvaluate(p -> setDefaults(pluginConfig, project));

        final GatewayConnectionProperties gatewayConnectionProperties = project.getExtensions().create("GatewayConnection", GatewayConnectionProperties.class, project);
        // Set Defaults
        project.afterEvaluate(p -> setDefaults(gatewayConnectionProperties));

        BuildExportQueryTask buildExportQueryTask = project.getTasks().create("build-export-query", BuildExportQueryTask.class);

        ExportTask exportTask = project.getTasks().create("export-raw", ExportTask.class, t -> {
            t.setGatewayConnectionProperties(gatewayConnectionProperties);
            t.getExportFile().set(pluginConfig.getRawBundle());
            t.getExportQuery().set(buildExportQueryTask.getExportQuery());
        });
        exportTask.dependsOn(buildExportQueryTask);

        SanitizeBundleTask sanitizeTask = project.getTasks().create("sanitize-export", SanitizeBundleTask.class, sanitizeBundleTask -> {
            sanitizeBundleTask.getInputBundleFile().set(pluginConfig.getRawBundle());
            sanitizeBundleTask.getOutputBundleFile().set(pluginConfig.getSanitizedBundle());
        });
        sanitizeTask.dependsOn(exportTask);

        ExplodeBundleTask explodeBundleTask = project.getTasks().create("export", ExplodeBundleTask.class, t -> {
            t.getFolderPath().set(pluginConfig.getFolderPath());
            t.getInputBundleFile().set(pluginConfig.getSanitizedBundle());
            t.getExportDir().set(pluginConfig.getSolutionDir());
            t.getExportEntities().set(pluginConfig.getExportEntities());
        });
        explodeBundleTask.dependsOn(sanitizeTask);

        project.getTasks().create("clean-export", Delete.class, t -> t.delete(pluginConfig.getSolutionDir()));
    }

    private void setDefaults(GatewayExportPluginConfig pluginConfig, @NotNull Project project) {
        if (!pluginConfig.getSolutionDir().isPresent()) {
            pluginConfig.getSolutionDir().set(new File(project.getProjectDir(), "src/main/gateway"));
        }
        if (!pluginConfig.getRawBundle().isPresent()) {
            pluginConfig.getRawBundle().set(new File(new File(project.getBuildDir(), "gateway"), project.getName() + ".raw.bundle"));
        }
        if (!pluginConfig.getSanitizedBundle().isPresent()) {
            pluginConfig.getSanitizedBundle().set(new File(new File(project.getBuildDir(), "gateway"), project.getName() + ".sanitized.bundle"));
        }
    }

    private static void setDefaults(final GatewayConnectionProperties gatewayConnectionProperties) {
        setDefault(gatewayConnectionProperties.getUrl(), () -> "https://localhost:8443/restman");
        setDefault(gatewayConnectionProperties.getUserName(), () -> "admin");
        setDefault(gatewayConnectionProperties.getUserPass(), () -> "password");
    }

    private static <T> void setDefault(Property<T> property, Supplier<T> supplier) {
        if (!property.isPresent()) {
            property.set(supplier.get());
        }
    }
}

