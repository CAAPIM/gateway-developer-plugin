/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.PropertiesEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Base loader for all entities stored into properties files.
 *
 * @param <P> type of entity.
 */
public abstract class PropertiesLoaderBase<P extends PropertiesEntity> implements EntityLoader {

    private FileUtils fileUtils;
    private final IdGenerator idGenerator;

    PropertiesLoaderBase(final FileUtils fileUtils, final IdGenerator idGenerator) {
        this.fileUtils = fileUtils;
        this.idGenerator = idGenerator;
    }

    private Map<String, Object> load(final File propertiesFile) {
        Map<String, Object> propertiesMap = new LinkedHashMap<>();
        if (propertiesFile != null && propertiesFile.exists()) {
            Properties properties = new Properties();
            try (InputStream inStream = fileUtils.getInputStream(propertiesFile)) {
                properties.load(inStream);
            } catch (IOException e) {
                throw new ConfigLoadException("Could not load properties file (" + propertiesFile + "): " + e.getMessage(), e);
            }
            properties.forEach((k, v) -> propertiesMap.put(k.toString(), v.toString()));
        }
        return propertiesMap;
    }

    @Override
    public Object loadSingle(String name, File entitiesFile) {
        return load(entitiesFile).get(name);
    }

    @Override
    public void load(Bundle bundle, File rootDir) {
        File propertiesFile = FileUtils.findConfigFileOrDir(rootDir, getFileName());
        load(propertiesFile).forEach((k, v) -> putToBundle(bundle, rootDir, k, v.toString()));
    }

    @Override
    public void load(Bundle bundle, String name, String value) {
        putToBundle(bundle, null, name, value);
    }

    private void putToBundle(Bundle bundle, File rootDir, String key, String value) {
        P entity;
        try {
            entity = this.getEntityClass().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ConfigLoadException("Could not create entity instance for " + this.getEntityClass().getSimpleName(), e);
        }

        entity.setKey(key);
        entity.setValue(value);
        entity.postLoad(key, bundle, rootDir, idGenerator);

        bundle.getEntities(this.getEntityClass()).put(key, entity);
    }

    /**
     * @return the file name for this loader
     */
    protected abstract String getFileName();

    /**
     * @return the entity class of this loader
     */
    protected abstract Class<P> getEntityClass();

}
