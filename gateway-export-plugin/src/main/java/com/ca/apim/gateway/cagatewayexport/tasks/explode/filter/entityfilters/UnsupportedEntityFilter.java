package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnsupportedEntityFilter implements EntityFilter<UnsupportedGatewayEntity> {
    private static final Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
            PolicyFilter.class,
            ServiceFilter.class).collect(Collectors.toSet());

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return FILTER_DEPENDENCIES;
    }

    @Override
    public List<UnsupportedGatewayEntity> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        Collection<Dependency> dependencies = bundle.getDependencyMap().entrySet().stream()
                // filter out dependencies that are not in the filtered bundle
                .filter(e -> filteredBundle.getEntities(e.getKey().getTypeClass()).get(e.getKey().getId()) != null)
                // keep only the dependencies
                .flatMap(e -> e.getValue().stream())
                .filter(d -> d.getTypeClass() == UnsupportedGatewayEntity.class)
                .collect(Collectors.toSet());
        // Gets entities of the given type that are dependencies of entities in the filteredBundle
        return bundle.getEntities(UnsupportedGatewayEntity.class).values().stream()
                .filter(unsupportedEntity -> dependencies.contains(new Dependency(unsupportedEntity.getId(),
                        UnsupportedGatewayEntity.class, unsupportedEntity.getName(), unsupportedEntity.getType())))
                .collect(Collectors.toList());
    }
}
