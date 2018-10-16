/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters.FolderFilter;

import javax.inject.Inject;
import java.util.List;

public class BundleFilter {
    private final EntityFilterRegistry filterRegistry;

    @Inject
    public BundleFilter(EntityFilterRegistry filterRegistry) {
        this.filterRegistry = filterRegistry;
    }

    public Bundle filter(String folderPath, Bundle bundle) {
        Bundle filteredBundle = new Bundle();
        //for each entity filter, filter and then add the results to the filtered bundle
        filterRegistry.getEntityFilters().forEach(ef -> ((List<? extends Entity>) ef.filter(folderPath, bundle, filteredBundle)).forEach(filteredBundle::addEntity));

        // Add parent folders to the filtered Bundle
        FolderFilter.parentFolders(folderPath, bundle).forEach(filteredBundle::addEntity);

        // build the folder tree
        FolderTree folderTree = new FolderTree(filteredBundle.getEntities(Folder.class).values());
        filteredBundle.setFolderTree(folderTree);
        return filteredBundle;
    }

}
