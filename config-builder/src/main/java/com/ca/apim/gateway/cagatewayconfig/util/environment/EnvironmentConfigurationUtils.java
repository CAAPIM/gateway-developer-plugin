/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.EntityTypeRegistry;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils;
import com.ca.apim.gateway.cagatewayconfig.beans.EnvironmentBundleData;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.environment.MissingEnvironmentException;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.PEM_CERT_FILE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_GATEWAY;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Utility class to handle tasks related to environment properties
 */
@Singleton
public class EnvironmentConfigurationUtils {

    private final JsonTools jsonTools;
    private final EntityLoaderRegistry entityLoaderRegistry;
    private final JsonFileUtils jsonFileUtils;
    private final EntityTypeRegistry entityTypeRegistry;

    @Inject
    EnvironmentConfigurationUtils(JsonTools jsonTools, EntityLoaderRegistry entityLoaderRegistry,
                                  JsonFileUtils jsonFileUtils, final EntityTypeRegistry entityTypeRegistry) {
        this.jsonTools = jsonTools;
        this.entityLoaderRegistry = entityLoaderRegistry;
        this.jsonFileUtils = jsonFileUtils;
        this.entityTypeRegistry = entityTypeRegistry;
    }

    /**
     * Parses the deployment bundle metadata file to extract environmental dependencies.
     *
     * @param metaDataFile deployment bundle metadata.
     * @param configFolder config folder to look for environmental files.
     * @return pair of bundle file name and environmental entities map.
     */
    public Pair<String, Map<String, String>> parseBundleMetadata(File metaDataFile, File configFolder) {
        if (!metaDataFile.exists()) {
            throw new MissingEnvironmentException("Metadata file " + metaDataFile.toString() + " does not exist.");
        }
        final EnvironmentBundleData environmentBundleData = jsonFileUtils.readBundleMetadataFile(metaDataFile, EnvironmentBundleData.class);
        if (environmentBundleData != null && environmentBundleData.getEnvironmentEntities() != null && !environmentBundleData.getEnvironmentEntities().isEmpty()) {
            final String bundleName = environmentBundleData.getName();
            final String bundleVersion = environmentBundleData.getVersion();
            final Map<String, String> environmentValues = new LinkedHashMap<>();
            final List<Map<String, String>> environmentEntities = environmentBundleData.getEnvironmentEntities();
            environmentEntities.stream().forEach(environmentEntitiy -> {
                String entityType = environmentEntitiy.get("type");
                String entityName = environmentEntitiy.get("name");
                if (EntityTypes.CLUSTER_PROPERTY_TYPE.equals(entityType)) {
                    entityName = PREFIX_GATEWAY + environmentEntitiy.get("name");
                    entityType = "ENVIRONMENT_PROPERTY";
                }

                Class<? extends GatewayEntity> entityClass = entityTypeRegistry.getEntityClass(entityType);
                final Pair<String, ConfigurationFile.FileType> configFileInfo = EntityUtils.getEntityConfigFileInfo(entityClass);
                final String environmentType = EntityUtils.getEntityEnvironmentType(entityClass);
                if (configFileInfo == null || environmentType == null) {
                    throw new MissingEnvironmentException("Unexpected entity type " + entityType);
                }

                final String configFileName = configFileInfo.getLeft() + "." + (configFileInfo.getRight().equals(ConfigurationFile.FileType.JSON_YAML)? YML_EXTENSION :
                        configFileInfo.getRight().name().toLowerCase());
                final File envConfigFile = new File(configFolder, configFileName);
                environmentValues.put(PREFIX_ENV + environmentType + "." + entityName,
                        loadConfigFromFile(envConfigFile, environmentType, entityName));

                if (EntityTypes.TRUSTED_CERT_TYPE.equals(entityType)) {
                    final File certDataFile = new File(configFolder + "/certificates", entityName + PEM_CERT_FILE_EXTENSION);
                    environmentValues.put(PREFIX_ENV + "CERTIFICATE_FILE" + "." + entityName + PEM_CERT_FILE_EXTENSION,
                            loadConfigFromFile(certDataFile, "CERTIFICATE_FILE", entityName));
                }

            });
            return ImmutablePair.of(bundleName + "-" + bundleVersion, unmodifiableMap(environmentValues));
        }

        return null;
    }

    @NotNull
    public String loadConfigFromFile(File configFile, String entityType, String entityName) {
        if (!configFile.exists()) {
            throw new MissingEnvironmentException("Environment config file " + configFile.toString() + " does not exist.");
        }

        // read a single file to the map of entities (or strings, if is a properties or certificate)
        EntityLoader loader = entityLoaderRegistry.getLoader(entityType);

        // find the entity value
        Object entity = loader.loadSingle(entityName, configFile);
        if (entity == null) {
            throw new MissingEnvironmentException("Environment config file " + configFile.toString() + " does not have the configuration for entity " + entityName + ", type " + entityType + ".");
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

    public static String tryInferContentTypeFromValue(final String value) {
        if (isBlank(value)) {
            return null;
        }

        // remove extra spaces and pick first char
        char initial = value.trim().charAt(0);
        switch (initial) {
            case '[':
            case '{': return JSON;
            case '<': throw new MissingEnvironmentException("XML Environment Values are not yet supported.");
            default: return YAML;
        }
    }
}
