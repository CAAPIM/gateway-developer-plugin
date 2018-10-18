package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.TrustedCertEntity;
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
public class TrustedCertificateFilter implements EntityFilter<TrustedCertEntity> {

    private static final Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
            PolicyFilter.class,
            ServiceFilter.class,
            IdentityProviderFilter.class).collect(Collectors.toSet());
    private static final String ENTITY_NAME = "certificates";

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return FILTER_DEPENDENCIES;
    }

    @Override
    public List<TrustedCertEntity> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        List<TrustedCertEntity> certificates = DependencyUtils.filterDependencies(TrustedCertEntity.class, bundle, filteredBundle, e -> filterConfiguration.getRequiredEntityNames(ENTITY_NAME).contains(e.getName()));
        DependencyUtils.validateEntitiesInList(certificates, filterConfiguration.getRequiredEntityNames(ENTITY_NAME), "Certificate(s)");
        return certificates;
    }

    @Override
    public String getFilterableEntityName() {
        return ENTITY_NAME;
    }
}
