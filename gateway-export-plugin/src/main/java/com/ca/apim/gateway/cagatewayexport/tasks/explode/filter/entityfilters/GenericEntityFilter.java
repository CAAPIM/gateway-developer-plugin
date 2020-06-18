package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;
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
public class GenericEntityFilter implements EntityFilter<GenericEntity> {

    private static final Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
            PolicyFilter.class,
            ServiceFilter.class).collect(Collectors.toSet());

    private static final String ENTITY_NAME = "genericEntities";

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return FILTER_DEPENDENCIES;
    }

    @Override
    public List<GenericEntity> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle,
                                      Bundle filteredBundle) {
        List<GenericEntity> genericEntities = DependencyUtils.filterDependencies(GenericEntity.class, bundle,
                filteredBundle, e -> filterConfiguration.getRequiredEntityNames(ENTITY_NAME).contains(e.getName()));
        DependencyUtils.validateEntitiesInList(genericEntities, filterConfiguration.getRequiredEntityNames(ENTITY_NAME),
                "Generic Entity(s)");
        return genericEntities;
    }

    @Override
    public String getFilterableEntityName() {
        return ENTITY_NAME;
    }
}
