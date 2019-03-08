/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.FolderTree;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters.FolderFilter;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class BundleFilter {
    private final EntityFilterRegistry filterRegistry;

    @Inject
    public BundleFilter(EntityFilterRegistry filterRegistry) {
        this.filterRegistry = filterRegistry;
    }

    public Bundle filter(String folderPath, FilterConfiguration filterConfiguration, Bundle bundle) {
        // Check that all the entity filters in the filter configuration have filters available for them
        filterConfiguration.getEntityFilters().keySet().forEach(k -> {
            boolean noFilters = filterRegistry.getEntityFilters().stream().noneMatch(f -> k.equals(f.getFilterableEntityName()));
            if (noFilters) {
                throw new IllegalArgumentException("Unknown entity type: " + k);
            }
        });

        Bundle filteredBundle = new Bundle();
        //for each entity filter, filter and then add the results to the filtered bundle
        filterRegistry.getEntityFilters()
                .forEach(ef ->
                        ((List<? extends GatewayEntity>) ef.filter(folderPath, filterConfiguration, bundle, filteredBundle))
                                .forEach(e -> {
                                            final Map<String, GatewayEntity> entities = filteredBundle.getEntities((Class<GatewayEntity>) e.getClass());
                                            entities.put(e.getId(), e);
                                        }
                                )
                );

        // Add parent folders to the filtered Bundle
        FolderFilter.parentFolders(folderPath, bundle).forEach(f -> filteredBundle.getFolders().put(f.getId(), f));

        // build the folder tree
        FolderTree folderTree = new FolderTree(filteredBundle.getFolders().values());
        filteredBundle.setFolderTree(folderTree);
        return filteredBundle;
    }

}
