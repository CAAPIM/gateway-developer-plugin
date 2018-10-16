package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.StoredPasswordEntity;
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
public class StoredPasswordFilter implements EntityFilter<StoredPasswordEntity> {

    private static final Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
            PolicyFilter.class,
            ServiceFilter.class,
            JDBCConnectionFilter.class).collect(Collectors.toSet());

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return FILTER_DEPENDENCIES;
    }

    @Override
    public List<StoredPasswordEntity> filter(String folderPath, Bundle bundle, Bundle filteredBundle) {
        return DependencyUtils.filterDependencies(StoredPasswordEntity.class, bundle, filteredBundle)
                // currently only string password types are supported. PEM password support still needs to be added
                .stream().filter(e -> e.isType(StoredPasswordEntity.Type.PASSWORD)).collect(Collectors.toList());
    }
}
