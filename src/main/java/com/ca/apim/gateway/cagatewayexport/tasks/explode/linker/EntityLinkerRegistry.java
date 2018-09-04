/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;

import static java.util.Collections.unmodifiableCollection;

@Singleton
public class EntityLinkerRegistry {

    private final Collection<EntitiesLinker> entityLinkers;

    @Inject
    public EntityLinkerRegistry(final Set<EntitiesLinker> linkers) {
        this.entityLinkers = unmodifiableCollection(linkers);
    }

    public Collection<EntitiesLinker> getEntityLinkers() {
        return entityLinkers;
    }
}
