/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.GatewayEntityInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.databind.type.MapType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility methods for entity loading.
 */
public class EntityLoaderUtils {

    private EntityLoaderUtils() {}

    /**
     * Create a generic instance of entity loader that will be responsible for loading entity specified by GatewayEntityInfo metadata object.
     *
     * @param jsonTools the JsonTools instance
     * @param idGenerator the IdGenerator instance
     * @param entityInfo the entity info for this loader
     * @return one instance of entity loader.
     */
    @NotNull
    public static EntityLoader createEntityLoader(JsonTools jsonTools, IdGenerator idGenerator, GatewayEntityInfo entityInfo) {
        return new EntityLoaderBase<GatewayEntity>(jsonTools, idGenerator) {
            @Override
            public String getEntityType() {
                return entityInfo.getEnvironmentType();
            }

            @Override
            protected Class<GatewayEntity> getBeanClass() {
                return entityInfo.getEntityClass();
            }

            @Override
            protected String getFileName() {
           with     return entityInfo.getFileName();
            }

            @Override
            protected void putToBundle(Bundle bundle, @NotNull Map<String, GatewayEntity> entitiesMap) {
                bundle.getEntities(entityInfo.getEntityClass()).putAll(entitiesMap);
            }
        };
    }

    /**
     * Create a generic instance of properties loader that will be responsible for loading entity specified by GatewayEntityInfo metadata object.
     *
     * @param fileUtils the {@link FileUtils} instance
     * @param idGenerator the IdGenerator instance
     * @param entityInfo the entity info for this loader
     * @return one instance of properties entity loader.
     */
    @NotNull
    static PropertiesLoaderBase createPropertiesLoader(FileUtils fileUtils, IdGenerator idGenerator, GatewayEntityInfo entityInfo) {
        return new PropertiesLoaderBase(fileUtils, idGenerator) {

            @Override
            public String getEntityType() {
                return entityInfo.getEnvironmentType();
            }

            @Override
            protected String getFileName() {
                return entityInfo.getFileName() + ".properties";
            }

            @Override
            protected Class getEntityClass() {
                return entityInfo.getEntityClass();
            }
        };
    }

    /**
     * Load entities from a json/yaml file to a map of key-value entities by name.
     *
     * @param jsonTools {@link JsonTools} instance
     * @param entityClass Entity class
     * @param entitiesFile Entity file
     * @param <B> Entity type
     * @return map of name-entity
     */
    static <B> Map<String, B> loadEntitiesFromFile(JsonTools jsonTools, Class<? extends GatewayEntity> entityClass, File entitiesFile) {
        final String fileType = jsonTools.getTypeFromFile(entitiesFile);
        final MapType type = jsonTools.getObjectMapper(fileType).getTypeFactory().constructMapType(LinkedHashMap.class, String.class, entityClass);

        return jsonTools.readDocumentFile(entitiesFile, type);
    }
}
