/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_EXISTING;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

public class BundleDocumentBuilder {

    private final List<Entity> entities = new LinkedList<>();
    private final Document document;

    BundleDocumentBuilder(final Document document) {
        this.document = document;
    }

    public Element build() {
        final Element references = document.createElement(REFERENCES);
        final Element mappings = document.createElement(MAPPINGS);

        final Element bundle = createElementWithChildren(document, BUNDLE, references, mappings);

        entities.forEach(e -> addEntity(references, mappings, e));
        bundle.setAttribute("xmlns:l7", "http://ns.l7tech.com/2010/04/gateway-management");
        return bundle;
    }

    private void addEntity(final Element references, final Element mappings, final Entity entity) {
        if (entity.getXml() != null) {
            final Element entityItem = buildEntityItem(entity, document);
            references.appendChild(entityItem);
        }
        final Element entityMapping = buildEntityMapping(entity, document);
        mappings.appendChild(entityMapping);
    }

    private Element buildEntityMapping(final Entity entity, final Document document) {
        final Element mapping = createElementWithAttributes(document, MAPPING, ImmutableMap.of(ATTRIBUTE_ACTION, NEW_OR_EXISTING, ATTRIBUTE_SRCID, entity.getId(), ATTRIBUTE_TYPE, entity.getType()));
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

    void addEntities(List<Entity> entitiesToAdd) {
        entities.addAll(entitiesToAdd);
    }
}