/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static org.junit.jupiter.api.Assertions.*;

class JmsDestinationLoaderTest {
    
    private JmsDestinationLoader loader = new JmsDestinationLoader();
    
    @Test
    void load() {
        Bundle bundle = new Bundle();
        loader.load(bundle, createJmsDestinationXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument())
        );

        assertFalse(bundle.getJmsDestinations().isEmpty());
        assertEquals(1, bundle.getJmsDestinations().size());

        JmsDestination jmsDestination = bundle.getJmsDestinations().get("jms-1");
        assertNotNull(jmsDestination);
        assertEquals("jms-1", jmsDestination.getName());
        assertEquals("id-1", jmsDestination.getId());
        assertTrue(jmsDestination.isInbound());
        assertFalse(jmsDestination.isTemplate());
        assertEquals("TIBCO EMS", jmsDestination.getProviderType());
    }
    
    private static Element createJmsDestinationXml(Document document) {
        Element jmsDestinationEle = createElementWithAttributesAndChildren(
                document,
                JMS_DESTINATION,
                ImmutableMap.of(ATTRIBUTE_ID, "id-1"),
                createElementWithAttributesAndChildren(
                        document,
                        JMS_DESTINATION_DETAIL,
                        ImmutableMap.of(ATTRIBUTE_ID, "id-1"),
                        createElementWithTextContent(document, NAME, "jms-1"),
                        createElementWithTextContent(document, INBOUND, "true"),
                        createElementWithTextContent(document, TEMPLATE, "false")
                ),
                createElementWithAttributesAndChildren(
                        document,
                        JMS_CONNECTION,
                        ImmutableMap.of(ATTRIBUTE_ID, "id-2"),
                        // provider type is null for Generic JMS
                        createElementWithTextContent(document, JMS_PROVIDER_TYPE, "TIBCO EMS"),
                        createElementWithTextContent(document, TEMPLATE, "false")
                )
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, NAME, "jms-1"),
                createElementWithTextContent(document, ID, "id-1"),
                createElementWithTextContent(document, TYPE, EntityTypes.JMS_DESTINATION_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        jmsDestinationEle
                )
        );
    }
}
