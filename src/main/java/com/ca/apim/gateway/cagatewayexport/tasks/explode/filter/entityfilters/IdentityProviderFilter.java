package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.IdentityProviderEntity;
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
public class IdentityProviderFilter implements EntityFilter<IdentityProviderEntity> {

    private static final Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
            PolicyFilter.class,
            ServiceFilter.class).collect(Collectors.toSet());
    private final String ENTITY_NAME = "identityProviders";

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return FILTER_DEPENDENCIES;
    }

    @Override
    public List<IdentityProviderEntity> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        Stream<IdentityProviderEntity> identityProviderEntityStream = DependencyUtils.filterDependencies(IdentityProviderEntity.class, bundle, filteredBundle, e -> filterConfiguration.getRequiredEntityNames(ENTITY_NAME).contains(e.getName())).stream();
        if (!filterConfiguration.getRequiredEntityNames(ENTITY_NAME).contains(IdentityProviderEntity.INTERNAL_IDP_NAME)) {
            identityProviderEntityStream = identityProviderEntityStream
                    // filter out the internal identity provider
                    .filter(idp -> !IdentityProviderEntity.INTERNAL_IDP_ID.equals(idp.getId()));
        }
        List<IdentityProviderEntity> identityProviders = identityProviderEntityStream.collect(Collectors.toList());
        DependencyUtils.validateEntitiesInList(identityProviders, filterConfiguration.getRequiredEntityNames(ENTITY_NAME), "Identity Provider(s)");
        return identityProviders;
    }

    @Override
    public String getFilterableEntityName() {
        return ENTITY_NAME;
    }
}
