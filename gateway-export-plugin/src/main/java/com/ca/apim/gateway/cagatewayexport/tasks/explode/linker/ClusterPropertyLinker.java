/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EnvironmentProperty;
import com.ca.apim.gateway.cagatewayexport.util.properties.OrderedProperties;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
        Properties staticProperties = getExistingProperties(rootFolder);

        for (ClusterProperty clusterProperty : clusterPropertyMap.values()) {
            if (!staticProperties.containsKey(clusterProperty.getName())) {
                filteredBundle.addEntity(new EnvironmentProperty(clusterProperty.getName(), clusterProperty.getValue(), EnvironmentProperty.Type.GLOBAL));
                idsToRemove.add(clusterProperty.getId());
            }
        }
        idsToRemove.forEach(clusterPropertyMap::remove);
    }

    @NotNull
    private Properties getExistingProperties(File rootFolder) {
        File propertiesFile = new File(new File(rootFolder, "config"), "static.properties");
        Properties staticProperties = new OrderedProperties();
        if (propertiesFile.exists()) {
            try (InputStream stream = Files.newInputStream(propertiesFile.toPath())) {
                staticProperties.load(stream);
            } catch (IOException e) {
                throw new LinkerException("Exception reading existing contents from static.properties file", e);
            }
        }
        return staticProperties;
    }
}
