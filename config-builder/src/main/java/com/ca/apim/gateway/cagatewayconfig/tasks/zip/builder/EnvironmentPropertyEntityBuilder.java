/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentPropertyEntityBuilder implements EntityBuilder {
    private final Document document;
    private final IdGenerator idGenerator;

    EnvironmentPropertyEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle) {
        return bundle.getEnvironmentProperties().entrySet().stream()
                .filter(propertyEntry -> propertyEntry.getKey().startsWith("gateway."))
                .map(propertyEntry ->
                        buildEnvironmentPropertyEntity(propertyEntry.getKey().substring(8))
                ).collect(Collectors.toList());
    }

    private Entity buildEnvironmentPropertyEntity(String name) {
        String id = idGenerator.generate();
        HashMap<String, String> valueAttributes = new HashMap<>();
        valueAttributes.put("env", "true");
        Entity entity = new Entity("CLUSTER_PROPERTY", "ENV." + name, id, ClusterPropertyEntityBuilder.buildClusterPropertyElement("ENV." + name, id, "ENV.gateway." + name, document, valueAttributes));
        entity.setMappingProperty(Entity.MAPPING_PROPERTY_MAP_BY, "name");
        return entity;
    }
}
