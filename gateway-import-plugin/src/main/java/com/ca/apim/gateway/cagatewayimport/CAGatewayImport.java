/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayimport;

import com.ca.apim.gateway.cagatewayimport.config.GatewayConnectionProperties;
import com.ca.apim.gateway.cagatewayimport.tasks.ImportBundleTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CAGatewayImport implements Plugin<Project> {
    @Override
    public void apply(@NotNull final Project project) {
        // This plugin builds on the CAGatewayExportBase plugin to define conventions
        project.getPlugins().apply(CAGatewayImportBase.class);
        // Add the base plugin to add standard lifecycle tasks: https://docs.gradle.org/current/userguide/standard_plugins.html#sec:base_plugins
        project.getPlugins().apply("base");

        final GatewayConnectionProperties gatewayConnectionProperties = project.getExtensions().create("GatewayImportConnection", GatewayConnectionProperties.class, project);
        // Set Defaults
        project.afterEvaluate(p -> setDefaults(gatewayConnectionProperties));

        project.getTasks().create("import-bundle", ImportBundleTask.class, t -> t.setGatewayConnectionProperties(gatewayConnectionProperties));
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

