package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import com.ca.apim.gateway.cagatewayexport.util.gateway.DependencyUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ListenPortFilter implements EntityFilter<ListenPortEntity> {

    private static final String ENTITY_NAME = "listenPorts";

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return Collections.emptySet();
    }

    @Override
    public List<ListenPortEntity> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        List<ListenPortEntity> listenPorts = bundle.getEntities(ListenPortEntity.class).values().stream()
                // filter out the default listen ports
                .filter(l -> filterConfiguration.getRequiredEntityNames(ENTITY_NAME).contains(l.getName())).collect(Collectors.toList());
        DependencyUtils.validateEntitiesInList(listenPorts, filterConfiguration.getRequiredEntityNames(ENTITY_NAME), "Listen Port(s)");
        return listenPorts;
    }

    @Override
    public String getFilterableEntityName() {
        return ENTITY_NAME;
    }
}
