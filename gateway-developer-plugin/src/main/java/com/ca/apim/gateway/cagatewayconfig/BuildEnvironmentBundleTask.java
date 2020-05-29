/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreator;
import com.ca.apim.gateway.cagatewayconfig.environment.MissingEnvironmentException;
import com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.INSTALL_BUNDLE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.collectFiles;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.removeAllSpecialChars;
import static com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry.getInstance;
import static com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils.METADATA_FILE_NAME_SUFFIX;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * The BuildEnvironmentBundle task will grab provided environment properties and build a bundle.
 */
public class BuildEnvironmentBundleTask extends DefaultTask {

    private final DirectoryProperty into;
    private final Property<Map> environmentConfig;
    private final EnvironmentConfigurationUtils environmentConfigurationUtils;
    private final DirectoryProperty configFolder;
    private final Property<String> configName;

    @Inject
    public BuildEnvironmentBundleTask() {
        into = newOutputDirectory();
        environmentConfig = getProject().getObjects().property(Map.class);
        environmentConfigurationUtils = getInstance(EnvironmentConfigurationUtils.class);
        configFolder = newInputDirectory();
        configName = getProject().getObjects().property(String.class);
    }

    @OutputDirectory
    DirectoryProperty getInto() {
        return into;
    }

    @Input
    @Optional
    Property<Map> getEnvironmentConfig() {
        return environmentConfig;
    }

    @InputDirectory
    @Optional
    DirectoryProperty getConfigFolder() {
        return configFolder;
    }

    @Input
    @Optional
    Property<String> getConfigName() {
        return configName;
    }

    @TaskAction
    public void perform() {
        final EnvironmentBundleCreator environmentBundleCreator = getInstance(EnvironmentBundleCreator.class);
        final List<File> metaDataFiles = collectFiles(into.getAsFile().get().getPath(), METADATA_FILE_NAME_SUFFIX);
        if (metaDataFiles.isEmpty()) {
            throw new MissingEnvironmentException("Metadata file does not exist.");
        }

        metaDataFiles.stream().forEach(metaDataFile -> {
            final Pair<String, Map<String, String>> bundleEnvironmentValues = environmentConfigurationUtils.parseBundleMetadata(metaDataFile, configFolder.getAsFile().getOrNull());
            if (null != bundleEnvironmentValues) {
                final String envBundleFileName = getEnvBundleFilename(bundleEnvironmentValues.getLeft());
                Map<String, String> environmentValuesFromMetadata = bundleEnvironmentValues.getRight();
                //read environment properties from environmentConfig and merge it with metadata properties
                environmentValuesFromMetadata.putAll(environmentConfigurationUtils.parseEnvironmentValues(environmentConfig.get()));
                environmentBundleCreator.createEnvironmentBundle(
                        environmentValuesFromMetadata,
                        into.getAsFile().get().getPath(),
                        into.getAsFile().get().getPath(),
                        EMPTY,
                        PLUGIN,
                        envBundleFileName, // Passing envBundleFileName
                        bundleEnvironmentValues.getLeft()
                );
            }
        });
    }

    private String getEnvBundleFilename(String deployBundleName) {
        final String extension = "env" + INSTALL_BUNDLE_EXTENSION;
        if (configName != null && StringUtils.isNotBlank(configName.get())) {
            return deployBundleName + "-" + removeAllSpecialChars(configName.get()) + extension;
        } else {
            String configFolderName = configFolder != null && configFolder.isPresent()?
                    configFolder.getAsFile().get().getName() : "";
            if (StringUtils.equalsIgnoreCase(configFolderName, "config")) {
                return deployBundleName + "-" + extension;
            } else {
                return deployBundleName + "-" + removeAllSpecialChars(configFolderName) + extension;
            }
        }
    }
}
