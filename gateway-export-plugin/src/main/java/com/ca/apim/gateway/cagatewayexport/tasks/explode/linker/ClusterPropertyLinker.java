/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EnvironmentProperty;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Singleton
public class ClusterPropertyLinker implements EntitiesLinker {
    private DocumentFileUtils documentFileUtils;

    @Inject
    ClusterPropertyLinker(DocumentFileUtils documentFileUtils) {
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void link(Bundle filteredBundle, Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(Bundle filteredBundle, Bundle bundle, File rootFolder) {
        Map<String, ClusterProperty> clusterPropertyMap = filteredBundle.getEntities(ClusterProperty.class);
        Set<String> idsToRemove = new HashSet<>();
        File propertiesFile = new File(new File(rootFolder, "config"), "static.properties");
        Properties staticProperties = documentFileUtils.loadExistingProperties(propertiesFile, "static");

        for (ClusterProperty clusterProperty : clusterPropertyMap.values()) {
            if (!staticProperties.containsKey(clusterProperty.getName())) {
                filteredBundle.addEntity(new EnvironmentProperty(clusterProperty.getName(), clusterProperty.getValue(), EnvironmentProperty.Type.GLOBAL));
                idsToRemove.add(clusterProperty.getId());
            }
        }
        idsToRemove.forEach(clusterPropertyMap::remove);
    }

}
