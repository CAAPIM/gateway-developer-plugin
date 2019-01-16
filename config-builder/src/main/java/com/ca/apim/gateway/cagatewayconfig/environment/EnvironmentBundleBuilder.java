/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class EnvironmentBundleBuilder {

    private final EntityLoaderRegistry entityLoaderRegistry;

    @Inject
    EnvironmentBundleBuilder(EntityLoaderRegistry entityLoaderRegistry) {
        this.entityLoaderRegistry = entityLoaderRegistry;
    }

    void build(Bundle bundle, Map<String, String> environmentProperties) {
        environmentProperties.entrySet().stream().filter(e -> e.getKey().startsWith("ENV.")).forEach(e -> addEnvToBundle(bundle, e.getKey(), e.getValue()));
    }

    private void addEnvToBundle(Bundle bundle, String key, String value) {
        if (key.startsWith("ENV.")) {
            String environmentKey = key.substring(4);
            int typeEndIndex = environmentKey.indexOf('.');
            if(typeEndIndex != -1) {
                String type = environmentKey.substring(0, typeEndIndex);
                EntityLoader loader = entityLoaderRegistry.getLoader(type);
                if(loader != null) {
                    String name = environmentKey.substring(type.length() + 1);
                    loader.load(bundle, name, value);
                }
            }
        }
    }
}
