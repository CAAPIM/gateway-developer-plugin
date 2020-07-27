/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.environment;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.*;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.PREFIX_ENVIRONMENT;
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

    private static final Logger LOGGER = Logger.getLogger(EnvironmentConfigurationUtils.class.getName());
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

    @SuppressWarnings("unchecked")
    public Map<String, String> parseEnvironmentValues(Map providedEnvironmentValues) {
        final Map<String, String> environmentValues = new HashMap<>();
        providedEnvironmentValues.entrySet().forEach((Consumer<Entry>) e -> {
            int index = e.getKey().toString().indexOf('.');
            if (index != -1) {
                environmentValues.put(PREFIX_ENV + e.getKey().toString(), getEnvValue(e.getKey().toString(), e.getValue()));
            } else {
                final String entityType = (String) e.getKey();
                final Object object = e.getValue();
                if (!(object instanceof File)) {
                    throw new MissingEnvironmentException("Unable to load environment from specified property '" + e.getKey().toString() + "' due to unsupported value, it has to be a file");
                }
                Map<String, String> entities = loadConfigFromFile((File) object, entityType);
                entities.entrySet().forEach(entry -> environmentValues.put(PREFIX_ENV + entityType + "." + entry.getKey(), entry.getValue()));
            }
        });

        return unmodifiableMap(environmentValues);
    }

    private String getEnvValue(String key, Object o) {
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof File) {
            // get the entity type and name
            String entityType = key.substring(0, key.indexOf('.'));
            String entityName = key.substring(key.indexOf('.') + 1);
            return loadConfigFromFile((File) o, entityType, entityName);

        }

        throw new MissingEnvironmentException("Unable to load environment from specified property '" + o.toString() + "' due to unsupported value, it has to be a text content or a file");
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
        if (environmentBundleData != null && environmentBundleData.getReferencedEntities() != null) {
            String bundleName = environmentBundleData.getName();
            final String bundleVersion = environmentBundleData.getVersion();
            final Map<String, String> environmentValues = new LinkedHashMap<>();
            if (configFolder != null) {
                final List<Map<String, String>> environmentEntities = environmentBundleData.getReferencedEntities();
                environmentEntities.stream().forEach(environmentEntitiy -> {
                    String entityType = environmentEntitiy.get("type");
                    String entityName = environmentEntitiy.get("name");
                    entityName = EnvironmentConfigurationUtils.extractEntityName(entityName);
                    if (EntityTypes.CLUSTER_PROPERTY_TYPE.equals(entityType)) {
                        entityName = PREFIX_GATEWAY + environmentEntitiy.get("name");
                        entityType = "ENVIRONMENT_PROPERTY";
                    }

                    Class<? extends GatewayEntity> entityClass = entityTypeRegistry.getEntityClass(entityType);
                    entityClass = entityClass == null ? UnsupportedGatewayEntity.class : entityClass;
                    final Pair<String, ConfigurationFile.FileType> configFileInfo = EntityUtils.getEntityConfigFileInfo(entityClass);
                    final String environmentType = EntityUtils.getEntityEnvironmentType(entityClass);
                    if (configFileInfo == null || environmentType == null) {
                        throw new MissingEnvironmentException("Unexpected entity type " + entityType);
                    }
                    final String configFileName = configFileInfo.getLeft() + "." + (configFileInfo.getRight().equals(ConfigurationFile.FileType.JSON_YAML) ? YML_EXTENSION :
                            configFileInfo.getRight().name().toLowerCase());
                    final File envConfigFile = new File(configFolder, configFileName);
                    if (envConfigFile.exists()) {
                        try {
                            environmentValues.put(PREFIX_ENV + environmentType + "." + entityName,
                                    loadConfigFromFile(envConfigFile, environmentType, entityName));

                            if (EntityTypes.TRUSTED_CERT_TYPE.equals(entityType)) {
                                final File certDataFile = new File(configFolder + "/certificates", entityName + PEM_CERT_FILE_EXTENSION);
                                environmentValues.put(PREFIX_ENV + "CERTIFICATE_FILE" + "." + entityName + PEM_CERT_FILE_EXTENSION,
                                        loadConfigFromFile(certDataFile, "CERTIFICATE_FILE", entityName));
                            }
                        } catch (MissingEnvironmentException ex) {
                            LOGGER.log(Level.INFO, "could not find dependent environment entity in the configured folder " + entityName);
                        }
                    }
                });
            }

            bundleName = StringUtils.isBlank(bundleVersion) ? bundleName : bundleName + "-" + bundleVersion;
            return ImmutablePair.of(bundleName, environmentValues);
        }

        return null;
    }

    public Map<String, String> loadConfigFolder(final File configFolder) {
        final Map<String, String> environmentValues = new LinkedHashMap<>();
        final Map<String, EntityUtils.GatewayEntityInfo> entityInfoMap = entityTypeRegistry.getEnvironmentEntityTypes();
        entityInfoMap.entrySet().forEach(entityInfoEntry -> {
            final String entityType;
            if (EntityTypes.CLUSTER_PROPERTY_TYPE.equals(entityInfoEntry.getKey())) {
                entityType = "ENVIRONMENT_PROPERTY";
            } else {
                entityType = entityInfoEntry.getKey();
            }

            Class<? extends GatewayEntity> entityClass = entityInfoEntry.getValue().getEntityClass();
            final Pair<String, ConfigurationFile.FileType> configFileInfo = EntityUtils.getEntityConfigFileInfo(entityClass);
            final String environmentType = EntityUtils.getEntityEnvironmentType(entityClass);
            if (configFileInfo != null && environmentType != null) {
                final String configFileName = configFileInfo.getLeft() + "." + (configFileInfo.getRight().equals(ConfigurationFile.FileType.JSON_YAML) ? YML_EXTENSION :
                        configFileInfo.getRight().name().toLowerCase());
                final File envConfigFile = new File(configFolder, configFileName);
                if (envConfigFile.exists()) {
                    Map<String, String> entities = loadConfigFromFile(envConfigFile, environmentType);
                    entities.entrySet().forEach(entry -> {
                        environmentValues.put(PREFIX_ENV + environmentType + "." + entry.getKey(), entry.getValue());
                        if (EntityTypes.TRUSTED_CERT_TYPE.equals(entityType)) {
                            final File certDataFile = new File(configFolder + "/certificates", entry.getKey() + PEM_CERT_FILE_EXTENSION);
                            environmentValues.put(PREFIX_ENV + "CERTIFICATE_FILE" + "." + entry.getKey() + PEM_CERT_FILE_EXTENSION,
                                    loadConfigFromFile(certDataFile, "CERTIFICATE_FILE", entry.getKey()));
                        }
                    });
                }
            }
        });
        return environmentValues;
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

    /**
     *  This method reads all the entities of a given Environment entity type from the config file
     * @param configFile File
     * @param entityType String
     * @return Map
     */
    @NotNull
    public Map<String, String> loadConfigFromFile(File configFile, String entityType) {
        if (!configFile.exists()) {
            throw new MissingEnvironmentException("Environment config file " + configFile.toString() + " does not exist.");
        }

        // read a single file to the map of entities (or strings, if is a properties or certificate)
        EntityLoader loader = entityLoaderRegistry.getLoader(entityType);

        // find the entity value
        Map<String, Object> entityMap = loader.load(configFile);
        Map<String, String> entities = new HashMap<>();
        entityMap.entrySet().forEach(e -> {
            // in case of properties or certificates will be the string value
            if (e.getValue() instanceof String) {
                entities.put(e.getKey(), (String) e.getValue());
            }

            // otherwise is a real entity so we jsonify it
            try {
                entities.put(e.getKey(), jsonTools.getObjectWriter(JSON).writeValueAsString(e.getValue()));
            } catch (JsonProcessingException ex) {
                throw new MissingEnvironmentException("Unable to read environment for specified configuration " + e.getKey(), ex);
            }
        });
        return entities;
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

    /**
     * create dependent environment bundle from project info.
     * @param projectInfo
     * @return DependentBundle
     */
    public static DependentBundle generateDependentEnvBundleFromProject(final ProjectInfo projectInfo) {
        DependentBundle envBundle = new DependentBundle();
        envBundle.setGroupName(projectInfo.getGroupName());
        envBundle.setName(projectInfo.getName() + "-" + PREFIX_ENVIRONMENT);
        String version = StringUtils.isNotBlank(projectInfo.getVersion()) ? projectInfo.getMajorVersion() + "." + projectInfo.getMinorVersion() : "";
        envBundle.setVersion(version);
        envBundle.setType("bundle");
        return envBundle;
    }

    public static String extractEntityName(final String entityName) {
        String originalName = entityName;
        final String[] originalNameArray = entityName.split("::");
        if (originalNameArray.length > 2) {
            originalName = originalNameArray[2];
        }
        return originalName;
    }
}
