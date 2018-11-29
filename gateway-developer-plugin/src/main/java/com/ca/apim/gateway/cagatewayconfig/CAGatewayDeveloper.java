/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.tasks.gw7.PackageTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.provider.DefaultProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class CAGatewayDeveloper implements Plugin<Project> {

    private static final String BUNDLE_CONFIGURATION = "bundle";
    private static final String MODULAR_ASSERTION_CONFIGURATION = "assertion";
    private static final String CUSTOM_ASSERTION_CONFIGURATION = "customassertion";
    private static final String BUNDLE_FILE_EXTENSION = "bundle";
    private static final String BUILT_BUNDLE_DIRECTORY = "bundle";
    private static final String GATEWAY_BUILD_DIRECTORY = "gateway";
    private static final String ENV_APPLICATION_CONFIGURATION = "environment-creator-application";

    @Override
    public void apply(@NotNull final Project project) {
        // This plugin builds on the CAGatewayDeveloperBase plugin to define conventions
        project.getPlugins().apply(CAGatewayDeveloperBase.class);
        // Add the base plugin to add standard lifecycle tasks: https://docs.gradle.org/current/userguide/standard_plugins.html#sec:base_plugins
        project.getPlugins().apply("base");

        final GatewayDeveloperPluginConfig pluginConfig = project.getExtensions().create("GatewaySourceConfig", GatewayDeveloperPluginConfig.class, project);
        // Set Defaults
        project.afterEvaluate(p -> setDefaults(pluginConfig, project));

        //Add bundle configuration to the default configuration. This was transitive dependencies can be retrieved.
        createConfiguration(project, BUNDLE_CONFIGURATION);
        // Add modular and custom assertion dependencies configuration
        createConfiguration(project, MODULAR_ASSERTION_CONFIGURATION);
        createConfiguration(project, CUSTOM_ASSERTION_CONFIGURATION);

        // This is the configuration for the apply environment application that gets bundled within .gw7 packages.
        project.getConfigurations().create(ENV_APPLICATION_CONFIGURATION);

        //Attempt to use the same version of the environment creator application as this plugin.
        String version = this.getClass().getPackage().getImplementationVersion();
        project.getDependencies().add(ENV_APPLICATION_CONFIGURATION, "com.ca.apim.gateway:environment-creator-application:" + (version != null ? version : "+"));

        // Create build-bundle task
        final BuildBundleTask buildBundleTask = project.getTasks().create("build-bundle", BuildBundleTask.class, t -> {
            t.dependsOn(project.getConfigurations().getByName(BUNDLE_CONFIGURATION));
            t.getFrom().set(new DefaultProvider<>(() -> {
                Directory dir = pluginConfig.getSolutionDir().get();
                return dir.getAsFile().exists() ? dir : null;
            }));
            t.getInto().set(pluginConfig.getBuiltBundleDir());
            t.getDependencies().setFrom(project.getConfigurations().getByName(BUNDLE_CONFIGURATION));
        });

        // Create package task
        final PackageTask packageGW7Task = project.getTasks().create("package-gw7", PackageTask.class, t -> {
            t.dependsOn(buildBundleTask);
            t.getInto().set(new DefaultProvider<RegularFile>(() -> () -> new File(new File(project.getBuildDir(), GATEWAY_BUILD_DIRECTORY), getBuiltArtifactName(project, "gw7"))));
            t.getBundle().set(pluginConfig.getBuiltBundleDir().file(new DefaultProvider<>(() -> getBuiltArtifactName(project, BUNDLE_FILE_EXTENSION))));
            t.getDependencyBundles().setFrom(project.getConfigurations().getByName(BUNDLE_CONFIGURATION));
            t.getContainerApplicationDependencies().setFrom(project.getConfigurations().getByName(ENV_APPLICATION_CONFIGURATION));
            t.getDependencyModularAssertions().setFrom(project.getConfigurations().getByName(MODULAR_ASSERTION_CONFIGURATION));
            t.getDependencyCustomAssertions().setFrom(project.getConfigurations().getByName(CUSTOM_ASSERTION_CONFIGURATION));
        });

        // add build-bundle to the default build task
        project.afterEvaluate(p -> project.getTasks().getByPath("build").dependsOn(buildBundleTask, packageGW7Task));

        // add the built bundle to the default artifacts
        project.artifacts(artifactHandler -> artifactHandler
                .add("default", new LazyPublishArtifact(packageGW7Task.getBundle(), null) {
                            //We need to override this because gradle does not fully lazily load artifacts. Once we move to gradle 5 this will no longer be needed
                            @Override
                            public String getType() {
                                return BUNDLE_FILE_EXTENSION;
                            }
                        },
                        configurablePublishArtifact -> {
                            configurablePublishArtifact.builtBy(buildBundleTask);
                            configurablePublishArtifact.setExtension(BUNDLE_FILE_EXTENSION);
                            configurablePublishArtifact.setName(project.getName());
                            configurablePublishArtifact.setType(BUNDLE_FILE_EXTENSION);
                        }));
    }

    private void createConfiguration(Project project, String configurationName) {
        Configuration configuration = project.getConfigurations().create(configurationName);
        project.getConfigurations().getByName("default").extendsFrom(configuration);
    }

    @NotNull
    private String getBuiltArtifactName(@NotNull Project project, String bundleRequiredFileExtension) {
        return project.getName() + '-' + project.getVersion() + "." + bundleRequiredFileExtension;
    }

    private void setDefaults(GatewayDeveloperPluginConfig pluginConfig, Project project) {
        if (!pluginConfig.getSolutionDir().isPresent()) {
            pluginConfig.getSolutionDir().set(new File(project.getProjectDir(), "src/main/gateway"));
        }
        if (!pluginConfig.getBuiltBundleDir().isPresent()) {
            pluginConfig.getBuiltBundleDir().set(new File(new File(project.getBuildDir(), GATEWAY_BUILD_DIRECTORY), BUILT_BUNDLE_DIRECTORY));
        }
    }
}

