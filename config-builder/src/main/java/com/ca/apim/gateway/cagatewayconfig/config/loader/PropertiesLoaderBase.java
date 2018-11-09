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
import java.util.Properties;

public abstract class PropertiesLoaderBase<P extends PropertiesEntity> implements EntityLoader {

    private FileUtils fileUtils;
    private final IdGenerator idGenerator;

    PropertiesLoaderBase(final FileUtils fileUtils, final IdGenerator idGenerator) {
        this.fileUtils = fileUtils;
        this.idGenerator = idGenerator;
    }

    @Override
    public void load(Bundle bundle, File rootDir) {
        File propertiesFile = new File(rootDir, this.getFilePath());
        if (propertiesFile.exists()) {
            Properties properties = new Properties();
            try (InputStream inStream = fileUtils.getInputStream(propertiesFile)) {
                properties.load(inStream);
            } catch (IOException e) {
                throw new ConfigLoadException("Could not load properties file (" + propertiesFile + "): " + e.getMessage(), e);
            }
            properties.forEach((k, v) -> putToBundle(bundle, rootDir, k.toString(), v.toString()));
        }
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
     * @return the file path for this loader
     */
    protected abstract String getFilePath();

    /**
     * @return the entity class of this loader
     */
    protected abstract Class<P> getEntityClass();

}
