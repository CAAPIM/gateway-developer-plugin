/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.GatewayEntityInfo;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.Entity;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.reflections.Reflections;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.NO_INFO;
import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

/**
 * Registry to store gateway entities metadata read from each entity class.
 *
 * - key is the entity type from Named annotation
 * - value is an object GatewayEntityInfo which holds entity type, class, file name for loading, file type (json/yaml or properties) and its environment key for provision as env property.
 */
@Singleton
public class EntityTypeRegistry {

    public static final Set<String> NON_ENV_ENTITY_TYPES;

    static {
        NON_ENV_ENTITY_TYPES = new HashSet<>();
        NON_ENV_ENTITY_TYPES.add(EntityTypes.FOLDER_TYPE);
        NON_ENV_ENTITY_TYPES.add(EntityTypes.POLICY_TYPE);
        NON_ENV_ENTITY_TYPES.add(EntityTypes.SERVICE_TYPE);
        NON_ENV_ENTITY_TYPES.add(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
    }
    private final Map<String, GatewayEntityInfo> entityTypeMap;

    @Inject
    public EntityTypeRegistry(@Named("Reflections_ConfigBuilderInjectionProvider") final Reflections reflections) {
        Map<String, GatewayEntityInfo> entityTypes = new HashMap<>();
        reflections.getSubTypesOf(GatewayEntity.class).forEach(e -> {
            GatewayEntityInfo info = createEntityInfo(e);
            if (info != null) {
                entityTypes.put(info.getType(), info);
            }
        });

        this.entityTypeMap = unmodifiableMap(entityTypes);
    }

    /**
     * Get the entity class which has the specified type, or the placeholder NO_INFO if not found.
     *
     * @param entityType the entity type
     * @return the entity class
     */
    public Class<? extends GatewayEntity> getEntityClass(String entityType) {
        return ofNullable(entityTypeMap.get(entityType)).orElse(NO_INFO).getEntityClass();
    }

    public Map<String, GatewayEntityInfo> getEntityTypeMap() {
        return entityTypeMap;
    }

    public Map<String, GatewayEntityInfo> getEnvironmentEntityTypes() {
        return entityTypeMap.entrySet().stream().filter(entry -> !NON_ENV_ENTITY_TYPES.contains(entry.getValue().getType()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }

}
