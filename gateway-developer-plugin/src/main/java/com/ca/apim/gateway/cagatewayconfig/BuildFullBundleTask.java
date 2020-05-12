/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.environment.FullBundleCreator;
import com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.ProjectDependencyUtils.filterBundleFiles;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.BUNDLE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.DELETE_BUNDLE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.collectFiles;
import static com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry.getInstance;
import static org.apache.commons.collections4.ListUtils.union;

/**
 * The BuildFullBundleTask task will grab provided environment properties and build a single bundle merged with the deployment bundles.
 */
public class BuildFullBundleTask extends DefaultTask {

    private final Property<Map> environmentConfig;
    private final EnvironmentConfigurationUtils environmentConfigurationUtils;
    private final ConfigurableFileCollection dependencyBundles;
    private final RegularFileProperty outputBundle;
    private final Property<Boolean> detemplatizeDeploymentBundles;

    @Inject
    public BuildFullBundleTask() {
        environmentConfig = getProject().getObjects().property(Map.class);
        environmentConfigurationUtils = getInstance(EnvironmentConfigurationUtils.class);
        dependencyBundles = getProject().files();
        outputBundle = newOutputFile();
        detemplatizeDeploymentBundles = getProject().getObjects().property(Boolean.class);
    }

    @Input
    Property<Map> getEnvironmentConfig() {
        return environmentConfig;
    }

    @InputFiles
    ConfigurableFileCollection getDependencyBundles() {
        return dependencyBundles;
    }

    @OutputFile
    RegularFileProperty getOutputBundle() {
        return outputBundle;
    }

    @Input
    Property<Boolean> getDetemplatizeDeploymentBundles() {
        return detemplatizeDeploymentBundles;
    }

    @TaskAction
    public void perform() {
        final Map<String, String> environmentValues = environmentConfigurationUtils.parseEnvironmentValues(environmentConfig.getOrNull());
        final String bundleDirectory = outputBundle.getAsFile().get().getParentFile().getPath();
        final List<File> bundleFiles = union(
                collectBundleFiles(bundleDirectory),
                filterBundleFiles(dependencyBundles.getAsFileTree().getFiles())
        );

        final FullBundleCreator fullBundleCreator = getInstance(FullBundleCreator.class);
        fullBundleCreator.createFullBundle(
                environmentValues,
                bundleFiles,
                bundleDirectory,
                outputBundle.getAsFile().get().getName(),
                detemplatizeDeploymentBundles.get()
        );
    }

    private List<File> collectBundleFiles(final String bundleDirectory) {
        return collectFiles(bundleDirectory, BUNDLE_EXTENSION).stream().filter(file -> !StringUtils.endsWithIgnoreCase(
                file.getName(), DELETE_BUNDLE_EXTENSION)).collect(Collectors.toList());
    }
}
