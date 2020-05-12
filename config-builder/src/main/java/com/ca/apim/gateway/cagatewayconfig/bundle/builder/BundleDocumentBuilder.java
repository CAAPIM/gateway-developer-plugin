/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BuilderConstants.FILTER_ENV_ENTITIES;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.FOLDER_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_UPDATE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

@Singleton
public class BundleDocumentBuilder {

    static final String L7 = "xmlns:l7";
    static final String GATEWAY_MANAGEMENT = "http://ns.l7tech.com/2010/04/gateway-management";

    public Element build(Document document, List<Entity> entities) {
        final Element references = document.createElement(REFERENCES);
        final Element mappings = document.createElement(MAPPINGS);

        final Element bundle = createElementWithChildren(document, BUNDLE, references, mappings);

        entities.forEach(e -> addEntity(references, mappings, e, document));
        bundle.setAttribute(L7, GATEWAY_MANAGEMENT);
        return bundle;
    }

    public Element buildDeleteBundle(Document document, List<Entity> entities) {
        removeEntitiesForDeleteBundle(entities); // Filter entities for delete bundle based on type

        final Element references = document.createElement(REFERENCES);
        final Element mappings = document.createElement(MAPPINGS);

        final Element bundle = createElementWithChildren(document, BUNDLE, references, mappings);

        entities.forEach(e -> {
            e.setMappingAction(MappingActions.DELETE); // Set Mapping Action to DELETE
            addEntity(references, mappings, e, document);
        });
        bundle.setAttribute(L7, GATEWAY_MANAGEMENT);
        return bundle;
    }

    private void addEntity(final Element references, final Element mappings, final Entity entity, final Document document) {
        if (entity.getXml() != null) {
            final Element entityItem = buildEntityItem(entity, document);
            references.appendChild(entityItem);
        }
        final Element entityMapping = buildEntityMapping(entity, document);
        mappings.appendChild(entityMapping);
    }

    private Element buildEntityMapping(final Entity entity, final Document document) {
        final Element mapping = createElementWithAttributes(document, MAPPING, ImmutableMap.of(ATTRIBUTE_ACTION,
                entity.getMappingAction() == null ? NEW_OR_UPDATE : entity.getMappingAction(), ATTRIBUTE_SRCID,
                entity.getId(), ATTRIBUTE_TYPE, entity.getType()));
        buildAndAppendPropertiesElement(entity.getMappingProperties(), document, mapping);

        return mapping;
    }

    private Element buildEntityItem(final Entity entity, final Document document) {
        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, NAME, entity.getName()),
                createElementWithTextContent(document, ID, entity.getId()),
                createElementWithTextContent(document, TYPE, entity.getType()),
                createElementWithChildren(document, RESOURCE, entity.getXml())
        );
    }

    /**
     * Remove environment entities and folders from the entity list. Environment entities and Folders should not be
     * part of DELETE bundle.
     *
     * @param entities list of entities to be included in the delete bundle
     */
    private void removeEntitiesForDeleteBundle(final List<Entity> entities) {
        // SKIP all environment entities from DELETE bundle
        entities.removeAll(entities.stream().filter(FILTER_ENV_ENTITIES).collect(Collectors.toList()));

        // SKIP all Folders from DELETE bundle
        entities.removeAll(entities.stream().filter(e -> FOLDER_TYPE.equals(e.getType())).collect(Collectors.toList()));
    }
}