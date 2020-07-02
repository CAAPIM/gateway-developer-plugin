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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.*;
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
    private final EnvironmentConfigurationUtils environmentConfigurationUtils;
    private final DirectoryProperty configFolder;
    private final Property<String> configName;
    private final Property<Map> environmentConfig;
    private final Property<Map> envConfig;

    @Inject
    public BuildEnvironmentBundleTask() {
        into = newOutputDirectory();
        environmentConfig = getProject().getObjects().property(Map.class);
        envConfig = getProject().getObjects().property(Map.class);
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

    @Input
    @Optional
    Property<Map> getEnvConfig() {
        return envConfig;
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
        File configuredFolder = configFolder.getAsFile().getOrNull();
        Map environmentEntities = java.util.Optional.ofNullable(envConfig.getOrNull()).orElse(environmentConfig.getOrNull());
        if (configuredFolder == null && environmentEntities == null) {
            throw new MissingEnvironmentException("EnvironmentConfig is not configured");
        }
        Map<String, String> bundleEnvironmentValues = new HashMap<>();
        if(configuredFolder != null){
            bundleEnvironmentValues = environmentConfigurationUtils.loadConfigFolder(configuredFolder);
        }
        final String configurationName = configName != null ? removeAllSpecialChars(configName.get()) : EMPTY;
        final String envBundleFileName = getEnvBundleFilename(getProject().getName(), getProject().getVersion().toString(), configurationName);

        //read environment properties from environmentConfig and merge it with config folder entities
        if(environmentEntities != null) {
            bundleEnvironmentValues.putAll(environmentConfigurationUtils.parseEnvironmentValues(environmentEntities));
        }

        ProjectInfo projectInfo = new ProjectInfo(getProject().getName(), getProject().getGroup().toString(), getProject().getVersion().toString(), configurationName);
        environmentBundleCreator.createEnvironmentBundle(
                bundleEnvironmentValues,
                into.getAsFile().get().getPath(),
                into.getAsFile().get().getPath(),
                configuredFolder != null ? configuredFolder.getPath() : EMPTY,
                PLUGIN,
                envBundleFileName, // Passing envBundleFileName
                projectInfo
        );
    }

    /**
     * Generates the Environment install bundle filename in the format <module name>-environment-<version>-<configName>.install.bundle.
     *
     * Here <configName> is generated with the following preference:
     * 1) If "name" is provided in the EnvironmentConfig {} in build.gradle, "name" will be used after
     * removing all the special characters. For eg. if "name" is set "default", filename will have "-default"
     *
     * @param moduleName project module name
     * @param version project module version
     * @param configurationName name in EnvironmentConfig
     * @return Environment install bundle filename
     */
    private String getEnvBundleFilename(String moduleName, String version, String configurationName) {
        if (StringUtils.isNotBlank(configurationName)) {
            return moduleName + "-" + PREFIX_ENVIRONMENT + "-" + version + "-" + removeAllSpecialChars(configurationName) + INSTALL_BUNDLE_EXTENSION;
        } else {
            return moduleName + "-" + PREFIX_ENVIRONMENT + "-" + version + INSTALL_BUNDLE_EXTENSION;
        }
    }
}
