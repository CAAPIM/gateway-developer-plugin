/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.ConnectionPoolingSettings;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.MessageFormat.BYTES;
import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.PoolingType.CONNECTION;
import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.ReplyType.SPECIFIED_QUEUE;
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
    void testLoadInboundJmsDestination() {
        loadJmsDestination(true);
    }
    
    @Test 
    void testLoadOutboundJmsDestination() {
        loadJmsDestination(false);
    }
    
    private void loadJmsDestination(boolean isInbound) {
        Bundle bundle = new Bundle();
        loader.load(bundle, createJmsDestinationXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), isInbound)
        );

        assertFalse(bundle.getJmsDestinations().isEmpty());
        assertEquals(1, bundle.getJmsDestinations().size());

        JmsDestination jmsDestination = bundle.getJmsDestinations().get("my-jms-endpoint");
        assertNotNull(jmsDestination);
        assertEquals("my-jms-endpoint", jmsDestination.getName());
        assertEquals("id-1", jmsDestination.getId());
        assertEquals(isInbound, jmsDestination.isInbound());
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
        
        assertEquals(JmsDestination.DestinationType.QUEUE, jmsDestination.getDestinationType());
        assertEquals("my-qcf-name", jmsDestination.getConnectionFactoryName());
        assertEquals("my-jms-destination-name", jmsDestination.getDestinationName());
        assertEquals("my-destination-username", jmsDestination.getDestinationUsername());
        assertEquals("my-destination-password", jmsDestination.getDestinationPassword());
        assertNull(jmsDestination.getDestinationPasswordRef());
        
        if (isInbound) {
            checkInboundJmsDestination(jmsDestination);
        } else {
            checkOutboundJmsDestination(jmsDestination);
        }
    }
    
    private void checkInboundJmsDestination(JmsDestination jmsDestination) {
        
    }

    private void checkOutboundJmsDestination(JmsDestination jmsDestination) {
        assertFalse(jmsDestination.isInbound());
        assertNull(jmsDestination.getInboundDetail());

        OutboundJmsDestinationDetail outboundDetail = jmsDestination.getOutboundDetail();
        assertNotNull(outboundDetail);
        
        assertEquals(SPECIFIED_QUEUE, outboundDetail.getReplyType());
        assertEquals("my-reply-Q", outboundDetail.getReplyToQueueName());
        assertFalse(outboundDetail.useRequestCorrelationId());
        assertEquals(BYTES, outboundDetail.getMessageFormat());


        assertEquals(CONNECTION, outboundDetail.getPoolingType());
        ConnectionPoolingSettings connectionPoolingSettings = outboundDetail.getConnectionPoolingSettings();
        assertNotNull(connectionPoolingSettings);
        assertNull(outboundDetail.getSessionPoolingSettings());
                
        assertEquals(new Integer(10), connectionPoolingSettings.getSize());
        assertEquals(new Integer(5), connectionPoolingSettings.getMinIdle());
        assertEquals(new Integer(10000), connectionPoolingSettings.getMaxWaitMs());
    }
    
    private static Element createJmsDestinationXml(Document document, boolean isInbound) {
        final Map<String, Object> jmsDestinationDetailProps = new HashMap<>();
        jmsDestinationDetailProps.put(DESTINATION_TYPE, "Queue");
        jmsDestinationDetailProps.put(PROPERTY_USERNAME, "my-destination-username");
        jmsDestinationDetailProps.put(PROPERTY_PASSWORD, "my-destination-password");

        final Map<String, Object> jmsConnectionProps = new HashMap<>();
        jmsConnectionProps.put(JNDI_INITIAL_CONTEXT_FACTORY_CLASSNAME, "com.tibco.tibjms.naming.TibjmsInitialContextFactory");
        jmsConnectionProps.put(JNDI_PROVIDER_URL, "tibjmsnaming://machinename:7222");
        jmsConnectionProps.put(CONNECTION_FACTORY_NAME, "my-qcf-name");
        jmsConnectionProps.put(PROPERTY_USERNAME, "my-destination-username");
        jmsConnectionProps.put(PROPERTY_PASSWORD, "my-destination-password");
        
        final Map<String, Object> contextPropertiesTemplateProps = new HashMap<>();
        contextPropertiesTemplateProps.put("additional-jndi-prop-name-1", "additional-jndi-prop-val-1");
        contextPropertiesTemplateProps.put("additional-jndi-prop-name-2", "additional-jndi-prop-val-2");
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.soapAction.msgPropName", "listProducts");
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.prop.foobar", "reserved");
        contextPropertiesTemplateProps.put(JNDI_USERNAME, "my-jndi-username");
        contextPropertiesTemplateProps.put(JNDI_PASSWORD,"my-jndi-password");

        if (isInbound) {
            populateInboundJmsDestinationXml();
        } else {
            populateOutboundJmsDestinationXml(jmsDestinationDetailProps, contextPropertiesTemplateProps);
        }
        
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
                        createElementWithTextContent(document, INBOUND, isInbound),
                        createElementWithTextContent(document, TEMPLATE, false), 
                        buildPropertiesElement(jmsDestinationDetailProps, document)
                ),
                createElementWithAttributesAndChildren(
                        document,
                        JMS_CONNECTION,
                        ImmutableMap.of(ATTRIBUTE_ID, "id-2"),
                        createElementWithTextContent(document, JMS_PROVIDER_TYPE, "TIBCO EMS"),
                        createElementWithTextContent(document, TEMPLATE, false),
                        buildPropertiesElement(jmsConnectionProps, document),
                        buildPropertiesElement(contextPropertiesTemplateProps, document, CONTEXT_PROPERTIES_TEMPLATE)
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
    
    private static void populateInboundJmsDestinationXml() {
        
    }
    
    private static void populateOutboundJmsDestinationXml(Map<String, Object> jmsDestinationDetailProps, Map<String, Object> contextPropertiesTemplateProps) {
        jmsDestinationDetailProps.put(REPLY_TYPE, "REPLY_TO_OTHER");
        jmsDestinationDetailProps.put(REPLY_QUEUE_NAME, "my-reply-Q");
        jmsDestinationDetailProps.put(USE_REQUEST_CORRELATION_ID, Boolean.FALSE.toString());
        jmsDestinationDetailProps.put(OUTBOUND_MESSAGE_TYPE, "ALWAYS_BINARY");

        contextPropertiesTemplateProps.put(CONNECTION_POOL_ENABLED, Boolean.TRUE.toString());
        contextPropertiesTemplateProps.put(CONNECTION_POOL_SIZE, 10);
        contextPropertiesTemplateProps.put(CONNECTION_POOL_MIN_IDLE, 5);
        contextPropertiesTemplateProps.put(CONNECTION_POOL_MAX_WAIT, 10000);
    }
}
