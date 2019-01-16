/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.TestUtils;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private static final String JMS_DESTINATION_ENTITY_NAME = "my-jms-endpoint";
    private static final IdGenerator ID_GENERATOR = new IdGenerator();

    @Test
    void buildFromEmptyBundle_noConnections() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }
    
    @Test
    void buildEmptyDeploymentBundle() {
        TestUtils.testDeploymentBundleWithOnlyMapping(
                new JmsDestinationEntityBuilder(ID_GENERATOR),
                new Bundle(),
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                EntityTypes.JMS_DESTINATION_TYPE,
                Collections.emptyList()
        );
    }

    @Test
    void buildDeploymentBundle() {
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildOutbound(bundle, true, true).build()));

        TestUtils.testDeploymentBundleWithOnlyMapping(
                new JmsDestinationEntityBuilder(ID_GENERATOR),
                bundle,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                EntityTypes.JMS_DESTINATION_TYPE,
                Stream.of(JMS_DESTINATION_ENTITY_NAME).collect(Collectors.toList())
        );
    }
    
    @Test
    void buildJmsDestination_Inbound() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildInbound(bundle, true).build()));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, entities.size());
        verifyInbound(entities.get(0));
    }

    @Test
    void buildJmsDestination_Inbound_MissingAssociatedService() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildInbound(bundle, false).build()));

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }
    
    @Test
    void buildJmsDestination_Outbound() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildOutbound(bundle, true, true).build()));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, entities.size());
        verifyOutbound(entities.get(0));
    }

    @Test
    void buildJmsDestination_MissingJndiStoredPassword() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildOutbound(bundle, false, true).build()));

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void buildJmsDestination_MissingDestinationStoredPassword() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildOutbound(bundle, true, false).build()));

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }
    
    @Test
    void buildJmsDestination_TibcoEmsProvider() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildTibcoEmsProvider(bundle, true, true).build()));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, entities.size());
        verifyTibcoEmsProvider(entities.get(0));
    }

    @Test
    void buildJmsDestination_TibcoEmsProvider_MissingJndiClientAuthPrivateKey() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildTibcoEmsProvider(bundle, false, true).build()));

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void buildJmsDestination_TibcoEmsProvider_MissingDestinationClientAuthPrivateKey() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildTibcoEmsProvider(bundle, true, false).build()));

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }
    
    @Test
    void buildJmsDestination_WebShpereOverLdapProvider() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildWebShpereMqOverLdapProvider(bundle, true).build()));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertEquals(1, entities.size());
        verifyWebShpereMqOverLdapProvider(entities.get(0));
    }

    @Test
    void buildJmsDestination_WebShpereOverLdapProvider_MissingDestinationClientAuthPrivateKey() {
        JmsDestinationEntityBuilder builder = new JmsDestinationEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJmsDestinations(ImmutableMap.of(JMS_DESTINATION_ENTITY_NAME, buildWebShpereMqOverLdapProvider(bundle, false).build()));

        assertThrows(EntityBuilderException.class, () -> builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }
    
    @NotNull
    private static JmsDestination.Builder buildCommon(Bundle bundle,
                                                      boolean includeJndiStoredPasswordInBundle,
                                                      boolean includeDestStoredPasswordInBundle) {
        if (includeJndiStoredPasswordInBundle) {
            bundle.getStoredPasswords().put("my-jndi-password",
                    new StoredPassword.Builder()
                            .id("my-jndi-password-id")
                            .name("my-jndi-password")
                            .build());
        }

        if (includeDestStoredPasswordInBundle) {
            bundle.getStoredPasswords().put("my-destination-password",
                    new StoredPassword.Builder()
                            .id("my-destination-password-id")
                            .name("my-destination-password")
                            .build());
        }
        
        return new JmsDestination.Builder()
                .name(JMS_DESTINATION_ENTITY_NAME)
                .id("id-1")
                .initialContextFactoryClassName("my-jndi-initial-context-factory-classname")
                .jndiUrl("my-jndi-url")
                .jndiUsername("my-jndi-username")
                .jndiPasswordRef("my-jndi-password")
                .jndiProperties(new HashMap<String, Object>() {{
                    put("additional-jndi-prop-name-1", "additional-jndi-prop-val-1");
                    put("additional-jndi-prop-name-2", "additional-jndi-prop-val-2");
                }})
                .destinationType(JmsDestination.DestinationType.TOPIC)
                .connectionFactoryName("my-qcf-name")
                .destinationName("my-dest-name")
                .destinationUsername("my-destination-username")
                .destinationPasswordRef("my-destination-password");
    }

    private static void verifyCommon(Entity entity) {
        assertNotNull(entity);
        assertEquals(JMS_DESTINATION_ENTITY_NAME, entity.getName());
        assertEquals(EntityTypes.JMS_DESTINATION_TYPE, entity.getType());
        assertNotNull(entity.getId());
        
        Element jmsDestinationEle = entity.getXml();
        assertNotNull(jmsDestinationEle);

        Element jmsDestinationDetailEle = getSingleChildElement(jmsDestinationEle, JMS_DESTINATION_DETAIL);
        assertNotNull(jmsDestinationDetailEle);

        Element jmsConnectionEle = getSingleChildElement(jmsDestinationEle, JMS_CONNECTION);
        assertNotNull(jmsConnectionEle);
        
        assertEquals(entity.getId(), jmsDestinationEle.getAttribute(ATTRIBUTE_ID));
        assertEquals(entity.getId(), jmsDestinationDetailEle.getAttribute(ATTRIBUTE_ID));
        assertEquals(JMS_DESTINATION_ENTITY_NAME, getSingleChildElementTextContent(jmsDestinationDetailEle, NAME));
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
        assertEquals("${secpass.my-jndi-password.plaintext}", contextPropertiesTemplateProps.remove(JNDI_PASSWORD));
        assertEquals("additional-jndi-prop-val-1", contextPropertiesTemplateProps.remove("additional-jndi-prop-name-1"));
        assertEquals("additional-jndi-prop-val-2", contextPropertiesTemplateProps.remove("additional-jndi-prop-name-2"));

        assertEquals("Topic", jmsDestinationDetailProps.remove(DESTINATION_TYPE));
        assertEquals("my-dest-name", getSingleChildElementTextContent(jmsDestinationDetailEle, JMS_DESTINATION_NAME));
        assertEquals("my-destination-username", jmsDestinationDetailProps.remove(PROPERTY_USERNAME));
        assertEquals("${secpass.my-destination-password.plaintext}", jmsDestinationDetailProps.remove(PROPERTY_PASSWORD));
        assertEquals("my-destination-username", jmsConnectionProps.remove(PROPERTY_USERNAME));
        assertEquals("${secpass.my-destination-password.plaintext}", jmsConnectionProps.remove(PROPERTY_PASSWORD));
    }
    
    @NotNull
    private static JmsDestination.Builder buildInbound(Bundle bundle, boolean includeAssociatedServiceInBundle) {
        if (includeAssociatedServiceInBundle) {
            Folder folder = new Folder();
            folder.setName("my-folder-name");
            folder.setId("my-folder-id");
            folder.setParentFolder(Folder.ROOT_FOLDER);
            
            Service service = new Service();
            service.setName("my-service-name");
            service.setId("my-service-id");
            service.setParentFolder(folder);
            service.setServiceDetailsElement(null);
            service.setPolicy("");
            
            FolderTree folderTree = new FolderTree(ImmutableSet.of(folder, folder.getParentFolder()));

            bundle.setFolderTree(folderTree);
            bundle.addEntity(folder);
            bundle.addEntity(service);
        }
        
        JmsDestination.Builder builder = buildCommon(bundle, true, true);
        
        InboundJmsDestinationDetail inboundDetail = new InboundJmsDestinationDetail();
        inboundDetail.setAcknowledgeType(ON_TAKE);
        inboundDetail.setReplyType(AUTOMATIC);

        InboundJmsDestinationDetail.ServiceResolutionSettings serviceResolutionSettings = new InboundJmsDestinationDetail.ServiceResolutionSettings();
        serviceResolutionSettings.setContentTypeSource(JMS_PROPERTY);
        serviceResolutionSettings.setContentType("my-content-type-jms-prop");
        serviceResolutionSettings.setServiceRef("my-service-id");
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
        assertEquals("true", contextPropertiesTemplateProps.remove(IS_HARDWIRED_SERVICE));
        assertEquals("my-service-id", contextPropertiesTemplateProps.remove(HARDWIRED_SERVICE_ID));
        assertEquals("com.l7tech.server.jms.prop.contentType.header", contextPropertiesTemplateProps.remove(CONTENT_TYPE_SOURCE));
        assertEquals("my-content-type-jms-prop", contextPropertiesTemplateProps.remove(CONTENT_TYPE_VALUE));
        
        assertEquals(-1L, jmsDestinationDetailProps.remove(INBOUND_MAX_SIZE));
        assertEquals("true", contextPropertiesTemplateProps.remove(IS_DEDICATED_CONSUMER_CONNECTION));
        assertEquals("1", contextPropertiesTemplateProps.remove(DEDICATED_CONSUMER_CONNECTION_SIZE));
    }
    
    @NotNull
    private static JmsDestination.Builder buildOutbound(Bundle bundle,
                                                        boolean includeJndiStoredPasswordInBundle,
                                                        boolean includeDestStoredPasswordInBundle) {
        OutboundJmsDestinationDetail outboundDetail = new OutboundJmsDestinationDetail();
        outboundDetail.setIsTemplate(false);
        outboundDetail.setReplyType(NO_REPLY);
        outboundDetail.setMessageFormat(BYTES);
        outboundDetail.setPoolingType(CONNECTION);
        
        return buildCommon(bundle, includeJndiStoredPasswordInBundle, includeDestStoredPasswordInBundle).outboundDetail(outboundDetail);
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
    private static JmsDestination.Builder buildTibcoEmsProvider(
            Bundle bundle,
            boolean includeJndiClientAuthPrivateKeyInBundle,
            boolean includeDestClientAuthPrivateKeyInBundle) {
        
        if (includeJndiClientAuthPrivateKeyInBundle) {
            bundle.getPrivateKeys().put("key1",
                    new PrivateKey.Builder()
                            .setId("key1-id")
                            .setAlias("key1")
                            .setKeystore(KeyStoreType.GENERIC)
                            .build());
        }

        if (includeDestClientAuthPrivateKeyInBundle) {
            bundle.getPrivateKeys().put("key2",
                    new PrivateKey.Builder()
                            .setId("key2-id")
                            .setAlias("key2")
                            .setKeystore(KeyStoreType.GENERIC)
                            .build());
        }
        
        return buildOutbound(bundle, true, true)
                .providerType(PROVIDER_TYPE_TIBCO_EMS)
                .additionalProperties(new HashMap<String, Object>() {
                    {
                        put("com.tibco.tibjms.naming.security_protocol", "ssl");
                        put("com.tibco.tibjms.naming.ssl_auth_only", "com.l7tech.server.jms.prop.boolean.true");
                        put("com.tibco.tibjms.naming.ssl_enable_verify_host", "com.l7tech.server.jms.prop.boolean.true");
                        put("com.tibco.tibjms.naming.ssl_trusted_certs", "com.l7tech.server.jms.prop.trustedcert.listx509");
                        put("com.tibco.tibjms.naming.ssl_enable_verify_hostname", "com.l7tech.server.jms.prop.boolean.true");
                        put(JNDI_CLIENT_AUT_KEYSTORE_ALIAS, "key1");
                        put("com.l7tech.server.jms.prop.customizer.class", "com.l7tech.server.transport.jms.prov.TibcoConnectionFactoryCustomizer");
                        put("com.tibco.tibjms.ssl.auth_only", "com.l7tech.server.jms.prop.boolean.true");
                        put("com.tibco.tibjms.ssl.enable_verify_host", "com.l7tech.server.jms.prop.boolean.true");
                        put("com.tibco.tibjms.ssl.trusted_certs", "com.l7tech.server.jms.prop.trustedcert.listx509");
                        put("com.tibco.tibjms.ssl.enable_verify_hostname", "com.l7tech.server.jms.prop.boolean.true");
                        put(DESTINATION_CLIENT_AUTH_KEYSTORE_ALIAS, "key2");
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
        assertEquals("key1", contextPropertiesTemplateProps.remove(JNDI_CLIENT_AUT_KEYSTORE_ALIAS));
        assertEquals(KeyStoreType.GENERIC.getId(), contextPropertiesTemplateProps.remove(JNDI_CLIENT_AUT_KEYSTORE_ID));
        assertEquals("com.l7tech.server.jms.prop.keystore\t" +  KeyStoreType.GENERIC.getId() + "\tkey1", contextPropertiesTemplateProps.remove(JNDI_CLIENT_AUT_AUTH_IDENTITY));
        assertEquals("com.l7tech.server.jms.prop.keystore.password\t" +  KeyStoreType.GENERIC.getId() + "\tkey1", contextPropertiesTemplateProps.remove(JNDI_CLIENT_AUT_AUTH_PASSWORD));
        assertEquals("com.l7tech.server.transport.jms.prov.TibcoConnectionFactoryCustomizer", contextPropertiesTemplateProps.remove("com.l7tech.server.jms.prop.customizer.class"));
        assertEquals("com.l7tech.server.jms.prop.boolean.true", contextPropertiesTemplateProps.remove("com.tibco.tibjms.ssl.auth_only"));
        assertEquals("com.l7tech.server.jms.prop.boolean.true", contextPropertiesTemplateProps.remove("com.tibco.tibjms.ssl.enable_verify_host"));
        assertEquals("com.l7tech.server.jms.prop.trustedcert.listx509", contextPropertiesTemplateProps.remove("com.tibco.tibjms.ssl.trusted_certs"));
        assertEquals("com.l7tech.server.jms.prop.boolean.true", contextPropertiesTemplateProps.remove("com.tibco.tibjms.ssl.enable_verify_hostname"));
        assertEquals("key2", contextPropertiesTemplateProps.remove(DESTINATION_CLIENT_AUTH_KEYSTORE_ALIAS));
        assertEquals(KeyStoreType.GENERIC.getId(), contextPropertiesTemplateProps.remove(DESTINATION_CLIENT_AUTH_KEYSTORE_ID));
        assertEquals("com.l7tech.server.jms.prop.keystore.bytes\t" + KeyStoreType.GENERIC.getId() + "\tkey2", contextPropertiesTemplateProps.remove(DESTINATION_CLIENT_AUTH_IDENTITY));
        assertEquals("com.l7tech.server.jms.prop.keystore.password\t" + KeyStoreType.GENERIC.getId() + "\tkey2", contextPropertiesTemplateProps.remove(DESTINATION_CLIENT_AUTH_PASSWORD));
    }
    
    @NotNull
    private static JmsDestination.Builder buildWebShpereMqOverLdapProvider(Bundle bundle, boolean includeDestClientAuthPrivateKeyInBundle) {
        if (includeDestClientAuthPrivateKeyInBundle) {
            bundle.getPrivateKeys().put("key1",
                    new PrivateKey.Builder()
                            .setId("key1-id")
                            .setAlias("key1")
                            .setKeystore(KeyStoreType.GENERIC)
                            .build());
        }
        
        return buildOutbound(bundle, true, true)
                .providerType(PROVIDER_TYPE_WEBSPHERE_MQ_OVER_LDAP)
                .additionalProperties(new HashMap<String, Object>() {{
                        put("com.l7tech.server.jms.prop.customizer.class", "com.l7tech.server.transport.jms.prov.MQSeriesCustomizer");
                        put("com.l7tech.server.jms.prop.queue.useClientAuth", "true");
                        put(DESTINATION_CLIENT_AUTH_KEYSTORE_ALIAS, "key1");
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
        assertEquals(KeyStoreType.GENERIC.getId(), contextPropertiesTemplateProps.remove(DESTINATION_CLIENT_AUTH_KEYSTORE_ID));
        assertEquals("key1", contextPropertiesTemplateProps.remove(DESTINATION_CLIENT_AUTH_KEYSTORE_ALIAS));
    }
}
