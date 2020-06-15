/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Collection;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.APPLICATION;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;

/**
 * Collect values from provided environment variables/files and generates a restman bundle with those values.
 * Each value is an entity that when loaded outputs to a bundle xml element.
 */
@Singleton
@SuppressWarnings("squid:S2083") // This warn relates to path injection attacks - however, paths here are never changed by end users and all self contained into docker containers.
public class EnvironmentBundleBuilder {

    private static final String FILE_PREFIX = "FILE.";

    private final EntityLoaderRegistry entityLoaderRegistry;
    private final EnvironmentConfigurationUtils environmentConfigurationUtils;

    @Inject
    EnvironmentBundleBuilder(EntityLoaderRegistry entityLoaderRegistry, EnvironmentConfigurationUtils environmentConfigurationUtils) {
        this.entityLoaderRegistry = entityLoaderRegistry;
        this.environmentConfigurationUtils = environmentConfigurationUtils;
    }

    void build(Bundle bundle, Map<String, String> environmentProperties, String environmentConfigurationFolderPath, EnvironmentBundleCreationMode mode) {
        environmentProperties.entrySet().stream().filter(e -> e.getKey().startsWith(PREFIX_ENV)).forEach(e -> addEnvToBundle(bundle, e.getKey(), e.getValue(), environmentConfigurationFolderPath));
        // only when running from environment creator application we try to load from directory.
        if (mode != APPLICATION) {
            return;
        }

        File envDir = new File(environmentConfigurationFolderPath);
        if (envDir.exists()) {
            // Load the entities to build bundle
            final Collection<EntityLoader> entityLoaders = entityLoaderRegistry.getEntityLoaders();
            entityLoaders.parallelStream().forEach(e -> e.load(bundle, envDir));
        }
    }

    private void addEnvToBundle(Bundle bundle, String key, String value, String environmentConfigurationFolderPath) {
        if (!key.startsWith(PREFIX_ENV)) {
            return;
        }

        String environmentKey = key.substring(4);
        // check if var is starting with file prefix - then remove if true
        boolean isFile = environmentKey.startsWith(FILE_PREFIX);
        if (isFile) {
            environmentKey = environmentKey.substring(5);
        }

        int typeEndIndex = environmentKey.indexOf('.');
        if (typeEndIndex == -1) {
            return;
        }

        String type = environmentKey.substring(0, typeEndIndex);
        EntityLoader loader = entityLoaderRegistry.getLoader(type);
        if (loader == null) {
            return;
        }

        String name = environmentKey.substring(type.length() + 1);
        // if we have a file, replace the value contents with the content load from the file in JSON specification.
        if (isFile) {
            // load the file
            value = environmentConfigurationUtils.loadConfigFromFile(new File(value), type, name);
        }

        // then load it
        loader.load(bundle, name, value, environmentConfigurationFolderPath);
    }

}
