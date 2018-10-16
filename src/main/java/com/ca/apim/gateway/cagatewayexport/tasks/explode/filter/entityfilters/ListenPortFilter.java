package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity.DEFAULT_PORTS;

@Singleton
public class ListenPortFilter implements EntityFilter<ListenPortEntity> {

    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return Collections.emptySet();
    }

    @Override
    public List<ListenPortEntity> filter(String folderPath, Bundle bundle, Bundle filteredBundle) {
        return bundle.getEntities(ListenPortEntity.class).values().stream()
                // filter out the default listen ports
                .filter(l -> !DEFAULT_PORTS.contains(l.getPort())).collect(Collectors.toList());
    }

}
