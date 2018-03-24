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
    @Override
    public void apply(@NotNull final Project project) {
        // This plugin builds on the CAGatewayDeveloperBase plugin to define conventions
        project.getPlugins().apply(CAGatewayDeveloperBase.class);
        // Add the base plugin to add standard lifecycle tasks: https://docs.gradle.org/current/userguide/standard_plugins.html#sec:base_plugins
        project.getPlugins().apply("base");

        final File buildDir = new File(project.getBuildDir(), "gateway");
        final File solutionDir = new File(project.getProjectDir(), "src/main/gateway");
        final File bundleFile = new File(buildDir, project.getName() + ".bundle");

        // Create build-bundle task
        final BuildBundleTask buildBundleTask = project.getTasks().create("build-bundle", BuildBundleTask.class, t -> {
            t.getFrom().set(solutionDir);
            t.getInto().set(bundleFile);
        });

        // add build-bundle to the default build task
        project.getTasks().maybeCreate("build").dependsOn(buildBundleTask);

        // add the built bundle to the artifacts
        project.artifacts(artifactHandler -> artifactHandler.add("archives", bundleFile, configurablePublishArtifact -> {
            configurablePublishArtifact.builtBy(buildBundleTask);
            configurablePublishArtifact.setExtension("bundle");
            configurablePublishArtifact.setName(project.getName());
            configurablePublishArtifact.setType("bundle");
        }));

    }
}

