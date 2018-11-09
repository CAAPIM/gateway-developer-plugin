/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.GatewayEntityInfo;
import org.reflections.Reflections;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.NO_INFO;
import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

/**
 *
 */
@Singleton
public class EntityTypeRegistry {

    private final Map<String, GatewayEntityInfo> entityTypeMap;

    @Inject
    public EntityTypeRegistry(final Reflections reflections) {
        Map<String, GatewayEntityInfo> entityTypes = new HashMap<>();
        reflections.getSubTypesOf(GatewayEntity.class).forEach(e -> {
            GatewayEntityInfo info = createEntityInfo(e);
            if (info != null) {
                entityTypes.put(info.getType(), info);
            }
        });

        this.entityTypeMap = unmodifiableMap(entityTypes);
    }

    public Class<? extends GatewayEntity> getEntityClass(String entityType) {
        return ofNullable(entityTypeMap.get(entityType)).orElse(NO_INFO).getEntityClass();
    }

    public Map<String, GatewayEntityInfo> getEntityTypeMap() {
        return entityTypeMap;
    }

}
