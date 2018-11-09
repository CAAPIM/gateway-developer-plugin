/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

/**
 *
 */
@Singleton
public class EntityTypeRegistry {

    private static final GatewayEntityInfo NO_INFO = new GatewayEntityInfo();
    private final Map<String, GatewayEntityInfo> entityTypeMap;

    @Inject
    public EntityTypeRegistry(final Reflections reflections) {
        Map<String, GatewayEntityInfo> entityTypes = new HashMap<>();
        reflections.getSubTypesOf(GatewayEntity.class).forEach(e -> {
            String type = EntityUtils.getEntityType(e);
            if (type != null) {
                final Pair<String, FileType> configFileInfo = EntityUtils.getEntityConfigFileInfo(e);
                entityTypes.put(type, new GatewayEntityInfo(e, configFileInfo.getLeft(), configFileInfo.getRight()));
            }
        });

        this.entityTypeMap = unmodifiableMap(entityTypes);
    }

    public Class<? extends GatewayEntity> getEntityClass(String entityType) {
        return ofNullable(entityTypeMap.get(entityType)).orElse(NO_INFO).entityClass;
    }

    public Map<String, GatewayEntityInfo> getEntityTypeMap() {
        return entityTypeMap;
    }

    public static class GatewayEntityInfo {

        private Class<? extends GatewayEntity> entityClass;
        private String fileName;
        private FileType fileType;

        private GatewayEntityInfo() {
        }

        private GatewayEntityInfo(Class<? extends GatewayEntity> entityClass, String fileName, FileType fileType) {
            this.entityClass = entityClass;
            this.fileName = fileName;
            this.fileType = fileType;
        }

        @SuppressWarnings("unchecked")
        public Class<GatewayEntity> getEntityClass() {
            return (Class<GatewayEntity>) entityClass;
        }

        public String getFileName() {
            return fileName;
        }

        public FileType getFileType() {
            return fileType;
        }
    }
}
