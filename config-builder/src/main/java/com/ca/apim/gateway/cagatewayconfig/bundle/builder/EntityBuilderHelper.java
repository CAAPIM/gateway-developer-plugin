/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.Entity.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_EXISTING;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.*;

class EntityBuilderHelper {

    private EntityBuilderHelper() {
    }

    static Entity getEntityWithOnlyMapping(String entityType, String name, String id) {
        Entity entity = getEntityWithNameMapping(entityType, name, id, null);
        entity.setMappingAction(NEW_OR_EXISTING);
        entity.setMappingProperty(FAIL_ON_NEW, true);
        return entity;
    }

    static Entity getEntityWithNameMapping(String type, String name, String id, Element element) {
        return getEntityWithNameMapping(type, name, name, id, element, null);
    }

    static Entity getEntityWithNameMapping(String type, String originalName, String name, String id, Element element, String guid) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_BUNDLE_ENTITY_NAME, name);
        properties.put(PROPERTY_GUID, guid);
        Entity entity = new Entity(type, originalName, id, element);
        entity.setProperties(properties);
        entity.setMappingProperty(MAP_BY, MappingProperties.NAME);
        entity.setMappingProperty(MAP_TO, name);
        return entity;
    }

    static Entity getEntityWithPathMapping(String type, String originalPath, String pathInBundle, String id,
                                           Element element, boolean hasRouting) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_BUNDLE_ENTITY_NAME, pathInBundle);
        properties.put(PROPERTY_HAS_ROUTING, hasRouting);
        Entity entity = new Entity(type, originalPath, id, element);
        entity.setProperties(properties);
        entity.setMappingProperty(MAP_BY, MappingProperties.PATH);
        entity.setMappingProperty(MAP_TO, pathInBundle);
        return entity;
    }

    @VisibleForTesting
    static Entity getEntityWithMappings(final String type, final String path, final String id, final Element element,
                                        final String mappingAction, final Map<String, Object> mappingProperties) {
        Entity entity = new Entity(type, path, id, element);
        entity.setMappingAction(mappingAction);

        mappingProperties.forEach(entity::setMappingProperty);

        String mapBy = (String) entity.getMappingProperties().get(MappingProperties.MAP_BY);
        if (MappingProperties.PATH.equals(mapBy)) {
            entity.setMappingProperty(MappingProperties.MAP_TO, path);
        } else if (MappingProperties.NAME.equals(mapBy)) {
            entity.setMappingProperty(MappingProperties.MAP_TO, PathUtils.extractName(path));
        }
        return entity;
    }
}
