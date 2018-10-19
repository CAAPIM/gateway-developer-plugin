package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.StoredPasswordEntity;
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
public class StoredPasswordFilter implements EntityFilter<StoredPasswordEntity> {

    private static final Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
            PolicyFilter.class,
            ServiceFilter.class,
            JDBCConnectionFilter.class,
            CassandraConnectionFilter.class).collect(Collectors.toSet());
    private static final String ENTITY_NAME = "passwords";

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return FILTER_DEPENDENCIES;
    }

    @Override
    public List<StoredPasswordEntity> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        List<StoredPasswordEntity> passwords = DependencyUtils.filterDependencies(StoredPasswordEntity.class, bundle, filteredBundle, e -> filterConfiguration.getRequiredEntityNames(ENTITY_NAME).contains(e.getName()))
                // currently only string password types are supported. PEM password support still needs to be added
                .stream().filter(e -> e.isType(StoredPasswordEntity.Type.PASSWORD)).collect(Collectors.toList());
        DependencyUtils.validateEntitiesInList(passwords, filterConfiguration.getRequiredEntityNames(ENTITY_NAME), "Password(s)");
        return passwords;
    }

    @Override
    public String getFilterableEntityName() {
        return ENTITY_NAME;
    }
}
