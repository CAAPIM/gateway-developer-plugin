/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.google.common.annotations.VisibleForTesting;
import org.reflections.Reflections;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EntityUtils.getEntityType;
import static java.util.Collections.unmodifiableMap;

/**
 *
 */
public class EntityTypeRegistry {

    private final Map<String, Class<? extends Entity>> entityTypeMap;

    @Inject
    public EntityTypeRegistry(final Reflections reflections) {
        Map<String, Class<? extends Entity>> entityTypes = new HashMap<>();
        reflections.getSubTypesOf(Entity.class).forEach(e -> {
            String type = getEntityType(e);
            if (type != null) {
                entityTypes.put(type, e);
            }
        });

        this.entityTypeMap = unmodifiableMap(entityTypes);
    }

    public Class<? extends Entity> getEntityClass(String entityType) {
        return entityTypeMap.get(entityType);
    }

    @VisibleForTesting
    public Map<String, Class<? extends Entity>> getEntityTypeMap() {
        return entityTypeMap;
    }
}
