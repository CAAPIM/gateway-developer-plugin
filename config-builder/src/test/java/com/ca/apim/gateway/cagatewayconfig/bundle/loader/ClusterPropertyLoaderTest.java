/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ClusterProperty;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class ClusterPropertyLoaderTest {

    @Test
    void load() {
        ClusterPropertyLoader loader = new ClusterPropertyLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument()
        ));

        assertFalse(bundle.getClusterProperties().isEmpty());
        assertEquals(1, bundle.getClusterProperties().size());
        ClusterProperty clusterProperty = bundle.getClusterProperties().get("Prop");
        assertNotNull(clusterProperty);
        assertEquals("Prop", clusterProperty.getName());
        assertEquals("Value", clusterProperty.getValue());
        assertEquals("id", clusterProperty.getId());
    }

    private static Element createXml(Document document) {
        Element element = createElementWithAttributesAndChildren(
                document,
                CLUSTER_PROPERTY,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithTextContent(document, NAME, "Prop"),
                createElementWithTextContent(document, VALUE, "Value")
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.CLUSTER_PROPERTY_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        element
                )
        );
    }
}