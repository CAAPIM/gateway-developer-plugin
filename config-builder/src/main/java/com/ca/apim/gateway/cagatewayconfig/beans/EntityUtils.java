/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.BundleGeneration;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Named;

import static org.apache.commons.lang3.tuple.ImmutablePair.nullPair;

public class EntityUtils {

    public static final GatewayEntityInfo NO_INFO = new GatewayEntityInfo();

    private EntityUtils() {
        //
    }

    /**
     * Create EntityInfo object for the specified entity.
     *
     * @param entityClass entity class
     * @param <E> entity type
     * @return info object
     */
    public static <E extends GatewayEntity> GatewayEntityInfo createEntityInfo(Class<E> entityClass) {
        String type = getEntityType(entityClass);
        if (type != null) {
            final Pair<String, FileType> configFileInfo = getEntityConfigFileInfo(entityClass);
            return new GatewayEntityInfo(type, entityClass, configFileInfo.getLeft(), configFileInfo.getRight(), getEntityEnvironmentType(entityClass));
        }
        return null;
    }

    /**
     * @param entityClass entity class
     * @param <E> entity type
     * @return the entity type defined by annotation {@link Named} on each entity class
     */
    public static <E extends GatewayEntity> String getEntityType(Class<E> entityClass) {
        Named named = entityClass.getAnnotation(Named.class);
        return named != null ? named.value() : null;
    }

    /**
     * @param entityClass entity class
     * @param <E> entity type
     * @return a pair containing the file name and type for the config file of this entity or an empty pair if not configured
     */
    static <E extends GatewayEntity> Pair<String, FileType> getEntityConfigFileInfo(Class<E> entityClass) {
        ConfigurationFile configurationFile = entityClass.getAnnotation(ConfigurationFile.class);
        return configurationFile != null ? ImmutablePair.of(configurationFile.name(), configurationFile.type()) : nullPair();
    }

    /**
     * @param entityClass entity class
     * @param <E> entity type
     * @return the environment type of the entity configured by {@link EnvironmentType} annotation, otherwise null if not configured
     */
    static <E extends GatewayEntity> String getEntityEnvironmentType(Class<E> entityClass) {
        EnvironmentType environmentType = entityClass.getAnnotation(EnvironmentType.class);
        return environmentType != null ? environmentType.value() : null;
    }

    public static class GatewayEntityInfo {

        private String type;
        private Class<? extends GatewayEntity> entityClass;
        private String fileName;
        private FileType fileType;
        private String environmentType;

        private boolean bundleGenerationSupported;

        private GatewayEntityInfo() {
        }

        private GatewayEntityInfo(String type, Class<? extends GatewayEntity> entityClass, String fileName, FileType fileType, String environmentType) {
            this.type = type;
            this.entityClass = entityClass;
            this.fileName = fileName;
            this.fileType = fileType;
            this.environmentType = environmentType;
        }

        public String getType() {
            return type;
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

        public String getEnvironmentType() {
            return environmentType;
        }

        public boolean isBundleGenerationSupported() {
            BundleGeneration bundleGeneration = entityClass.getAnnotation(BundleGeneration.class);
            return bundleGeneration != null;
        }

    }
}
