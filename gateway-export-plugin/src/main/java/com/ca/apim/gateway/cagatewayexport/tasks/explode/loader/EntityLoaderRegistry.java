/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Singleton
public class EntityLoaderRegistry {

    private final Map<String, EntityLoader> entityLoaders;

    @Inject
    public EntityLoaderRegistry(final Set<EntityLoader> loaders) {
        entityLoaders = unmodifiableMap(loaders.stream().collect(toMap(EntityLoader::entityType, identity())));
    }

    public EntityLoader getLoader(String type) {
        return entityLoaders.get(type);
    }

    @VisibleForTesting
    public Map<String, EntityLoader> getEntityLoaders() {
        return entityLoaders;
    }
}
