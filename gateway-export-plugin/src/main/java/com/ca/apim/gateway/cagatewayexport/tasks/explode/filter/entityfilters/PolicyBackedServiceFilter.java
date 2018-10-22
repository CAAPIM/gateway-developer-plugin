package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyBackedServiceEntity;
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
public class PolicyBackedServiceFilter implements EntityFilter<PolicyBackedServiceEntity> {

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return Collections.singleton(PolicyFilter.class);
    }

    @Override
    public List<PolicyBackedServiceEntity> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        // get all policies from the filtered bundle
        Map<String, PolicyEntity> policies = filteredBundle.getEntities(PolicyEntity.class);
        return bundle.getEntities(PolicyBackedServiceEntity.class).values().stream()
                .filter(pbs -> pbs.getOperations().values().stream().anyMatch(policies::containsKey)).collect(Collectors.toList());
    }
}
