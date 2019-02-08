/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.ServiceResolutionSettings;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestinationDetail.ReplyType;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.ConnectionPoolingSettings;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.PoolingType;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.SessionPoolingSettings;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.AcknowledgeType.ON_COMPLETION;
import static com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination.*;
import static com.ca.apim.gateway.cagatewayconfig.beans.JmsDestinationDetail.ReplyType.SPECIFIED_QUEUE;
import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.PoolingType.CONNECTION;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BuilderConstants.STORED_PASSWORD_REF_FORMAT;
import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.getDeploymentBundle;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.JMS_DESTINATION_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

@Singleton
@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
public class JmsDestinationEntityBuilder implements EntityBuilder {
    private static final Integer ORDER = 1500;
    
    private static final String INBOUND_CONTENT_TYPE_SOURCE_NONE = "";
    private static final String INBOUND_CONTENT_TYPE_SOURCE_FREE_FORM = "com.l7tech.server.jms.prop.contentType.freeform";
    private static final String INBOUND_CONTENT_TYPE_SOURCE_JMS_PROPERTY = "com.l7tech.server.jms.prop.contentType.header";
    
    private final IdGenerator idGenerator;
    
    @Inject
    JmsDestinationEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
    
    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        switch (bundleType) {
            case DEPLOYMENT:
                return bundle.getJmsDestinations().entrySet().stream()
                        .map(e -> EntityBuilderHelper.getEntityWithOnlyMapping(JMS_DESTINATION_TYPE, e.getKey(), idGenerator.generate()))
                        .collect(Collectors.toList());
            case ENVIRONMENT:
                return bundle.getJmsDestinations().entrySet().stream().map(e ->
                        buildEntity(bundle, e.getKey(), e.getValue(), document)
                ).collect(Collectors.toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
    
    private Entity buildEntity(Bundle bundle, String name, JmsDestination jmsDestination, Document document) {
        String id = jmsDestination.getId();
        boolean isInbound = jmsDestination.getInboundDetail() != null;
        
        // Build JMS Destination element.
        Element jmsDestinationDetailEle = createElementWithAttributesAndChildren(
                document,
                JMS_DESTINATION_DETAIL,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, JMS_DESTINATION_NAME, jmsDestination.getDestinationName()),
                createElementWithTextContent(document, INBOUND, isInbound)
        );
        
        Map<String, Object> jmsDestinationDetailProps = new HashMap<>();
        jmsDestinationDetailProps.put(DESTINATION_TYPE, jmsDestination.getDestinationType().getType());
        jmsDestinationDetailProps.put(PROPERTY_USERNAME, jmsDestination.getDestinationUsername());
        jmsDestinationDetailProps.put(PROPERTY_PASSWORD, this.getPassword(bundle, jmsDestination.getDestinationPasswordRef(), jmsDestination.getDestinationPassword()));
        
        Map<String, Object> contextPropertiesTemplateProps =
                Optional.ofNullable(jmsDestination.getJndiProperties()).orElseGet(HashMap::new);
        contextPropertiesTemplateProps.put(JNDI_USERNAME, jmsDestination.getJndiUsername());
        contextPropertiesTemplateProps.put(JNDI_PASSWORD, this.getPassword(bundle, jmsDestination.getJndiPasswordRef(), jmsDestination.getJndiPassword()));
        
        boolean isTemplate = false;

        if (isInbound) {
            this.buildInboundDestination(bundle, name, jmsDestination, jmsDestinationDetailProps, contextPropertiesTemplateProps);
        } else {
            isTemplate = jmsDestination.getOutboundDetail().isTemplate();
            this.buildOutboundDestination(jmsDestination, jmsDestinationDetailProps, contextPropertiesTemplateProps);
        }

        String providerType = jmsDestination.getProviderType();
        if (jmsDestination.getAdditionalProperties() != null && !jmsDestination.getAdditionalProperties().isEmpty()) {
            contextPropertiesTemplateProps.putAll(jmsDestination.getAdditionalProperties());
            buildPrivateKeyReferences(bundle, providerType, contextPropertiesTemplateProps);
        }
        jmsDestinationDetailEle.appendChild(createElementWithTextContent(document, ENABLED, true));
        jmsDestinationDetailEle.appendChild(createElementWithTextContent(document, TEMPLATE, isTemplate));
        buildAndAppendPropertiesElement(jmsDestinationDetailProps, document, jmsDestinationDetailEle);

        // Build JMS Connection element.
        String jmsConnectionEleId = idGenerator.generate();
        Element jmsConnectionEle = createElementWithAttributesAndChildren(
                document,
                JMS_CONNECTION,
                ImmutableMap.of(ATTRIBUTE_ID, jmsConnectionEleId)
        );
        
        if (StringUtils.isNotEmpty(providerType)) {
            jmsConnectionEle.appendChild(createElementWithTextContent(document, JMS_PROVIDER_TYPE, providerType));
        }
        createElementWithTextContent(document, TEMPLATE, isTemplate);

        Map<String, Object> jmsConnectionProps = new HashMap<>();
        jmsConnectionProps.put(JNDI_INITIAL_CONTEXT_FACTORY_CLASSNAME, jmsDestination.getInitialContextFactoryClassName());
        jmsConnectionProps.put(JNDI_PROVIDER_URL, jmsDestination.getJndiUrl());
        jmsConnectionProps.put(CONNECTION_FACTORY_NAME, jmsDestination.getConnectionFactoryName());
        jmsConnectionProps.put(PROPERTY_USERNAME, jmsDestination.getDestinationUsername());
        jmsConnectionProps.put(PROPERTY_PASSWORD, this.getPassword(bundle, jmsDestination.getDestinationPasswordRef(), jmsDestination.getDestinationPassword()));
        buildAndAppendPropertiesElement(jmsConnectionProps, document, jmsConnectionEle);
        jmsConnectionEle.appendChild(buildPropertiesElement(contextPropertiesTemplateProps, document, CONTEXT_PROPERTIES_TEMPLATE));

        // Build JMS Destination element.
        Element jmsDestinationEle = createElementWithAttributesAndChildren(
                document,
                JMS_DESTINATION,
                ImmutableMap.of(ATTRIBUTE_ID, id));
        jmsDestinationEle.appendChild(jmsDestinationDetailEle);
        jmsDestinationEle.appendChild(jmsConnectionEle);

        return EntityBuilderHelper.getEntityWithNameMapping(EntityTypes.JMS_DESTINATION_TYPE, name, id, jmsDestinationEle);
    }
    
    private String getPassword(final Bundle bundle, final String passwordRef, final String plaintext) {
        if (passwordRef != null) {
            if (bundle.getStoredPasswords().get(passwordRef) == null) {
                throw new EntityBuilderException("JMS destination is referencing missing stored password '" + passwordRef +"'");
            }
            return String.format(STORED_PASSWORD_REF_FORMAT, passwordRef);
        } else {
            return plaintext;
        }
    }
    
    private void buildPrivateKeyReferences(
            final Bundle bundle,
            final String providerType,
            final Map<String, Object> contextPropertiesTemplateProps) {
        String alias = (String) contextPropertiesTemplateProps.get(DESTINATION_CLIENT_AUTH_KEYSTORE_ALIAS);
        if (StringUtils.isNotEmpty(alias)) {
            if (null == bundle.getPrivateKeys().get(alias)) {
                throw new EntityBuilderException("Jms destination is referencing missing private key '" + alias + "'");
            }
            
            contextPropertiesTemplateProps.put(DESTINATION_CLIENT_AUTH_KEYSTORE_ID, KeyStoreType.GENERIC.getId());
            
            if(PROVIDER_TYPE_TIBCO_EMS.equals(providerType)) {
                contextPropertiesTemplateProps.put(DESTINATION_CLIENT_AUTH_IDENTITY, 
                        "com.l7tech.server.jms.prop.keystore.bytes\t" + KeyStoreType.GENERIC.getId() + "\t" + alias);
                contextPropertiesTemplateProps.put(DESTINATION_CLIENT_AUTH_PASSWORD, 
                        "com.l7tech.server.jms.prop.keystore.password\t" + KeyStoreType.GENERIC.getId() + "\t" + alias);
            }
        }

        alias = (String) contextPropertiesTemplateProps.get(JNDI_CLIENT_AUT_KEYSTORE_ALIAS);
        if (StringUtils.isNotEmpty(alias)) {
            if (null == bundle.getPrivateKeys().get(alias)) {
                throw new EntityBuilderException("Jms destination is referencing missing private key '" + alias + "'");
            }
            
            if(PROVIDER_TYPE_TIBCO_EMS.equals(providerType)) {
                contextPropertiesTemplateProps.put(JNDI_CLIENT_AUT_KEYSTORE_ID, KeyStoreType.GENERIC.getId());
                contextPropertiesTemplateProps.put(JNDI_CLIENT_AUT_AUTH_IDENTITY,
                        "com.l7tech.server.jms.prop.keystore\t" + KeyStoreType.GENERIC.getId() + "\t" + alias);
                contextPropertiesTemplateProps.put(JNDI_CLIENT_AUT_AUTH_PASSWORD,
                        "com.l7tech.server.jms.prop.keystore.password\t" + KeyStoreType.GENERIC.getId() + "\t" + alias);
            }
        }
    }
    
    private void buildInboundDestination (
            final Bundle bundle,
            final String name,
            final JmsDestination jmsDestination,
            final Map<String, Object> jmsDestinationDetailProps,
            final Map<String, Object> contextPropertiesTemplateProps) {

        InboundJmsDestinationDetail inboundDetail = jmsDestination.getInboundDetail();
        jmsDestinationDetailProps.put(INBOUND_ACKNOWLEDGEMENT_TYPE, inboundDetail.getAcknowledgeType().getType());
        ReplyType replyType = inboundDetail.getReplyType();
        jmsDestinationDetailProps.put(REPLY_TYPE, replyType.getType());

        if (SPECIFIED_QUEUE.equals(replyType)) {
            jmsDestinationDetailProps.put(REPLY_QUEUE_NAME, inboundDetail.getReplyToQueueName());
        }

        jmsDestinationDetailProps.put(USE_REQUEST_CORRELATION_ID, inboundDetail.isUseRequestCorrelationId());

        ServiceResolutionSettings serviceResolutionSettings = inboundDetail.getServiceResolutionSettings();
        if (serviceResolutionSettings != null) {
            String serviceRef = serviceResolutionSettings.getServiceRef();
            Service service = bundle.getServices().get(serviceRef);

            if (service == null) {
                service = getDeploymentBundle().getServices().get(serviceRef);
            }

            if (service == null) {
                throw new EntityBuilderException("Could not find associated Service for inbound JMS Destination: " + name + ". Service Path: " + serviceRef);
            }

            if (serviceRef == null) {
                contextPropertiesTemplateProps.put(IS_HARDWIRED_SERVICE, Boolean.FALSE.toString());
            } else {
                contextPropertiesTemplateProps.put(IS_HARDWIRED_SERVICE, Boolean.TRUE.toString());
                contextPropertiesTemplateProps.put(HARDWIRED_SERVICE_ID, service.getId());
            }

            putToMapIfValueIsNotNull(
                    contextPropertiesTemplateProps,
                    SOAP_ACTION_MSG_PROP_NAME,
                    serviceResolutionSettings.getSoapActionMessagePropertyName());
            
            contextPropertiesTemplateProps.put(CONTENT_TYPE_SOURCE, this.getContentTypeSource(serviceResolutionSettings));

            String contentType = serviceResolutionSettings.getContentType();
            contentType = Strings.nullToEmpty(contentType);

            contextPropertiesTemplateProps.put(CONTENT_TYPE_VALUE, contentType);
        }

        if (ON_COMPLETION.equals(inboundDetail.getAcknowledgeType())){
            putToMapIfValueIsNotNull(
                    jmsDestinationDetailProps,
                    INBOUND_FAILURE_QUEUE_NAME,
                    inboundDetail.getFailureQueueName());
        }

        contextPropertiesTemplateProps.put(IS_DEDICATED_CONSUMER_CONNECTION, Boolean.TRUE.toString());
        Integer numOfConsumerConnections = inboundDetail.getNumOfConsumerConnections();
        if (numOfConsumerConnections == null) {
            numOfConsumerConnections = DEFAULT_DEDICATED_CONSUMER_CONNECTION_SIZE;
        }
        contextPropertiesTemplateProps.put(DEDICATED_CONSUMER_CONNECTION_SIZE, Integer.toString(numOfConsumerConnections));

        Long maxInboundMessageSize = inboundDetail.getMaxMessageSizeBytes();
        if (maxInboundMessageSize == null) {
            maxInboundMessageSize = DEFAULT_MAX_INBOUND_MESSAGE_SIZE;
        }
        jmsDestinationDetailProps.put(INBOUND_MAX_SIZE, maxInboundMessageSize);
    }

    private String getContentTypeSource(ServiceResolutionSettings serviceResolutionSettings) {
        if (serviceResolutionSettings.getContentTypeSource() == null) {
            return INBOUND_CONTENT_TYPE_SOURCE_NONE;
        }
        
        String contentTypeSource ;
        switch (serviceResolutionSettings.getContentTypeSource()) {
            case NONE:
                contentTypeSource = INBOUND_CONTENT_TYPE_SOURCE_NONE;
                break;
            case FREE_FORM:
                contentTypeSource = INBOUND_CONTENT_TYPE_SOURCE_FREE_FORM;
                break;
            case JMS_PROPERTY:
                contentTypeSource = INBOUND_CONTENT_TYPE_SOURCE_JMS_PROPERTY;
                break;
            default:
                contentTypeSource = INBOUND_CONTENT_TYPE_SOURCE_NONE;
                break;
        }
        return contentTypeSource;
    }
    
    private void buildOutboundDestination (
            final JmsDestination jmsDestination,
            final Map<String, Object> jmsDestinationDetailProps,
            final Map<String, Object> contextPropertiesTemplateProps) {

        OutboundJmsDestinationDetail outboundDetail = jmsDestination.getOutboundDetail();
        ReplyType replyType = outboundDetail.getReplyType();
        jmsDestinationDetailProps.put(REPLY_TYPE, replyType.getType());

        if (SPECIFIED_QUEUE.equals(replyType)) {
            jmsDestinationDetailProps.put(REPLY_QUEUE_NAME, outboundDetail.getReplyToQueueName());
        }

        jmsDestinationDetailProps.put(USE_REQUEST_CORRELATION_ID, outboundDetail.isUseRequestCorrelationId());
        jmsDestinationDetailProps.put(OUTBOUND_MESSAGE_TYPE, outboundDetail.getMessageFormat().getType());

        PoolingType poolingType = outboundDetail.getPoolingType();
        if (CONNECTION.equals(poolingType)) {
            contextPropertiesTemplateProps.put(CONNECTION_POOL_ENABLED, Boolean.TRUE.toString());
            ConnectionPoolingSettings connectionPoolingSettings = outboundDetail.getConnectionPoolingSettings();
            if (connectionPoolingSettings != null) {
                putToMapIfValueIsNotNull(contextPropertiesTemplateProps, CONNECTION_POOL_SIZE, connectionPoolingSettings.getSize());
                putToMapIfValueIsNotNull(contextPropertiesTemplateProps, CONNECTION_POOL_MIN_IDLE, connectionPoolingSettings.getMinIdle());
                putToMapIfValueIsNotNull(contextPropertiesTemplateProps, CONNECTION_POOL_MAX_WAIT, connectionPoolingSettings.getMaxWaitMs());
            }
        } else {
            contextPropertiesTemplateProps.put(CONNECTION_POOL_ENABLED, Boolean.FALSE.toString());
            SessionPoolingSettings sessionPoolingSettings = outboundDetail.getSessionPoolingSettings();
            if (sessionPoolingSettings != null) {
                putToMapIfValueIsNotNull(contextPropertiesTemplateProps, SESSION_POOL_SIZE, sessionPoolingSettings.getSize());
                putToMapIfValueIsNotNull(contextPropertiesTemplateProps, SESSION_POOL_MAX_IDLE, sessionPoolingSettings.getMaxIdle());
                putToMapIfValueIsNotNull(contextPropertiesTemplateProps, SESSION_POOL_MAX_WAIT, sessionPoolingSettings.getMaxWaitMs()); 
            }
        }
    }

    private static void putToMapIfValueIsNotNull(
            @NotNull Map<String, Object> map,
            @NotNull String key,
            @Nullable Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
