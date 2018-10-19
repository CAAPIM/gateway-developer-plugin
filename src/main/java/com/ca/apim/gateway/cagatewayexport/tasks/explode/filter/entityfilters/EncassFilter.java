package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class EncassFilter implements EntityFilter<EncassEntity> {

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return Collections.singleton(PolicyFilter.class);
    }

    @Override
    public List<EncassEntity> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        // get all policies from the filtered bundle
        Map<String, PolicyEntity> policies = filteredBundle.getEntities(PolicyEntity.class);
        return bundle.getEntities(EncassEntity.class).values().stream()
                // keep only encapsulated assertions with backing policies that are in the filtered bundle
                .filter(e -> policies.containsKey(e.getPolicyId())).collect(Collectors.toList());
    }

}
