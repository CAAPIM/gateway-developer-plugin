/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.google.common.annotations.VisibleForTesting;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.Entity.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_EXISTING;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_UPDATE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.*;

class EntityBuilderHelper {
    private static final Logger LOGGER = Logger.getLogger(EntityBuilderHelper.class.getName());
    // TODO: reconsider the default value. NewOrExisting is the safest and acceptable action for most of the customer's scenarios.
    private static final String DEFAULT_ENTITY_MAPPING_ACTION = NEW_OR_UPDATE;
    private static final String DEFAULT_ENTITY_MAPPING_ACTION_PROPERTY = "com.ca.apim.build.defaultEntityMappingAction";
    private static String defaultEntityMappingAction;

    private static final String IGNORE_ANNOTATIONS_PROPERTY = "com.ca.apim.build.ignoreAnnotations";
    private static final String IGNORE_ANNOTATIONS = System.getProperty(IGNORE_ANNOTATIONS_PROPERTY);
    private EntityBuilderHelper() {
    }

    @VisibleForTesting
    static void resetDefaultEntityMappingAction(String value) {
        defaultEntityMappingAction = null;
        System.setProperty(DEFAULT_ENTITY_MAPPING_ACTION_PROPERTY, value != null ? value : DEFAULT_ENTITY_MAPPING_ACTION);
    }

    static String getDefaultEntityMappingAction() {
        if (defaultEntityMappingAction == null) {
            String action = System.getProperty(DEFAULT_ENTITY_MAPPING_ACTION_PROPERTY);
            if (action == null ||
                    !Pattern.matches(NEW_OR_EXISTING + "|" + NEW_OR_UPDATE, action)) {
                action = DEFAULT_ENTITY_MAPPING_ACTION;
            }
            defaultEntityMappingAction = action;
            LOGGER.info("Using default entity mapping action as " + defaultEntityMappingAction);
        }

        return defaultEntityMappingAction;
    }

    /**
     * Returns system property ignore annotations value, default is false
     * @return boolean
     */
    static boolean isIgnoreAnnotations() {
       return IGNORE_ANNOTATIONS != null && "true".equals(IGNORE_ANNOTATIONS.trim());
    }

    static Entity getEntityWithOnlyMapping(String entityType, String name, String id) {
        Entity entity = getEntityWithNameMapping(entityType, name, id, null);
        entity.setMappingAction(NEW_OR_EXISTING);
        entity.setMappingProperty(FAIL_ON_NEW, true);
        return entity;
    }

    static Entity getEntityWithNameMapping(String type, String name, String id, Element element) {
        return getEntityWithNameMapping(type, name, name, id, element, null, null);
    }

    static Entity getEntityWithNameMapping(String type, String originalName, String name, String id, Element element, String guid, GatewayEntity gatewayEntity) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_BUNDLE_ENTITY_NAME, name);
        properties.put(PROPERTY_GUID, guid);
        Entity entity = new Entity(type, originalName, id, element, gatewayEntity);
        entity.setProperties(properties);
        entity.setMappingProperty(MAP_BY, MappingProperties.NAME);
        entity.setMappingProperty(MAP_TO, name);
        return entity;
    }

    static Entity getEntityWithPathMapping(String type, String originalPath, String pathInBundle, String id,
                                           Element element, boolean hasRouting, GatewayEntity gatewayEntity) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_BUNDLE_ENTITY_NAME, pathInBundle);
        properties.put(PROPERTY_HAS_ROUTING, hasRouting);
        Entity entity = new Entity(type, originalPath, id, element, gatewayEntity);
        entity.setProperties(properties);
        entity.setMappingProperty(MAP_BY, MappingProperties.PATH);
        entity.setMappingProperty(MAP_TO, pathInBundle);
        return entity;
    }

    @VisibleForTesting
    static Entity getEntityWithMappings(final String type, final String path, final String id, final Element element,
                                        final String mappingAction, final Map<String, Object> mappingProperties) {
        Entity entity = new Entity(type, path, id, element, null);
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

    static Entity getEntityWithDeleteMappings(final Entity entity){
        return getEntityWithMappings(entity.getType(), entity.getName(), entity.getId(), null, MappingActions.DELETE, entity.getMappingProperties());
    }

    static String getPath(Folder folder, String name) {
        return PathUtils.unixPath(getPath(folder).toString(), name);
    }

    static Path getPath(Folder folder) {
        if (folder.getParentFolder() == null) {
            return Paths.get("");
        }
        return getPath(folder.getParentFolder()).resolve(folder.getName());
    }
}
