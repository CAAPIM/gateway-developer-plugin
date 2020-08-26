/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static java.util.Collections.unmodifiableCollection;

@Singleton
public class EntityFilterRegistry {

    private final Collection<EntityFilter> entityFilters;

    @Inject
    public EntityFilterRegistry(final Set<EntityFilter> entityFilters) {
        // Ordering is necessary for the filtering, otherwise it may not work appropriately.
        final Map<String, EntityFilter> sortedFilters = new LinkedHashMap<>();
        for (EntityFilter entityFilter : entityFilters)
        {
            addFilterWithDependencies(entityFilter.getClass(), sortedFilters);
            sortedFilters.put(entityFilter.getClass().getName(), entityFilter);
        }
        this.entityFilters = unmodifiableCollection(sortedFilters.values());
    }

    private void addFilterWithDependencies(Class<? extends EntityFilter> entityFilter, Map<String, EntityFilter> sortedFilters) {
        Collection<Class<? extends EntityFilter>> dependentFilters = getDependentFilters(entityFilter);
        if(dependentFilters.isEmpty()){
            sortedFilters.putIfAbsent(entityFilter.getName(), null);
        } else {
            for(Class<? extends EntityFilter> dependentFilter : dependentFilters) {
                if(!sortedFilters.containsKey(dependentFilter.getName())){
                    addFilterWithDependencies(dependentFilter, sortedFilters);
                }
            }
            sortedFilters.putIfAbsent(entityFilter.getName(), null);
        }
    }

    private Collection<Class<? extends EntityFilter>> getDependentFilters(Class<? extends EntityFilter> entityFilterClass) {
        EntityFilter entityFilter = EntityFilter.getEntityFilterFromClass(entityFilterClass);
        return (Collection<Class<? extends EntityFilter>>) entityFilter.getDependencyEntityFilters();
    }

    public Collection<EntityFilter> getEntityFilters() {
        return entityFilters;
    }
}
