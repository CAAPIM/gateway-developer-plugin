/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import org.w3c.dom.Element;

import java.util.Map;

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
        Entity entity = new Entity(type, name, id, element);
        entity.setMappingProperty(MAP_BY, MappingProperties.NAME);
        entity.setMappingProperty(MAP_TO, name);
        return entity;
    }

    static Entity getEntityWithNameMapping(String type, String name, String id, Element element, String guid) {
        Entity entity = new Entity(type, name, id, element, guid);
        entity.setMappingProperty(MAP_BY, MappingProperties.NAME);
        entity.setMappingProperty(MAP_TO, name);
        return entity;
    }

    static Entity getEntityWithPathMapping(String type, String path, String id, Element element) {
        Entity entity = new Entity(type, path, id, element);
        entity.setMappingProperty(MAP_BY, MappingProperties.PATH);
        entity.setMappingProperty(MAP_TO, path);
        return entity;
    }

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
