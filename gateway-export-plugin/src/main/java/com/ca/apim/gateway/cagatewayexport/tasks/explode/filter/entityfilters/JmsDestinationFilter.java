/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import com.ca.apim.gateway.cagatewayexport.util.gateway.DependencyUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class JmsDestinationFilter implements EntityFilter<JmsDestination> {
    
    private static final Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
            PolicyFilter.class,
            ServiceFilter.class).collect(Collectors.toSet());
    private static final String ENTITY_NAME = "jmsDestinations";

    @Override
    public String getFilterableEntityName() {
        return ENTITY_NAME;
    }
    
    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return FILTER_DEPENDENCIES;
    }

    @Override
    public List<JmsDestination> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        List<JmsDestination> jmsDestinations = DependencyUtils.filterDependencies(
                JmsDestination.class,
                bundle,
                filteredBundle,
                e -> filterConfiguration.getRequiredEntityNames(ENTITY_NAME).contains(e.getName())
        );
        DependencyUtils.validateEntitiesInList(
                jmsDestinations,
                filterConfiguration.getRequiredEntityNames(ENTITY_NAME),
                "JMS Destination(s)"
        );
        return jmsDestinations;
    }
}
