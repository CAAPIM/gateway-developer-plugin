package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UnsupportedEntityFilter implements EntityFilter<UnsupportedGatewayEntity> {
    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<UnsupportedGatewayEntity> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        return bundle.getEntities(UnsupportedGatewayEntity.class).values().stream()
                .filter(p ->! p.getType().equals("RBAC_ROLE")).collect(Collectors.toList());
    }
}
