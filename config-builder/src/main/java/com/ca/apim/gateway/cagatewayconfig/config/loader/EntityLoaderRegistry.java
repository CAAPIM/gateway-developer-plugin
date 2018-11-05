/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Singleton
public class EntityLoaderRegistry {

    private final Map<String, EntityLoader> entityLoaders;

    @Inject
    EntityLoaderRegistry(final Set<EntityLoader> loaders) {
        this.entityLoaders = unmodifiableMap(loaders.stream().collect(toMap(EntityLoader::getEntityType, identity())));
    }
    public EntityLoader getLoader(String type) {
        return entityLoaders.get(type);
    }

    public Collection<EntityLoader> getEntityLoaders() {
        return entityLoaders.values();
    }
}
