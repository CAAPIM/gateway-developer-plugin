/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreator;
import com.ca.apim.gateway.cagatewayconfig.environment.MissingEnvironmentException;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreationMode.PLUGIN;
import static com.ca.apim.gateway.cagatewayconfig.util.injection.ConfigBuilderModule.getInstance;
import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.JSON;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static java.util.Optional.ofNullable;

/**
 * The BuildEnvironmentBundle task will grab provided environment properties and build a bundle.
 */
public class BuildEnvironmentBundleTask extends DefaultTask {

    private final DirectoryProperty into;
    private final Property<Map> environmentConfig;
    private final JsonTools jsonTools;
    private final EntityLoaderRegistry entityLoaderRegistry;

    @Inject
    public BuildEnvironmentBundleTask() {
        into = newOutputDirectory();
        environmentConfig = getProject().getObjects().property(Map.class);
        jsonTools = getInstance(JsonTools.class);
        entityLoaderRegistry = getInstance(EntityLoaderRegistry.class);
    }

    @OutputDirectory
    DirectoryProperty getInto() {
        return into;
    }

    @Input
    Property<Map> getEnvironmentConfig() {
        return environmentConfig;
    }

    @TaskAction
    public void perform() {
        final Map envConfig = ofNullable(environmentConfig.getOrNull()).orElseThrow(() -> new MissingEnvironmentException("Environment configuration was not specified into gradle configuration file."));
        final Map<String, String> environmentValues = new HashMap<>();
        envConfig.entrySet().forEach((Consumer<Entry>) e -> environmentValues.put(PREFIX_ENV + e.getKey().toString(), getEnvValue(e.getKey().toString(), e.getValue())));

        final EnvironmentBundleCreator environmentBundleCreator = getInstance(EnvironmentBundleCreator.class);
        final String bundleFileName = getProject().getName() + '-' + getProject().getVersion() + "-environment.bundle";
        environmentBundleCreator.createEnvironmentBundle(
                environmentValues,
                into.getAsFile().get().getPath(),
                into.getAsFile().get().getPath(),
                PLUGIN,
                bundleFileName
        );
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
