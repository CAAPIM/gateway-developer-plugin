/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.BuildBundleTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CAGatewayDeveloper implements Plugin<Project> {

    private static final String BUNDLE_CONFIGURATION = "bundle";
    private static final String BUNDLE_FILE_EXTENSION = "bundle";

    @Override
    public void apply(@NotNull final Project project) {
        // This plugin builds on the CAGatewayDeveloperBase plugin to define conventions
        project.getPlugins().apply(CAGatewayDeveloperBase.class);
        // Add the base plugin to add standard lifecycle tasks: https://docs.gradle.org/current/userguide/standard_plugins.html#sec:base_plugins
        project.getPlugins().apply("base");

        final GatewayDeveloperPluginConfig pluginConfig = project.getExtensions().create("GatewaySourceConfig", GatewayDeveloperPluginConfig.class, project);
        // Set Defaults
        project.afterEvaluate(p -> setDefaults(pluginConfig, project));

        //Add bundle configuration
        project.getConfigurations().create(BUNDLE_CONFIGURATION);

        // Create build-bundle task
        final BuildBundleTask buildBundleTask = project.getTasks().create("build-bundle", BuildBundleTask.class, t -> {
            t.getFrom().set(pluginConfig.getSolutionDir());
            t.getInto().set(pluginConfig.getBuiltBundle());
            t.getDependencies().setFrom(project.getConfigurations().getByName(BUNDLE_CONFIGURATION));
        });

        // add build-bundle to the default build task
        project.afterEvaluate(p -> project.getTasks().getByPath("build").dependsOn(buildBundleTask));

        // add the built bundle to the artifacts
        project.artifacts(artifactHandler -> artifactHandler.add("archives", pluginConfig.getBuiltBundle(), configurablePublishArtifact -> {
            configurablePublishArtifact.builtBy(buildBundleTask);
            configurablePublishArtifact.setExtension(BUNDLE_FILE_EXTENSION);
            configurablePublishArtifact.setName(project.getName());
            configurablePublishArtifact.setType(BUNDLE_FILE_EXTENSION);
        }));

    }

    private void setDefaults(GatewayDeveloperPluginConfig pluginConfig, Project project) {
        if (!pluginConfig.getSolutionDir().isPresent()) {
            pluginConfig.getSolutionDir().set(new File(project.getProjectDir(), "src/main/gateway"));
        }
        if (!pluginConfig.getBuiltBundle().isPresent()) {
            pluginConfig.getBuiltBundle().set(new File(new File(project.getBuildDir(), "gateway"), project.getName() + ".bundle"));
        }
    }
}

