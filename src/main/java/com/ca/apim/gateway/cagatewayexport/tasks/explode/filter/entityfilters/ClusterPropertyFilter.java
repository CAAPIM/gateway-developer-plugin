package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import com.ca.apim.gateway.cagatewayexport.util.gateway.DependencyUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class ClusterPropertyFilter implements EntityFilter<ClusterProperty> {

    private static final Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
            EncassFilter.class,
            IdentityProviderFilter.class,
            PolicyBackedServiceFilter.class,
            PolicyFilter.class,
            ServiceFilter.class).collect(Collectors.toSet());

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return FILTER_DEPENDENCIES;
    }

    @Override
    public List<ClusterProperty> filter(String folderPath, Bundle bundle, Bundle filteredBundle) {
        return DependencyUtils.filterDependencies(ClusterProperty.class, bundle, filteredBundle);
    }
}
