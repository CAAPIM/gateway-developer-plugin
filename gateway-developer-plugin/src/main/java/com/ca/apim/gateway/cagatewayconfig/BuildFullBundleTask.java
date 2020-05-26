/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.environment.FullBundleCreator;
import com.ca.apim.gateway.cagatewayconfig.environment.MissingEnvironmentException;
import com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.ProjectDependencyUtils.filterBundleFiles;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.BUNDLE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.collectFiles;
import static com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry.getInstance;
import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.YML_EXTENSION;
import static org.apache.commons.collections4.ListUtils.union;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * The BuildFullBundleTask task will grab provided environment properties and build a single bundle merged with the deployment bundles.
 */
public class BuildFullBundleTask extends DefaultTask {

    private final EnvironmentConfigurationUtils environmentConfigurationUtils;
    private final Property<Map> environmentConfig;
    private final ConfigurableFileCollection dependencyBundles;
    private final DirectoryProperty into;
    private final Property<Boolean> detemplatizeDeploymentBundles;
    private final DirectoryProperty configFolder;
    private final Property<String> configName;

    @Inject
    public BuildFullBundleTask() {
        environmentConfigurationUtils = getInstance(EnvironmentConfigurationUtils.class);
        environmentConfig = getProject().getObjects().property(Map.class);
        dependencyBundles = getProject().files();
        into = newOutputDirectory();
        detemplatizeDeploymentBundles = getProject().getObjects().property(Boolean.class);
        configFolder = newInputDirectory();
        configName = getProject().getObjects().property(String.class);
    }

    @InputFiles
    ConfigurableFileCollection getDependencyBundles() {
        return dependencyBundles;
    }

    @OutputDirectory
    DirectoryProperty getInto() {
        return into;
    }

    @Input
    Property<Map> getEnvironmentConfig() {
        return environmentConfig;
    }

    @Input
    Property<Boolean> getDetemplatizeDeploymentBundles() {
        return detemplatizeDeploymentBundles;
    }

    @InputDirectory
    DirectoryProperty getConfigFolder() {
        return configFolder;
    }

    @Input
    Property<String> getConfigName() {
        return configName;
    }

    @TaskAction
    public void perform() {
        final FullBundleCreator fullBundleCreator = getInstance(FullBundleCreator.class);
        final String bundleDirectory = into.getAsFile().get().getPath();
        final List<File> metaDataFiles = collectFiles(bundleDirectory, YML_EXTENSION);
        if(metaDataFiles.isEmpty()) {
            //read environment properties from environmentConfig
            if (environmentConfig.get().isEmpty()) {
                throw new MissingEnvironmentException("Metadata file does not exist and environment configuration is not specified in the gradle configuration file.");
            }
            final Map<String, String> environmentValuesFromConfig = environmentConfigurationUtils.parseEnvironmentValues(environmentConfig.get());
            final String bundleFileName = getProject().getName() + '-' + getProject().getVersion() + "-full.install.bundle";
            final List<File> bundleFiles = union(
                    collectFiles(bundleDirectory, EMPTY + BUNDLE_EXTENSION),
                    filterBundleFiles(dependencyBundles.getAsFileTree().getFiles())
            );

            fullBundleCreator.createFullBundle(
                    environmentValuesFromConfig,
                    bundleFiles,
                    bundleDirectory,
                    bundleFileName,
                    detemplatizeDeploymentBundles.get()
            );
        }
        metaDataFiles.stream().forEach(metaDataFile-> {
            final Pair<String, Map<String, String>> bundleEnvironmentValues = environmentConfigurationUtils.parseBundleMetadata(metaDataFile, configFolder.getAsFile().get());
            if (null != bundleEnvironmentValues) {
                final String bundleFileName = bundleEnvironmentValues.getLeft() + "-full.install.bundle";
                //read environment properties from environmentConfig
                final Map<String, String> environmentValuesFromConfig = environmentConfigurationUtils.parseEnvironmentValues(environmentConfig.get());
                Map<String, String> environmentValuesFromMetadata = bundleEnvironmentValues.getRight();
                environmentValuesFromMetadata.putAll(environmentValuesFromConfig);
                final List<File> bundleFiles = union(
                        collectFiles(bundleDirectory, bundleEnvironmentValues.getLeft() + BUNDLE_EXTENSION),
                        filterBundleFiles(dependencyBundles.getAsFileTree().getFiles())
                );

                fullBundleCreator.createFullBundle(
                        environmentValuesFromMetadata,
                        bundleFiles,
                        bundleDirectory,
                        bundleFileName,
                        detemplatizeDeploymentBundles.get()
                );
            }
        });
    }
}
