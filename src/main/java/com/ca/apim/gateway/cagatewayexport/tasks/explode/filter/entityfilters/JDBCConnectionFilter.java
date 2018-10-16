package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.JdbcConnectionEntity;
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
public class JDBCConnectionFilter implements EntityFilter<JdbcConnectionEntity> {

    private static final Set<Class<? extends EntityFilter>> FILTER_DEPENDENCIES = Stream.of(
            PolicyFilter.class,
            ServiceFilter.class).collect(Collectors.toSet());

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return FILTER_DEPENDENCIES;
    }

    @Override
    public List<JdbcConnectionEntity> filter(String folderPath, Bundle bundle, Bundle filteredBundle) {
        return DependencyUtils.filterDependencies(JdbcConnectionEntity.class, bundle, filteredBundle);
    }
}
