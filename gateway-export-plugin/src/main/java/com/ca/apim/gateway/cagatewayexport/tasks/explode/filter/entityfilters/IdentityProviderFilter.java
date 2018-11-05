package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider;
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
public class IdentityProviderFilter implements EntityFilter<IdentityProvider> {

    private static final Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
            PolicyFilter.class,
            ServiceFilter.class).collect(Collectors.toSet());
    private static final String ENTITY_NAME = "identityProviders";

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return FILTER_DEPENDENCIES;
    }

    @Override
    public List<IdentityProvider> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        Stream<IdentityProvider> identityProviderEntityStream = DependencyUtils.filterDependencies(IdentityProvider.class, bundle, filteredBundle, e -> filterConfiguration.getRequiredEntityNames(ENTITY_NAME).contains(e.getName())).stream();
        if (!filterConfiguration.getRequiredEntityNames(ENTITY_NAME).contains(IdentityProvider.INTERNAL_IDP_NAME)) {
            identityProviderEntityStream = identityProviderEntityStream
                    // filter out the internal identity provider
                    .filter(idp -> !IdentityProvider.INTERNAL_IDP_ID.equals(idp.getId()));
        }
        List<IdentityProvider> identityProviders = identityProviderEntityStream.collect(Collectors.toList());
        DependencyUtils.validateEntitiesInList(identityProviders, filterConfiguration.getRequiredEntityNames(ENTITY_NAME), "Identity Provider(s)");
        return identityProviders;
    }

    @Override
    public String getFilterableEntityName() {
        return ENTITY_NAME;
    }
}
