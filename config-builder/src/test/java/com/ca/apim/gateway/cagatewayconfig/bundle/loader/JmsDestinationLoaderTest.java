/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.ServiceResolutionSettings;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.ConnectionPoolingSettings;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.AcknowledgeType.*;
import static com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.ContentTypeSource.FREE_FORM;
import static com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination.*;
import static com.ca.apim.gateway.cagatewayconfig.beans.JmsDestinationDetail.ReplyType.SPECIFIED_QUEUE;
import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.MessageFormat.BYTES;
import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.PoolingType.CONNECTION;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static org.junit.jupiter.api.Assertions.*;

class JmsDestinationLoaderTest {
    
    @Test
    void testLoadInbound() {
        JmsDestinationLoader loader = new JmsDestinationLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createJmsDestinationXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true, PROVIDER_TYPE_GENERIC)
        );
        
        assertFalse(bundle.getJmsDestinations().isEmpty());
        assertEquals(1, bundle.getJmsDestinations().size());

        JmsDestination jmsDestination = bundle.getJmsDestinations().get("my-jms-endpoint");
        verifyCommonSettings(jmsDestination);
        verifyGenericProviderSettings(jmsDestination);
        verifyInboundSettings(jmsDestination);
    }
    
    @Test
    void testLoadOutbound() {
        JmsDestinationLoader loader = new JmsDestinationLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createJmsDestinationXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), false, PROVIDER_TYPE_GENERIC)
        );
        
        assertFalse(bundle.getJmsDestinations().isEmpty());
        assertEquals(1, bundle.getJmsDestinations().size());

        JmsDestination jmsDestination = bundle.getJmsDestinations().get("my-jms-endpoint");
        verifyCommonSettings(jmsDestination);
        verifyGenericProviderSettings(jmsDestination);
        verifyOutboundSettings(jmsDestination);
    }

    @Test
    void testLoadTibcoEmsProvider() {
        JmsDestinationLoader loader = new JmsDestinationLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createJmsDestinationXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true, PROVIDER_TYPE_TIBCO_EMS)
        );

        assertFalse(bundle.getJmsDestinations().isEmpty());
        assertEquals(1, bundle.getJmsDestinations().size());

        JmsDestination jmsDestination = bundle.getJmsDestinations().get("my-jms-endpoint");
        verifyCommonSettings(jmsDestination);
        verifyInboundSettings(jmsDestination);
        verifyTibcoEmsProviderSettings(jmsDestination);
    }
    
    @Test
    void testLoadWebShpereMqOverLdapProvider() {
        JmsDestinationLoader loader = new JmsDestinationLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createJmsDestinationXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true, PROVIDER_TYPE_WEBSPHERE_MQ_OVER_LDAP)
        );

        assertFalse(bundle.getJmsDestinations().isEmpty());
        assertEquals(1, bundle.getJmsDestinations().size());

        JmsDestination jmsDestination = bundle.getJmsDestinations().get("my-jms-endpoint");
        verifyCommonSettings(jmsDestination);
        verifyInboundSettings(jmsDestination);
        verifyWebShpereMqOverLdapProviderSettings(jmsDestination);
    }
    
    private static Element createJmsDestinationXml(Document document, boolean isInbound, String providerType) {
        final Map<String, Object> jmsDestinationDetailProps = new HashMap<>();
        jmsDestinationDetailProps.put(DESTINATION_TYPE, "Queue");
        jmsDestinationDetailProps.put(PROPERTY_USERNAME, "my-destination-username");
        jmsDestinationDetailProps.put(PROPERTY_PASSWORD, "my-destination-password");

        final Map<String, Object> jmsConnectionProps = new HashMap<>();
        jmsConnectionProps.put(JNDI_INITIAL_CONTEXT_FACTORY_CLASSNAME, "my-jndi-initial-context-factory-classname");
        jmsConnectionProps.put(JNDI_PROVIDER_URL, "my-jndi-provider-url");
        jmsConnectionProps.put(CONNECTION_FACTORY_NAME, "my-qcf-name");
        jmsConnectionProps.put(PROPERTY_USERNAME, "my-destination-username");
        jmsConnectionProps.put(PROPERTY_PASSWORD, "my-destination-password");
        
        final Map<String, Object> contextPropertiesTemplateProps = new HashMap<>();
        contextPropertiesTemplateProps.put("additional-jndi-prop-name-1", "additional-jndi-prop-val-1");
        contextPropertiesTemplateProps.put("additional-jndi-prop-name-2", "additional-jndi-prop-val-2");
        contextPropertiesTemplateProps.put(JNDI_USERNAME, "my-jndi-username");
        contextPropertiesTemplateProps.put(JNDI_PASSWORD,"my-jndi-password");

        if (isInbound) {
            loadInboundJmsDestinationXml(jmsDestinationDetailProps, contextPropertiesTemplateProps);
        } else {
            loadOutboundJmsDestinationXml(jmsDestinationDetailProps, contextPropertiesTemplateProps);
        }
        
        if(PROVIDER_TYPE_TIBCO_EMS.equals(providerType)) {
            loadTibcoEmsProviderXml(contextPropertiesTemplateProps);
        } else if(PROVIDER_TYPE_WEBSPHERE_MQ_OVER_LDAP.equals(providerType)) {
            loadWebsphereMqOverLdapProviderXml(contextPropertiesTemplateProps);
        }
        
        Element jmsDestinationDetailEle = createElementWithAttributesAndChildren(
                document,
                JMS_DESTINATION_DETAIL,
                ImmutableMap.of(ATTRIBUTE_ID, "id-1"),
                createElementWithTextContent(document, NAME, "my-jms-endpoint"),
                createElementWithTextContent(document, JMS_DESTINATION_NAME, "my-jms-destination-name"),
                createElementWithTextContent(document, INBOUND, isInbound),
                createElementWithTextContent(document, ENABLED, true),
                createElementWithTextContent(document, TEMPLATE, false),
                buildPropertiesElement(jmsDestinationDetailProps, document)
        );
        
        Element jmsConnectionEle = createElementWithAttributesAndChildren(
                document,
                JMS_CONNECTION,
                ImmutableMap.of(ATTRIBUTE_ID, "id-2")
        );
        if (providerType != null) {
            jmsConnectionEle.appendChild(createElementWithTextContent(document, JMS_PROVIDER_TYPE, providerType));
        }
        jmsConnectionEle.appendChild(createElementWithTextContent(document, TEMPLATE, false));
        jmsConnectionEle.appendChild(buildPropertiesElement(jmsConnectionProps, document));
        jmsConnectionEle.appendChild(buildPropertiesElement(contextPropertiesTemplateProps, document, CONTEXT_PROPERTIES_TEMPLATE));
        
        Element jmsDestinationEle = createElementWithAttributesAndChildren(
                document,
                JMS_DESTINATION,
                ImmutableMap.of(ATTRIBUTE_ID, "id-1"),
                jmsDestinationDetailEle,
                jmsConnectionEle
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
    
    private static void loadInboundJmsDestinationXml(
            Map<String, Object> jmsDestinationDetailProps,
            Map<String, Object> contextPropertiesTemplateProps) {

        jmsDestinationDetailProps.put(INBOUND_ACKNOWLEDGEMENT_TYPE, "ON_COMPLETION");
        jmsDestinationDetailProps.put(REPLY_TYPE, "REPLY_TO_OTHER");
        jmsDestinationDetailProps.put(REPLY_QUEUE_NAME, "my-reply-Q");
        jmsDestinationDetailProps.put(USE_REQUEST_CORRELATION_ID, Boolean.FALSE);

        contextPropertiesTemplateProps.put(IS_HARDWIRED_SERVICE, "true");
        contextPropertiesTemplateProps.put(HARDWIRED_SERVICE_ID, "/folder1/service1");
        contextPropertiesTemplateProps.put(SOAP_ACTION_MSG_PROP_NAME, "my-jms-soap-action-prop-name");
        contextPropertiesTemplateProps.put(CONTENT_TYPE_SOURCE, "FREE_FORM");
        contextPropertiesTemplateProps.put(CONTENT_TYPE_VALUE, "text/yaml");
        
        jmsDestinationDetailProps.put(INBOUND_FAILURE_QUEUE_NAME, "my-failure-Q");
        contextPropertiesTemplateProps.put(DEDICATED_CONSUMER_CONNECTION_SIZE, "5");
        jmsDestinationDetailProps.put(INBOUND_MAX_SIZE, 2048L);
    }

    private static void loadOutboundJmsDestinationXml(
            Map<String, Object> jmsDestinationDetailProps,
            Map<String, Object> contextPropertiesTemplateProps) {

        jmsDestinationDetailProps.put(REPLY_TYPE, "REPLY_TO_OTHER");
        jmsDestinationDetailProps.put(REPLY_QUEUE_NAME, "my-reply-Q");
        jmsDestinationDetailProps.put(USE_REQUEST_CORRELATION_ID, Boolean.FALSE);
        jmsDestinationDetailProps.put(OUTBOUND_MESSAGE_TYPE, "ALWAYS_BINARY");

        contextPropertiesTemplateProps.put(CONNECTION_POOL_ENABLED, Boolean.TRUE.toString());
        contextPropertiesTemplateProps.put(CONNECTION_POOL_SIZE, "10");
        contextPropertiesTemplateProps.put(CONNECTION_POOL_MIN_IDLE, "5");
        contextPropertiesTemplateProps.put(CONNECTION_POOL_MAX_WAIT, "10000");
    }

    private static void loadTibcoEmsProviderXml(Map<String, Object> contextPropertiesTemplateProps) {
        // JNDI
        contextPropertiesTemplateProps.put("com.tibco.tibjms.naming.security_protocol", "ssl");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.naming.ssl_auth_only", "com.l7tech.server.jms.prop.boolean.true");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.naming.ssl_enable_verify_host", "com.l7tech.server.jms.prop.boolean.true");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.naming.ssl_trusted_certs","com.l7tech.server.jms.prop.trustedcert.listx509");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.naming.ssl_enable_verify_hostname", "com.l7tech.server.jms.prop.boolean.true");
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.prop.jndi.ssgKeyAlias", "key1");
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.prop.jndi.ssgKeystoreId", "00000000000000000000000000000002");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.naming.ssl_identity", "com.l7tech.server.jms.prop.keystore 00000000000000000000000000000002    key1");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.naming.ssl_password", "com.l7tech.server.jms.prop.keystore.password    00000000000000000000000000000002    key1");

        // Destination
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.prop.customizer.class", "com.l7tech.server.transport.jms.prov.TibcoConnectionFactoryCustomizer");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.ssl.auth_only", "com.l7tech.server.jms.prop.boolean.true");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.ssl.enable_verify_host", "com.l7tech.server.jms.prop.boolean.true");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.ssl.trusted_certs", "com.l7tech.server.jms.prop.trustedcert.listx509");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.ssl.enable_verify_hostname", "com.l7tech.server.jms.prop.boolean.true");
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.prop.queue.ssgKeyAlias", "key2");
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.prop.queue.ssgKeystoreId", "00000000000000000000000000000002");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.ssl.identity", "com.l7tech.server.jms.prop.keystore.bytes   00000000000000000000000000000002    key2");
        contextPropertiesTemplateProps.put("com.tibco.tibjms.ssl.password", "com.l7tech.server.jms.prop.keystore.password    00000000000000000000000000000002    key2");
    }
    
    private static void loadWebsphereMqOverLdapProviderXml(Map<String, Object> contextPropertiesTemplateProps) {
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.prop.customizer.class","com.l7tech.server.transport.jms.prov.MQSeriesCustomizer");
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.prop.queue.useClientAuth", Boolean.TRUE.toString());
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.prop.queue.ssgKeyAlias", "key1");
        contextPropertiesTemplateProps.put("com.l7tech.server.jms.prop.queue.ssgKeystoreId", "00000000000000000000000000000002");
    }
    
    // Check settings common to all provider types.
    private static void verifyCommonSettings(JmsDestination jmsDestination) {
        assertNotNull(jmsDestination);
        assertEquals("my-jms-endpoint", jmsDestination.getName());
        assertEquals("id-1", jmsDestination.getId());

        assertEquals("my-jndi-initial-context-factory-classname", jmsDestination.getInitialContextFactoryClassName());
        assertEquals("my-jndi-provider-url", jmsDestination.getJndiUrl());
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
    }

    private static void verifyGenericProviderSettings(JmsDestination jmsDestination) {
        assertEquals(PROVIDER_TYPE_GENERIC, jmsDestination.getProviderType());
        assertNull(jmsDestination.getAdditionalProperties());
    }

    private static void verifyTibcoEmsProviderSettings(JmsDestination jmsDestination) {
        assertEquals(PROVIDER_TYPE_TIBCO_EMS, jmsDestination.getProviderType());
        
        Map<String, Object> expectedAdditionalProps = new HashMap<String, Object>() {{ 
            put("com.tibco.tibjms.naming.security_protocol", "ssl");
            put("com.tibco.tibjms.naming.ssl_auth_only", "com.l7tech.server.jms.prop.boolean.true");
            put("com.tibco.tibjms.naming.ssl_enable_verify_host", "com.l7tech.server.jms.prop.boolean.true");
            put("com.tibco.tibjms.naming.ssl_trusted_certs", "com.l7tech.server.jms.prop.trustedcert.listx509");
            put("com.tibco.tibjms.naming.ssl_enable_verify_hostname", "com.l7tech.server.jms.prop.boolean.true");
            put("com.l7tech.server.jms.prop.jndi.ssgKeyAlias", "key1");
            put("com.l7tech.server.jms.prop.customizer.class", "com.l7tech.server.transport.jms.prov.TibcoConnectionFactoryCustomizer");
            put("com.tibco.tibjms.ssl.auth_only", "com.l7tech.server.jms.prop.boolean.true");
            put("com.tibco.tibjms.ssl.enable_verify_host", "com.l7tech.server.jms.prop.boolean.true");
            put("com.tibco.tibjms.ssl.trusted_certs", "com.l7tech.server.jms.prop.trustedcert.listx509");
            put("com.tibco.tibjms.ssl.enable_verify_hostname", "com.l7tech.server.jms.prop.boolean.true");
            put("com.l7tech.server.jms.prop.queue.ssgKeyAlias", "key2");
        }};
        
        assertPropertiesContent(expectedAdditionalProps, jmsDestination.getAdditionalProperties());
    }

    private static void verifyWebShpereMqOverLdapProviderSettings(JmsDestination jmsDestination) {
        assertEquals(PROVIDER_TYPE_WEBSPHERE_MQ_OVER_LDAP, jmsDestination.getProviderType());
        assertPropertiesContent(ImmutableMap.of(
                "com.l7tech.server.jms.prop.customizer.class","com.l7tech.server.transport.jms.prov.MQSeriesCustomizer", 
                "com.l7tech.server.jms.prop.queue.useClientAuth", "true",
                "com.l7tech.server.jms.prop.queue.ssgKeyAlias", "key1"),
                jmsDestination.getAdditionalProperties());
    }
    
    private static void verifyInboundSettings(JmsDestination jmsDestination) {
        assertNull(jmsDestination.getOutboundDetail());

        InboundJmsDestinationDetail inboundDetail = jmsDestination.getInboundDetail();
        assertNotNull(inboundDetail);

        assertEquals(ON_COMPLETION, inboundDetail.getAcknowledgeType());
        assertEquals(SPECIFIED_QUEUE, inboundDetail.getReplyType());
        assertEquals("my-reply-Q", inboundDetail.getReplyToQueueName());
        assertFalse(inboundDetail.isUseRequestCorrelationId());

        ServiceResolutionSettings serviceResolutionSettings = inboundDetail.getServiceResolutionSettings();
        assertNotNull(serviceResolutionSettings);
        assertEquals("/folder1/service1", serviceResolutionSettings.getServiceRef());
        assertEquals("my-jms-soap-action-prop-name", serviceResolutionSettings.getSoapActionMessagePropertyName());
        assertEquals(FREE_FORM, serviceResolutionSettings.getContentTypeSource());
        assertEquals("text/yaml", serviceResolutionSettings.getContentType());

        assertEquals("my-failure-Q", inboundDetail.getFailureQueueName());
        assertEquals(new Integer(5), inboundDetail.getNumOfConsumerConnections());
        assertEquals(new Long(2048L), inboundDetail.getMaxMessageSizeBytes());
    }

    private void verifyOutboundSettings(JmsDestination jmsDestination) {
        assertNull(jmsDestination.getInboundDetail());

        OutboundJmsDestinationDetail outboundDetail = jmsDestination.getOutboundDetail();
        assertNotNull(outboundDetail);

        assertFalse(outboundDetail.isTemplate());
        assertEquals(SPECIFIED_QUEUE, outboundDetail.getReplyType());
        assertEquals("my-reply-Q", outboundDetail.getReplyToQueueName());
        assertFalse(outboundDetail.isUseRequestCorrelationId());
        assertEquals(BYTES, outboundDetail.getMessageFormat());

        assertEquals(CONNECTION, outboundDetail.getPoolingType());
        ConnectionPoolingSettings connectionPoolingSettings = outboundDetail.getConnectionPoolingSettings();
        assertNotNull(connectionPoolingSettings);
        assertNull(outboundDetail.getSessionPoolingSettings());

        assertEquals(new Integer(10), connectionPoolingSettings.getSize());
        assertEquals(new Integer(5), connectionPoolingSettings.getMinIdle());
        assertEquals(new Integer(10000), connectionPoolingSettings.getMaxWaitMs());
    }
}
