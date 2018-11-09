/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.EntityTypeRegistry;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.GatewayEntityInfo;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.createEntityLoader;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.createPropertiesLoader;
import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.PROPERTIES;
import static java.util.Collections.unmodifiableMap;

@Singleton
public class EntityLoaderRegistry {

    private final Map<String, EntityLoader> entityLoaders;

    @Inject
    EntityLoaderRegistry(final Set<EntityLoader> loaders,
                         final EntityTypeRegistry entityTypeRegistry,
                         final JsonTools jsonTools,
                         final IdGenerator idGenerator,
                         final FileUtils fileUtils) {
        Map<String, EntityLoader> allLoaders = new HashMap<>();
        // adds the concrete loaders implementation
        loaders.forEach(l -> allLoaders.put(l.getEntityType(), l));

        // adds loaders for the ones without a concrete implementation
        entityTypeRegistry.getEntityTypeMap().values().forEach(e -> {
            if (e.getEnvironmentType() != null) {
                allLoaders.computeIfAbsent(e.getEnvironmentType(), s -> createGenericLoaderForEntity(jsonTools, idGenerator, fileUtils, e));
            }
        });
        this.entityLoaders = unmodifiableMap(allLoaders);
    }

    public EntityLoader getLoader(String type) {
        return entityLoaders.get(type);
    }

    public Collection<EntityLoader> getEntityLoaders() {
        return entityLoaders.values();
    }

    @NotNull
    @VisibleForTesting
    static EntityLoader createGenericLoaderForEntity(JsonTools jsonTools,
                                                     IdGenerator idGenerator,
                                                     FileUtils fileUtils,
                                                     GatewayEntityInfo entityInfo) {
        if (entityInfo.getFileType() == PROPERTIES) {
            return createPropertiesLoader(fileUtils, idGenerator, entityInfo);
        }

        return createEntityLoader(jsonTools, idGenerator, entityInfo);
    }

}
