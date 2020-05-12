/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.EnvironmentBundleData;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.environment.MissingEnvironmentException;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.PEM_CERT_FILE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_GATEWAY;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Utility class to handle tasks related to environment properties
 */
@Singleton
public class EnvironmentConfigurationUtils {

    private final JsonTools jsonTools;
    private final EntityLoaderRegistry entityLoaderRegistry;
    private static final Map<String, Pair<String,String>> ENTITY_FILE_MAP = unmodifiableMap(createEntityFileMap());

    @Inject
    EnvironmentConfigurationUtils(JsonTools jsonTools, EntityLoaderRegistry entityLoaderRegistry) {
        this.jsonTools = jsonTools;
        this.entityLoaderRegistry = entityLoaderRegistry;
    }

    /**
     * Creates the Map of entity names with their environment config file names.
     *
     * @return entity names with config file names.
     */
    private static Map<String, Pair<String,String>> createEntityFileMap() {
        Map<String, Pair<String,String>> entityFileMap = new HashMap<>();
        entityFileMap.put(EntityTypes.ID_PROVIDER_CONFIG_TYPE, ImmutablePair.of("IDENTITY_PROVIDER", "identity-providers.yml"));
        entityFileMap.put(EntityTypes.JDBC_CONNECTION, ImmutablePair.of("JDBC_CONNECTION","jdbc-connections.yml"));
        entityFileMap.put(EntityTypes.TRUSTED_CERT_TYPE, ImmutablePair.of("CERTIFICATE","trusted-certs.yml"));
        entityFileMap.put(EntityTypes.PRIVATE_KEY_TYPE, ImmutablePair.of("PRIVATE_KEY","private-keys.yml"));
        entityFileMap.put(EntityTypes.CASSANDRA_CONNECTION_TYPE, ImmutablePair.of("CASSANDRA_CONNECTION","cassandra-connections.yml"));
        entityFileMap.put(EntityTypes.JMS_DESTINATION_TYPE, ImmutablePair.of("JMS_DESTINATION","jms-destinations.yml"));
        entityFileMap.put(EntityTypes.SCHEDULED_TASK_TYPE, ImmutablePair.of("SCHEDULED_TASK", "scheduled-tasks.yml"));
        entityFileMap.put(EntityTypes.LISTEN_PORT_TYPE, ImmutablePair.of("LISTEN_PORT","listen-ports.yml"));
        entityFileMap.put(EntityTypes.STORED_PASSWORD_TYPE, ImmutablePair.of("PASSWORD","stored-passwords.properties"));
        entityFileMap.put(EntityTypes.CLUSTER_PROPERTY_TYPE, ImmutablePair.of("PROPERTY","global-env.properties"));

        return entityFileMap;
    }

    /**
     * Parses the deployment bundle metadata file to extract environmental dependencies.
     *
     * @param metaDataFile deployment bundle metadata
     * @param configFolder config folder path to look for environmental files.
     * @return pair of bundle file name and environmental entities map.
     */
    public Pair<String, Map<String, String>> parseBundleMetadata(File metaDataFile, String configFolder) {
        if (!metaDataFile.exists()) {
            throw new MissingEnvironmentException("Metadata file " + metaDataFile.toString() + " does not exist.");
        }

        final String envConfigPath = new File(configFolder).getAbsolutePath();
        final EnvironmentBundleData environmentBundleData;
        try {
            final ObjectMapper objectMapper = jsonTools.getObjectMapper(YAML_EXTENSION);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            environmentBundleData = objectMapper.readValue(metaDataFile, EnvironmentBundleData.class);
        } catch (IOException e) {
            throw new MissingEnvironmentException("Unable to parse the data from metadata file " + metaDataFile.toString(), e);
        }

        if (environmentBundleData != null && environmentBundleData.getEnvironmentEntities() != null && !environmentBundleData.getEnvironmentEntities().isEmpty()) {
            final String bundleName = environmentBundleData.getName();
            final String bundleVersion = environmentBundleData.getVersion();
            final Map<String, String> environmentValues = new LinkedHashMap<>();
            final List<LinkedHashMap<String, String>> environmentEntities = environmentBundleData.getEnvironmentEntities();
            environmentEntities.stream().forEach(environmentEntitiy -> {
                final String entityType = environmentEntitiy.get("type");
                final String entityName = !EntityTypes.CLUSTER_PROPERTY_TYPE.equals(entityType) ? environmentEntitiy.get("name") : PREFIX_GATEWAY + environmentEntitiy.get("name");
                final Pair<String, String> entityFilePair = ENTITY_FILE_MAP.get(entityType);
                if (null == entityFilePair) {
                    throw new MissingEnvironmentException("Unexpected entity type " + entityType);
                }

                final File envConfigFile = new File(envConfigPath, entityFilePair.getRight());
                environmentValues.put(PREFIX_ENV + entityFilePair.getLeft() + "." + entityName,
                        loadConfigFromFile(envConfigFile, entityFilePair.getLeft(), entityName));

                if (EntityTypes.TRUSTED_CERT_TYPE.equals(entityType)) {
                    final File certDataFile = new File(envConfigPath + "/certificates", entityName + PEM_CERT_FILE_EXTENSION);
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
