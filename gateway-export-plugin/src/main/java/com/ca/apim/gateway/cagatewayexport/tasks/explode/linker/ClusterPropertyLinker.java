/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ClusterProperty;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.GlobalEnvironmentProperty;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;

import javax.inject.Singleton;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayexport.util.properties.PropertyFileUtils.loadExistingProperties;

@Singleton
public class ClusterPropertyLinker implements EntitiesLinker {
    @Override
    public void link(Bundle filteredBundle, Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void link(Bundle filteredBundle, Bundle bundle, File rootFolder) {
        Map<String, ClusterProperty> clusterPropertyMap = filteredBundle.getEntities(ClusterProperty.class);
        Set<String> idsToRemove = new HashSet<>();
        File propertiesFile = new File(new File(rootFolder, "config"), "static.properties");
        Properties staticProperties = loadExistingProperties(propertiesFile);

        for (ClusterProperty clusterProperty : clusterPropertyMap.values()) {
            if (!staticProperties.containsKey(clusterProperty.getName())) {
                GlobalEnvironmentProperty prop = new GlobalEnvironmentProperty(clusterProperty.getName(), clusterProperty.getValue());
                filteredBundle.getEntities(GlobalEnvironmentProperty.class).put(prop.getName(), prop);
                idsToRemove.add(clusterProperty.getId());
            }
        }
        idsToRemove.forEach(clusterPropertyMap::remove);
    }

    @Override
    public void link(Set<Dependency> dependencies) {
        dependencies.stream().filter(entity -> EntityTypes.CLUSTER_PROPERTY_TYPE.equals(entity.getType())).forEach(e -> e.setType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE));
    }

}
