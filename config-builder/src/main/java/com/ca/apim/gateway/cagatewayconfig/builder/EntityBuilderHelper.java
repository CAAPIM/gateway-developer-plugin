/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.builder;

import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import org.w3c.dom.Element;

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
}
