/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Named;

import static org.apache.commons.lang3.tuple.ImmutablePair.nullPair;

public class EntityUtils {

    private EntityUtils() {
        //
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
    public static <E extends GatewayEntity> Pair<String, FileType> getEntityConfigFileInfo(Class<E> entityClass) {
        ConfigurationFile configurationFile = entityClass.getAnnotation(ConfigurationFile.class);
        return configurationFile != null ? ImmutablePair.of(configurationFile.name(), configurationFile.type()) : nullPair();
    }
}
