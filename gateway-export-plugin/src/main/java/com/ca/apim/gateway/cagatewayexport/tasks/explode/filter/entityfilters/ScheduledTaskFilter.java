/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.ScheduledTask;
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
public class ScheduledTaskFilter implements EntityFilter<ScheduledTask> {
    @Override
    public @NotNull Collection<Class<? extends EntityFilter>> getDependencyEntityFilters() {
        return Collections.singleton(PolicyFilter.class);
    }

    @Override
    public List<ScheduledTask> filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle, Bundle filteredBundle) {
        // get all policies from the filtered bundle
        Map<String, Policy> policies = filteredBundle.getEntities(Policy.class);
        return bundle.getEntities(ScheduledTask.class).values().stream()
                // keep only Scheduled Tasks with backing policies that are in the filtered bundle
                .filter(e -> policies.containsKey(e.getPolicy())).collect(Collectors.toList());
    }
}
