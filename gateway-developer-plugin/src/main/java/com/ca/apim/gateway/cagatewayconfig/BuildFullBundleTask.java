/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreator;
import com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.ProjectDependencyUtils.filterBundleFiles;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static com.ca.apim.gateway.cagatewayconfig.util.injection.ConfigBuilderModule.getInstance;

/**
 * The BuildFullBundleTask task will grab provided environment properties and build a single bundle merged with the deployment bundles.
 */
public class BuildFullBundleTask extends DefaultTask {

    private final DirectoryProperty into;
    private final Property<Map> environmentConfig;
    private final EnvironmentConfigurationUtils environmentConfigurationUtils;
    private final ConfigurableFileCollection dependencyBundles;

    @Inject
    public BuildFullBundleTask() {
        into = newOutputDirectory();
        environmentConfig = getProject().getObjects().property(Map.class);
        environmentConfigurationUtils = getInstance(EnvironmentConfigurationUtils.class);
        dependencyBundles = getProject().files();
    }

    @OutputDirectory
    DirectoryProperty getInto() {
        return into;
    }

    @Input
    Property<Map> getEnvironmentConfig() {
        return environmentConfig;
    }

    @InputFiles
    public ConfigurableFileCollection getDependencyBundles() {
        return dependencyBundles;
    }

    @TaskAction
    public void perform() {
        final Map<String, String> environmentValues = environmentConfigurationUtils.parseEnvironmentValues(environmentConfig.getOrNull());

        final EnvironmentBundleCreator environmentBundleCreator = getInstance(EnvironmentBundleCreator.class);
        final String bundleFileName = getProject().getName() + '-' + getProject().getVersion() + "-full.bundle";
        environmentBundleCreator.createFullBundle(
                environmentValues,
                filterBundleFiles(dependencyBundles.getAsFileTree().getFiles()),
                into.getAsFile().get().getPath(),
                bundleFileName
        );
    }
}
