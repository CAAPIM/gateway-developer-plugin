/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSet;

@Singleton
public class EntityFilterRegistry {

    private final Collection<EntityFilter> entityFilters;

    @Inject
    public EntityFilterRegistry(final Set<EntityFilter> entityFilters) {
        // TreeSet is needed here to sort the Entity Filter in the proper order to get a correctly filtered bundle
        // Ordering is necessary for the filtering, otherwise it may not work appropriately.
        this.entityFilters = unmodifiableSet(new TreeSet<>(entityFilters));
    }

    public Collection<EntityFilter> getEntityFilters() {
        return entityFilters;
    }
}
