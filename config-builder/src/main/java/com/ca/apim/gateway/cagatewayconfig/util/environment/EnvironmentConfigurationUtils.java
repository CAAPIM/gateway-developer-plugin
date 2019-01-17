/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.environment;

import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.environment.MissingEnvironmentException;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.JSON;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

/**
 * Utility class to handle tasks related to environment properties
 */
@Singleton
public class EnvironmentConfigurationUtils {

    private final JsonTools jsonTools;
    private final EntityLoaderRegistry entityLoaderRegistry;

    @Inject
    EnvironmentConfigurationUtils(JsonTools jsonTools, EntityLoaderRegistry entityLoaderRegistry) {
        this.jsonTools = jsonTools;
        this.entityLoaderRegistry = entityLoaderRegistry;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> parseEnvironmentValues(Map providedEnvironmentValues) {
        final Map envConfig = ofNullable(providedEnvironmentValues).orElseThrow(() -> new MissingEnvironmentException("Environment configuration was not specified into gradle configuration file."));
        final Map<String, String> environmentValues = new HashMap<>();
        envConfig.entrySet().forEach((Consumer<Entry>) e -> environmentValues.put(PREFIX_ENV + e.getKey().toString(), getEnvValue(e.getKey().toString(), e.getValue())));

        return unmodifiableMap(environmentValues);
    }

    private String getEnvValue(String key, Object o) {
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof File) {
            File configFile = (File) o;
            if (!configFile.exists()) {
                throw new MissingEnvironmentException("Specified environment config file " + configFile.toString() + " does not exist.");
            }

            // get the entity type and name
            String entityType = key.substring(0, key.indexOf('.'));
            String entityName = key.substring(key.indexOf('.') + 1);

            // read a single file to the map of entities (or strings, if is a properties or certificate)
            EntityLoader loader = entityLoaderRegistry.getLoader(entityType);

            // find the entity value
            Object entity = loader.loadSingle(entityName, configFile);
            if (entity == null) {
                throw new MissingEnvironmentException("Specified environment config file " + configFile.toString() + " does not have the configuration for entity " + entityName + ", type " + entityType + ".");
            }

            // in case of properties or certificates will be the string value
            if (entity instanceof String) {
                return (String) entity;
            }

            // otherwise is a real entity so we jsonify it
            try {
                return jsonTools.getObjectWriter(JSON).writeValueAsString(entity);
            } catch (JsonProcessingException e) {
                throw new MissingEnvironmentException("Unable to read environment for specified configuration " + entityName, e);
            }
        }

        throw new MissingEnvironmentException("Unable to load environment from specified property: " + o.toString());
    }
}
