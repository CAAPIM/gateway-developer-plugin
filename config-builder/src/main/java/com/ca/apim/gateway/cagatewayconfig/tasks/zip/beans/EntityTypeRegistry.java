/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.google.common.annotations.VisibleForTesting;
import org.reflections.Reflections;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 *
 */
public class EntityTypeRegistry {

    private final Map<String, Class<? extends GatewayEntity>> entityTypeMap;

    @Inject
    public EntityTypeRegistry(final Reflections reflections) {
        Map<String, Class<? extends GatewayEntity>> entityTypes = new HashMap<>();
        reflections.getSubTypesOf(GatewayEntity.class).forEach(e -> {
            String type = EntityUtils.getEntityType(e);
            if (type != null) {
                entityTypes.put(type, e);
            }
        });

        this.entityTypeMap = unmodifiableMap(entityTypes);
    }

    public Class<? extends GatewayEntity> getEntityClass(String entityType) {
        return entityTypeMap.get(entityType);
    }

    @VisibleForTesting
    public Map<String, Class<? extends GatewayEntity>> getEntityTypeMap() {
        return entityTypeMap;
    }
}
