/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Singleton
public class EntityLoaderRegistry {

    private final Collection<EntityLoader> entityLoaders;

    @Inject
    EntityLoaderRegistry(final Set<EntityLoader> loaders) {
        this.entityLoaders = Collections.unmodifiableCollection(loaders);
    }

    public Collection<EntityLoader> getEntityLoaders() {
        return entityLoaders;
    }
}
