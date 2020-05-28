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
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.ENV_INSTALL_BUNDLE_NAME_SUFFIX;
import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.collectFiles;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.removeAllSpecialChars;
import static com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry.getInstance;
import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.YML_EXTENSION;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * The BuildEnvironmentBundle task will grab provided environment properties and build a bundle.
 */
public class BuildEnvironmentBundleTask extends DefaultTask {



    private final DirectoryProperty into;
    private final EnvironmentConfigurationUtils environmentConfigurationUtils;
    private final DirectoryProperty configFolder;
    private final Property<String> configName;

    @Inject
    public BuildEnvironmentBundleTask() {
        into = newOutputDirectory();
        environmentConfigurationUtils = getInstance(EnvironmentConfigurationUtils.class);
        configFolder = newInputDirectory();
        configName = getProject().getObjects().property(String.class);
    }

    @OutputDirectory
    DirectoryProperty getInto() {
        return into;
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
        final EnvironmentBundleCreator environmentBundleCreator = getInstance(EnvironmentBundleCreator.class);
        final List<File> metaDataFiles = collectFiles(into.getAsFile().get().getPath(), YML_EXTENSION);
        if (metaDataFiles.isEmpty()) {
            throw new MissingEnvironmentException("Metadata file does not exist.");
        }

        metaDataFiles.stream().forEach(metaDataFile-> {
            final Pair<String, Map<String, String>> bundleEnvironmentValues = environmentConfigurationUtils.parseBundleMetadata(metaDataFile, configFolder.getAsFile().get());
            if (null != bundleEnvironmentValues) {
                final String envBundleFilename = getEnvBundleFileName(bundleEnvironmentValues.getLeft());
                environmentBundleCreator.createEnvironmentBundle(
                        bundleEnvironmentValues.getRight(),
                        into.getAsFile().get().getPath(),
                        into.getAsFile().get().getPath(),
                        EMPTY,
                        PLUGIN,
                        envBundleFilename, // Passing envBundleFilename
                        bundleEnvironmentValues.getLeft()
                );
            }
        });
    }

    private String getEnvBundleFileName(String deployBundleName) {
        if (configName != null && StringUtils.isNotBlank(configName.get())) {
            return deployBundleName + "-" + removeAllSpecialChars(configName.get()) + ENV_INSTALL_BUNDLE_NAME_SUFFIX;
        } else {
            String configFolderName = configFolder != null ? configFolder.getAsFile().get().getName() : "";
            if (StringUtils.equalsIgnoreCase(configFolderName, "config")) {
                return deployBundleName + "-" + ENV_INSTALL_BUNDLE_NAME_SUFFIX;
            } else {
                return deployBundleName + "-" + removeAllSpecialChars(configFolderName) + ENV_INSTALL_BUNDLE_NAME_SUFFIX;
            }
        }
    }
}
