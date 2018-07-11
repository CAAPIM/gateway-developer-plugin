/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClusterPropertyEntityBuilder implements EntityBuilder {
    private final Document document;
    private final IdGenerator idGenerator;

    public ClusterPropertyEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle) {
        return Stream.concat(
                bundle.getStaticProperties().entrySet().stream().map(propertyEntry ->
                        buildClusterPropertyEntity(propertyEntry.getKey(), propertyEntry.getValue())
                ),
                bundle.getEnvironmentProperties().entrySet().stream()
                        .filter(propertyEntry -> propertyEntry.getKey().startsWith("gateway."))
                        .map(propertyEntry ->
                                buildEnvironmentPropertyEntity("ENV." + propertyEntry.getKey().substring(8))
                        )
        ).collect(Collectors.toList());
    }

    private Entity buildClusterPropertyEntity(String name, String value) {
        String id = idGenerator.generate();
        Entity entity = new Entity("CLUSTER_PROPERTY", name, id, buildClusterPropertyElement(name, id, value, document));
        entity.setMappingProperty(Entity.MAPPING_PROPERTY_MAP_BY, "name");
        return entity;
    }

    static Element buildClusterPropertyElement(String name, String id, String value, Document document) {
        Element clusterPropertyElement = document.createElement("l7:ClusterProperty");

        clusterPropertyElement.setAttribute("id", id);

        Element nameElement = document.createElement("l7:Name");
        nameElement.setTextContent(name);
        clusterPropertyElement.appendChild(nameElement);

        Element valueElement = document.createElement("l7:Value");
        valueElement.setTextContent(value);
        clusterPropertyElement.appendChild(valueElement);
        return clusterPropertyElement;
    }

    private Entity buildEnvironmentPropertyEntity(String name) {
        Entity entity = new Entity("CLUSTER_PROPERTY", name, idGenerator.generate(), null);
        entity.setMappingProperty(Entity.MAPPING_PROPERTY_MAP_BY, "name");
        entity.setMappingProperty(Entity.MAPPING_PROPERTY_MAP_TO, name);
        entity.setMappingProperty(Entity.MAPPING_PROPERTY_FAIL_ON_NEW, true);
        return entity;
    }
}
