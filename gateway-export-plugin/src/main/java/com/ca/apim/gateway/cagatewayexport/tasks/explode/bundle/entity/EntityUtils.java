/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;

public class EntityUtils {

    private EntityUtils() {
        //
    }

    /**
     * @param entityClass entity class
     * @param <E> entity type
     * @return the entity type defined by annotation {@link Named} on each entity class
     */
    public static <E extends Entity> String getEntityType(Class<E> entityClass) {
        Named named = entityClass.getAnnotation(Named.class);
        return named != null ? named.value() : null;
    }
}
