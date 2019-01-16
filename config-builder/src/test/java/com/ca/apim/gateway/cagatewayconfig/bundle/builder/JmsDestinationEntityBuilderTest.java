/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.AcknowledgeType.ON_TAKE;
import static com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.ContentTypeSource.JMS_PROPERTY;
import static com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination.PROVIDER_TYPE_TIBCO_EMS;
import static com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination.PROVIDER_TYPE_WEBSPHERE_MQ_OVER_LDAP;
import static com.ca.apim.gateway.cagatewayconfig.beans.JmsDestinationDetail.ReplyType.*;
import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.MessageFormat.BYTES;
import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.PoolingType.CONNECTION;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.junit.jupiter.api.Assertions.*;

class JmsDestinationEntityBuilderTest {

    private static final IdGenerator ID_GENERATOR = new IdGenerator();

    @Test
    void testBuildFromEmptyBundle_noJmsDestination() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }
    
    @Test
    void checkJmsDestination_Inbound() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of("my-jms-endpoint", buildInbound().build()));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, entities.size());
        verifyInbound(entities.get(0));
    }

    @Test
    void checkJmsDestination_Outbound() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of("my-jms-endpoint", buildOutbound().build()));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, entities.size());
        verifyOutbound(entities.get(0));
    }

    @Test
    void checkJmsDestination_TibcoEmsProvider() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of("my-jms-endpoint", buildTibcoEmsProvider(bundle).build()));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, entities.size());
        verifyTibcoEmsProvider(entities.get(0));
    }

    @Test
    void checkJmsDestination_WebShpereOverLdapProvider() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of("my-jms-endpoint", buildWebShpereMqOverLdapProvider(bundle).build()));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, entities.size());
        verifyWebShpereMqOverLdapProvider(entities.get(0));
    }
    
    // (kpak): Add tests for missing stored password, missing private key, missing service.
    @NotNull
    private static JmsDestination.Builder buildCommon() {
        return new JmsDestination.Builder()
                .name("my-jms-endpoint")
                .id("id-1")
                .initialContextFactoryClassName("my-jndi-initial-context-factory-classname")
                .jndiUrl("my-jndi-url")
                .jndiUsername("my-jndi-username")
                .jndiPassword("my-plaintext-jndi-password")
                .jndiProperties(new HashMap<String, Object>() {{
                    put("additional-jndi-prop-name-1", "additional-jndi-prop-val-1");
                    put("additional-jndi-prop-name-2", "additional-jndi-prop-val-2");
                }})
                .destinationType(JmsDestination.DestinationType.TOPIC)
                .connectionFactoryName("my-qcf-name")
                .destinationName("my-dest-name")
                .destinationUsername("my-destination-username")
                .destinationPassword("my-plaintext-destination-password");
    }

    private static void verifyCommon(Entity entity) {
        assertNotNull(entity);
        
        Element jmsDestinationEle = entity.getXml();
        assertNotNull(jmsDestinationEle);

        Element jmsDestinationDetailEle = getSingleChildElement(jmsDestinationEle, JMS_DESTINATION_DETAIL);
        assertNotNull(jmsDestinationDetailEle);

        Element jmsConnectionEle = getSingleChildElement(jmsDestinationEle, JMS_CONNECTION);
        assertNotNull(jmsConnectionEle);
        
        assertEquals(entity.getId(), jmsDestinationEle.getAttribute(ATTRIBUTE_ID));
        assertEquals(entity.getId(), jmsDestinationDetailEle.getAttribute(ATTRIBUTE_ID));
        assertEquals("my-jms-endpoint", getSingleChildElementTextContent(jmsDestinationDetailEle, NAME));
        assertEquals("true", getSingleChildElementTextContent(jmsDestinationDetailEle, ENABLED));

        Map<String, Object> jmsDestinationDetailProps = 
                mapPropertiesElements(getSingleChildElement(jmsDestinationDetailEle, PROPERTIES, false), PROPERTIES);
        assertNotNull(jmsDestinationDetailProps);
        
        Map<String, Object> jmsConnectionProps = 
                mapPropertiesElements(getSingleChildElement(jmsConnectionEle, PROPERTIES, false), PROPERTIES);
        assertNotNull(jmsConnectionProps);
        
        Map<String, Object> contextPropertiesTemplateProps = 
                mapPropertiesElements(getSingleChildElement(jmsConnectionEle, CONTEXT_PROPERTIES_TEMPLATE, false), CONTEXT_PROPERTIES_TEMPLATE);
        assertNotNull(contextPropertiesTemplateProps);

        assertEquals("my-jndi-initial-context-factory-classname", jmsConnectionProps.remove(JNDI_INITIAL_CONTEXT_FACTORY_CLASSNAME));
        assertEquals("my-jndi-url", jmsConnectionProps.remove(JNDI_PROVIDER_URL));
        assertEquals("my-jndi-username", contextPropertiesTemplateProps.remove(JNDI_USERNAME));
        assertEquals("my-plaintext-jndi-password", contextPropertiesTemplateProps.remove(JNDI_PASSWORD));
        assertEquals("additional-jndi-prop-val-1", contextPropertiesTemplateProps.remove("additional-jndi-prop-name-1"));
        assertEquals("additional-jndi-prop-val-2", contextPropertiesTemplateProps.remove("additional-jndi-prop-name-2"));

        assertEquals("Topic", jmsDestinationDetailProps.remove(DESTINATION_TYPE));
        assertEquals("my-dest-name", getSingleChildElementTextContent(jmsDestinationDetailEle, JMS_DESTINATION_NAME));
        assertEquals("my-destination-username", jmsDestinationDetailProps.remove(PROPERTY_USERNAME));
        assertEquals("my-plaintext-destination-password", jmsDestinationDetailProps.remove(PROPERTY_PASSWORD));
        assertEquals("my-destination-username", jmsConnectionProps.remove(PROPERTY_USERNAME));
        assertEquals("my-plaintext-destination-password", jmsConnectionProps.remove(PROPERTY_PASSWORD));
    }
    
    @NotNull
    private static JmsDestination.Builder buildInbound() {
        JmsDestination.Builder builder = buildCommon();
        
        InboundJmsDestinationDetail inboundDetail = new InboundJmsDestinationDetail();
        inboundDetail.setAcknowledgeType(ON_TAKE);
        inboundDetail.setReplyType(AUTOMATIC);

        InboundJmsDestinationDetail.ServiceResolutionSettings serviceResolutionSettings = new InboundJmsDestinationDetail.ServiceResolutionSettings();
        serviceResolutionSettings.setContentTypeSource(JMS_PROPERTY);
        serviceResolutionSettings.setContentType("my-content-type-jms-prop");
        inboundDetail.setServiceResolutionSettings(serviceResolutionSettings);

        builder.inboundDetail(inboundDetail);
        return builder;
    }

    private static void verifyInbound(Entity entity) {
        verifyCommon(entity);

        assertNotNull(entity);
        Element jmsDestinationEle = entity.getXml();
        assertNotNull(jmsDestinationEle);

        Element jmsDestinationDetailEle = getSingleChildElement(jmsDestinationEle, JMS_DESTINATION_DETAIL);
        assertNotNull(jmsDestinationDetailEle);

        Element jmsConnectionEle = getSingleChildElement(jmsDestinationEle, JMS_CONNECTION);
        assertNotNull(jmsConnectionEle);
        
        Map<String, Object> jmsDestinationDetailProps =
                mapPropertiesElements(getSingleChildElement(jmsDestinationDetailEle, PROPERTIES, false), PROPERTIES);
        assertNotNull(jmsDestinationDetailProps);

        Map<String, Object> jmsConnectionProps =
                mapPropertiesElements(getSingleChildElement(jmsConnectionEle, PROPERTIES, false), PROPERTIES);
        assertNotNull(jmsConnectionProps);

        Map<String, Object> contextPropertiesTemplateProps =
                mapPropertiesElements(getSingleChildElement(jmsConnectionEle, CONTEXT_PROPERTIES_TEMPLATE, false), CONTEXT_PROPERTIES_TEMPLATE);
        assertNotNull(contextPropertiesTemplateProps);

        assertEquals("true", getSingleChildElementTextContent(jmsDestinationDetailEle, INBOUND));
        
        assertEquals("AUTOMATIC", jmsDestinationDetailProps.remove(INBOUND_ACKNOWLEDGEMENT_TYPE));
        assertEquals("AUTOMATIC", jmsDestinationDetailProps.remove(REPLY_TYPE));
        assertEquals("false", contextPropertiesTemplateProps.remove(IS_HARDWIRED_SERVICE));
        assertEquals("com.l7tech.server.jms.prop.contentType.header", contextPropertiesTemplateProps.remove(CONTENT_TYPE_SOURCE));
        assertEquals("my-content-type-jms-prop", contextPropertiesTemplateProps.remove(CONTENT_TYPE_VALUE));
        
        assertEquals(-1L, jmsDestinationDetailProps.remove(INBOUND_MAX_SIZE));
        assertEquals("true", contextPropertiesTemplateProps.remove(IS_DEDICATED_CONSUMER_CONNECTION));
        assertEquals("1", contextPropertiesTemplateProps.remove(DEDICATED_CONSUMER_CONNECTION_SIZE));
    }
    
    @NotNull
    private static JmsDestination.Builder buildOutbound() {
        OutboundJmsDestinationDetail outboundDetail = new OutboundJmsDestinationDetail();
        outboundDetail.setIsTemplate(false);
        outboundDetail.setReplyType(NO_REPLY);
        outboundDetail.setMessageFormat(BYTES);
        outboundDetail.setPoolingType(CONNECTION);
        
        return buildCommon().outboundDetail(outboundDetail);
    }

    private static void verifyOutbound(Entity entity) {
        verifyCommon(entity);

        assertNotNull(entity);
        Element jmsDestinationEle = entity.getXml();
        assertNotNull(jmsDestinationEle);

        Element jmsDestinationDetailEle = getSingleChildElement(jmsDestinationEle, JMS_DESTINATION_DETAIL);
        assertNotNull(jmsDestinationDetailEle);

        Element jmsConnectionEle = getSingleChildElement(jmsDestinationEle, JMS_CONNECTION);
        assertNotNull(jmsConnectionEle);

        assertEquals("false", getSingleChildElementTextContent(jmsDestinationDetailEle, INBOUND));
        
        Map<String, Object> jmsDestinationDetailProps =
                mapPropertiesElements(getSingleChildElement(jmsDestinationDetailEle, PROPERTIES, false), PROPERTIES);
        assertNotNull(jmsDestinationDetailProps);

        Map<String, Object> jmsConnectionProps =
                mapPropertiesElements(getSingleChildElement(jmsConnectionEle, PROPERTIES, false), PROPERTIES);
        assertNotNull(jmsConnectionProps);

        Map<String, Object> contextPropertiesTemplateProps =
                mapPropertiesElements(getSingleChildElement(jmsConnectionEle, CONTEXT_PROPERTIES_TEMPLATE, false), CONTEXT_PROPERTIES_TEMPLATE);
        assertNotNull(contextPropertiesTemplateProps);

        assertEquals("false", getSingleChildElementTextContent(jmsDestinationDetailEle, TEMPLATE));
        assertEquals("NO_REPLY", jmsDestinationDetailProps.remove(REPLY_TYPE));
        assertEquals("ALWAYS_BINARY", jmsDestinationDetailProps.remove(OUTBOUND_MESSAGE_TYPE));
        assertEquals("true", contextPropertiesTemplateProps.remove(CONNECTION_POOL_ENABLED));
    }
    
    @NotNull
    private static JmsDestination.Builder buildTibcoEmsProvider(Bundle bundle) {
        bundle.getPrivateKeys().put("key1",
                new PrivateKey.Builder()
                        .setId("key1-id")
                        .setAlias("key1")
                        .setKeystore(KeyStoreType.GENERIC)
                        .build());

        bundle.getPrivateKeys().put("key2",
                new PrivateKey.Builder()
                        .setId("key2-id")
                        .setAlias("key2")
                        .setKeystore(KeyStoreType.GENERIC)
                        .build());
        
        return buildOutbound()
                .providerType(PROVIDER_TYPE_TIBCO_EMS)
                .additionalProperties(new HashMap<String, Object>() {
                    {
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
                    }});
    }

    private static void verifyTibcoEmsProvider(Entity entity) {
        verifyOutbound(entity);

        assertNotNull(entity);
        Element jmsDestinationEle = entity.getXml();
        assertNotNull(jmsDestinationEle);

        Element jmsDestinationDetailEle = getSingleChildElement(jmsDestinationEle, JMS_DESTINATION_DETAIL);
        assertNotNull(jmsDestinationDetailEle);

        Element jmsConnectionEle = getSingleChildElement(jmsDestinationEle, JMS_CONNECTION);
        assertNotNull(jmsConnectionEle);

        Map<String, Object> jmsDestinationDetailProps =
                mapPropertiesElements(getSingleChildElement(jmsDestinationDetailEle, PROPERTIES, false), PROPERTIES);
        assertNotNull(jmsDestinationDetailProps);

        Map<String, Object> jmsConnectionProps =
                mapPropertiesElements(getSingleChildElement(jmsConnectionEle, PROPERTIES, false), PROPERTIES);
        assertNotNull(jmsConnectionProps);

        Map<String, Object> contextPropertiesTemplateProps =
                mapPropertiesElements(getSingleChildElement(jmsConnectionEle, CONTEXT_PROPERTIES_TEMPLATE, false), CONTEXT_PROPERTIES_TEMPLATE);
        assertNotNull(contextPropertiesTemplateProps);
        
        assertEquals("ssl", contextPropertiesTemplateProps.remove("com.tibco.tibjms.naming.security_protocol"));
        assertEquals("com.l7tech.server.jms.prop.boolean.true", contextPropertiesTemplateProps.remove("com.tibco.tibjms.naming.ssl_auth_only"));
        assertEquals("com.l7tech.server.jms.prop.boolean.true", contextPropertiesTemplateProps.remove("com.tibco.tibjms.naming.ssl_enable_verify_host"));
        assertEquals("com.l7tech.server.jms.prop.trustedcert.listx509", contextPropertiesTemplateProps.remove("com.tibco.tibjms.naming.ssl_trusted_certs"));
        assertEquals("com.l7tech.server.jms.prop.boolean.true", contextPropertiesTemplateProps.remove("com.tibco.tibjms.naming.ssl_enable_verify_hostname"));
        assertEquals("key1", contextPropertiesTemplateProps.remove("com.l7tech.server.jms.prop.jndi.ssgKeyAlias"));
        assertEquals(KeyStoreType.GENERIC.getId(), contextPropertiesTemplateProps.remove("com.l7tech.server.jms.prop.jndi.ssgKeystoreId"));
        assertEquals("com.l7tech.server.jms.prop.keystore\t" +  KeyStoreType.GENERIC.getId() + "\tkey1", contextPropertiesTemplateProps.remove("com.tibco.tibjms.naming.ssl_identity"));
        assertEquals("com.l7tech.server.jms.prop.keystore.password\t" +  KeyStoreType.GENERIC.getId() + "\tkey1", contextPropertiesTemplateProps.remove("com.tibco.tibjms.naming.ssl_password"));
        assertEquals("com.l7tech.server.transport.jms.prov.TibcoConnectionFactoryCustomizer", contextPropertiesTemplateProps.remove("com.l7tech.server.jms.prop.customizer.class"));
        assertEquals("com.l7tech.server.jms.prop.boolean.true", contextPropertiesTemplateProps.remove("com.tibco.tibjms.ssl.auth_only"));
        assertEquals("com.l7tech.server.jms.prop.boolean.true", contextPropertiesTemplateProps.remove("com.tibco.tibjms.ssl.enable_verify_host"));
        assertEquals("com.l7tech.server.jms.prop.trustedcert.listx509", contextPropertiesTemplateProps.remove("com.tibco.tibjms.ssl.trusted_certs"));
        assertEquals("com.l7tech.server.jms.prop.boolean.true", contextPropertiesTemplateProps.remove("com.tibco.tibjms.ssl.enable_verify_hostname"));
        assertEquals("key2", contextPropertiesTemplateProps.remove("com.l7tech.server.jms.prop.queue.ssgKeyAlias"));
        assertEquals(KeyStoreType.GENERIC.getId(), contextPropertiesTemplateProps.remove("com.l7tech.server.jms.prop.queue.ssgKeystoreId"));
        assertEquals("com.l7tech.server.jms.prop.keystore.bytes\t" + KeyStoreType.GENERIC.getId() + "\tkey2", contextPropertiesTemplateProps.remove("com.tibco.tibjms.ssl.identity"));
        assertEquals("com.l7tech.server.jms.prop.keystore.password\t" + KeyStoreType.GENERIC.getId() + "\tkey2", contextPropertiesTemplateProps.remove("com.tibco.tibjms.ssl.password"));
    }
    
    @NotNull
    private static JmsDestination.Builder buildWebShpereMqOverLdapProvider(Bundle bundle) {
        bundle.getPrivateKeys().put("key1",
                new PrivateKey.Builder()
                        .setId("key1-id")
                        .setAlias("key1")
                        .setKeystore(KeyStoreType.GENERIC)
                        .build());
        
        return buildOutbound()
                .providerType(PROVIDER_TYPE_WEBSPHERE_MQ_OVER_LDAP)
                .additionalProperties(new HashMap<String, Object>() {{
                        put("com.l7tech.server.jms.prop.customizer.class", "com.l7tech.server.transport.jms.prov.MQSeriesCustomizer");
                        put("com.l7tech.server.jms.prop.queue.useClientAuth", "true");
                        put("com.l7tech.server.jms.prop.queue.ssgKeyAlias", "key1");
                    }});
    }

    private static void verifyWebShpereMqOverLdapProvider(Entity entity) {
        verifyOutbound(entity);

        assertNotNull(entity);
        Element jmsDestinationEle = entity.getXml();
        assertNotNull(jmsDestinationEle);

        Element jmsDestinationDetailEle = getSingleChildElement(jmsDestinationEle, JMS_DESTINATION_DETAIL);
        assertNotNull(jmsDestinationDetailEle);

        Element jmsConnectionEle = getSingleChildElement(jmsDestinationEle, JMS_CONNECTION);
        assertNotNull(jmsConnectionEle);

        Map<String, Object> contextPropertiesTemplateProps =
                mapPropertiesElements(getSingleChildElement(jmsConnectionEle, CONTEXT_PROPERTIES_TEMPLATE, false), CONTEXT_PROPERTIES_TEMPLATE);
        assertNotNull(contextPropertiesTemplateProps);
        
        assertEquals("com.l7tech.server.transport.jms.prov.MQSeriesCustomizer", contextPropertiesTemplateProps.remove("com.l7tech.server.jms.prop.customizer.class"));
        assertEquals("true", contextPropertiesTemplateProps.remove("com.l7tech.server.jms.prop.queue.useClientAuth"));
        assertEquals(KeyStoreType.GENERIC.getId(), contextPropertiesTemplateProps.remove("com.l7tech.server.jms.prop.queue.ssgKeystoreId"));
        assertEquals("key1", contextPropertiesTemplateProps.remove("com.l7tech.server.jms.prop.queue.ssgKeyAlias"));
    }
}
