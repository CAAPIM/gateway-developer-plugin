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

import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static org.junit.jupiter.api.Assertions.*;

class JmsDestinationLoaderTest {
    
    private JmsDestinationLoader loader = new JmsDestinationLoader();
    
    @Test
    void testLoad() {
        Bundle bundle = new Bundle();
        loader.load(bundle, createJmsDestinationXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument())
        );

        assertFalse(bundle.getJmsDestinations().isEmpty());
        assertEquals(1, bundle.getJmsDestinations().size());

        JmsDestination jmsDestination = bundle.getJmsDestinations().get("my-jms-endpoint");
        assertNotNull(jmsDestination);
        assertEquals("my-jms-endpoint", jmsDestination.getName());
        assertEquals("id-1", jmsDestination.getId());
        assertTrue(jmsDestination.isInbound());
        assertFalse(jmsDestination.isTemplate());
        assertEquals("TIBCO EMS", jmsDestination.getProviderType());
        assertEquals("com.tibco.tibjms.naming.TibjmsInitialContextFactory", jmsDestination.getInitialContextFactoryClassName());
        assertEquals("tibjmsnaming://machinename:7222", jmsDestination.getJndiUrl());
        assertEquals("my-jndi-username", jmsDestination.getJndiUsername());
        assertEquals("my-jndi-password", jmsDestination.getJndiPassword());
        assertNull(jmsDestination.getJndiPasswordRef());

        assertPropertiesContent(ImmutableMap.of(
                "additional-jndi-prop-name-1", "additional-jndi-prop-val-1",
                "additional-jndi-prop-name-2", "additional-jndi-prop-val-2"),
                jmsDestination.getJndiProperties());
        
        assertEquals("Queue", jmsDestination.getDestinationType());
        assertEquals("my-qcf-name", jmsDestination.getConnectionFactoryName());
        assertEquals("my-jms-destination-name", jmsDestination.getDestinationName());
        assertEquals("my-destination-username", jmsDestination.getDestinationUsername());
        assertEquals("my-destination-password", jmsDestination.getDestinationPassword());
        assertNull(jmsDestination.getDestinationPasswordRef());
    }
    
    private static Element createJmsDestinationXml(Document document) {
        final Map<String, Object> jmsDestinationDetailProperties = new HashMap<>();
        jmsDestinationDetailProperties.put(DESTINATION_TYPE, "Queue");
        jmsDestinationDetailProperties.put(PROPERTY_USERNAME, "my-destination-username");
        jmsDestinationDetailProperties.put(PROPERTY_PASSWORD, "my-destination-password");

        final Map<String, Object> jmsConnectionProperties = new HashMap<>();
        jmsConnectionProperties.put(JNDI_INITIAL_CONTEXT_FACTORY_CLASSNAME, "com.tibco.tibjms.naming.TibjmsInitialContextFactory");
        jmsConnectionProperties.put(JNDI_PROVIDER_URL, "tibjmsnaming://machinename:7222");
        jmsConnectionProperties.put(CONNECTION_FACTORY_NAME, "my-qcf-name");
        jmsConnectionProperties.put(PROPERTY_USERNAME, "my-destination-username");
        jmsConnectionProperties.put(PROPERTY_PASSWORD, "my-destination-password");
        
        final Map<String, Object> contextPropertiesTemplate = new HashMap<>();
        contextPropertiesTemplate.put("additional-jndi-prop-name-1", "additional-jndi-prop-val-1");
        contextPropertiesTemplate.put("additional-jndi-prop-name-2", "additional-jndi-prop-val-2");
        contextPropertiesTemplate.put("com.l7tech.server.jms.soapAction.msgPropName", "listProducts");
        contextPropertiesTemplate.put("com.l7tech.server.jms.prop.foobar", "reserved");
        contextPropertiesTemplate.put(JNDI_USERNAME, "my-jndi-username");
        contextPropertiesTemplate.put(JNDI_PASSWORD,"my-jndi-password");
        
        Element jmsDestinationEle = createElementWithAttributesAndChildren(
                document,
                JMS_DESTINATION,
                ImmutableMap.of(ATTRIBUTE_ID, "id-1"),
                createElementWithAttributesAndChildren(
                        document,
                        JMS_DESTINATION_DETAIL,
                        ImmutableMap.of(ATTRIBUTE_ID, "id-1"),
                        createElementWithTextContent(document, NAME, "my-jms-endpoint"),
                        createElementWithTextContent(document, JMS_DESTINATION_NAME, "my-jms-destination-name"),
                        createElementWithTextContent(document, INBOUND, "true"),
                        createElementWithTextContent(document, TEMPLATE, "false"),
                        buildPropertiesElement(jmsDestinationDetailProperties, document)
                ),
                createElementWithAttributesAndChildren(
                        document,
                        JMS_CONNECTION,
                        ImmutableMap.of(ATTRIBUTE_ID, "id-2"),
                        createElementWithTextContent(document, JMS_PROVIDER_TYPE, "TIBCO EMS"),
                        createElementWithTextContent(document, TEMPLATE, "false"),
                        buildPropertiesElement(jmsConnectionProperties, document),
                        buildPropertiesElement(contextPropertiesTemplate, document, CONTEXT_PROPERTIES_TEMPLATE)
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
