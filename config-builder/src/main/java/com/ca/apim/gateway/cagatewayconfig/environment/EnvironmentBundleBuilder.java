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
import java.util.Map;

@Singleton
public class EnvironmentBundleBuilder {

    private static final String ENVIRONMENT_FILES_CONFIG_PATH = "/opt/SecureSpan/Gateway/node/default/etc/bootstrap/env";
    private static final String ENV_PREFIX = "ENV.";
    private static final String FILE_PREFIX = "FILE.";

    private final EntityLoaderRegistry entityLoaderRegistry;
    private final EnvironmentConfigurationUtils environmentConfigurationUtils;

    @Inject
    EnvironmentBundleBuilder(EntityLoaderRegistry entityLoaderRegistry, EnvironmentConfigurationUtils environmentConfigurationUtils) {
        this.entityLoaderRegistry = entityLoaderRegistry;
        this.environmentConfigurationUtils = environmentConfigurationUtils;
    }

    void build(Bundle bundle, Map<String, String> environmentProperties) {
        environmentProperties.entrySet().stream().filter(e -> e.getKey().startsWith("ENV.")).forEach(e -> addEnvToBundle(bundle, e.getKey(), e.getValue()));
    }

    private void addEnvToBundle(Bundle bundle, String key, String value) {
        if (!key.startsWith(ENV_PREFIX)) {
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
        if (isFile) {
            // if we have a file, replace the value contents with the content load from the file in JSON specification.
            value = environmentConfigurationUtils.loadConfigFromFile(new File(ENVIRONMENT_FILES_CONFIG_PATH, value), type, name);
        }

        // then load it
        loader.load(bundle, name, value);
    }
}
