/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.ConnectionPoolingSettings;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.PoolingType;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.ReplyType;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.SessionPoolingSettings;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.PoolingType.CONNECTION;
import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.ReplyType.SPECIFIED_QUEUE;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.JMS_DESTINATION_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

@Singleton
@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
public class JmsDestinationEntityBuilder implements EntityBuilder {
    private static final String STORED_PASSWORD_REF_FORMAT = "${secpass.%s.plaintext}";
    private static final Integer ORDER = 1500;
    
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

    private Entity buildEntity(Bundle bundle, String name, JmsDestination jmsDestination, Document document) {
        String id = idGenerator.generate();
        boolean isInbound = jmsDestination.isInbound();
        
        // Build JMS Destination element.
        Element jmsDestinationDetailEle = createElementWithAttributesAndChildren(
                document,
                JMS_DESTINATION_DETAIL,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, JMS_DESTINATION_NAME, jmsDestination.getDestinationName()),
                createElementWithTextContent(document, INBOUND, isInbound),
                createElementWithTextContent(document, ENABLED, true), // (kpak) - implement
                createElementWithTextContent(document, TEMPLATE, jmsDestination.isTemplate()) // (kpak) - remove
        );
        
        Map<String, Object> jmsDestinationDetailProps = new HashMap<>();
        jmsDestinationDetailProps.put(DESTINATION_TYPE, jmsDestination.getDestinationType());
        jmsDestinationDetailProps.put(PROPERTY_USERNAME, jmsDestination.getDestinationUsername());
        if (jmsDestination.getDestinationPasswordRef() != null) {
            jmsDestinationDetailProps.put(PROPERTY_PASSWORD, String.format(STORED_PASSWORD_REF_FORMAT, jmsDestination.getDestinationPasswordRef()));
        } else {
            jmsDestinationDetailProps.put(PROPERTY_PASSWORD, jmsDestination.getDestinationPassword());
        }

        Map<String, Object> contextPropertiesTemplateProps =
                Optional.ofNullable(jmsDestination.getJndiProperties()).orElseGet(HashMap::new);
        contextPropertiesTemplateProps.put(JNDI_USERNAME, jmsDestination.getJndiUsername());
        if (jmsDestination.getJndiPasswordRef() != null) {
            contextPropertiesTemplateProps.put(JNDI_PASSWORD, String.format(STORED_PASSWORD_REF_FORMAT, jmsDestination.getJndiPasswordRef()));
        } else {
            contextPropertiesTemplateProps.put(JNDI_PASSWORD, jmsDestination.getJndiPassword());
        }
        
        if (!isInbound) {
            OutboundJmsDestinationDetail outboundDetail = jmsDestination.getOutboundDetail();
            ReplyType replyType = outboundDetail.getReplyType();
            jmsDestinationDetailProps.put(REPLY_TYPE, replyType);
            
            if (SPECIFIED_QUEUE.equals(replyType)) {
                jmsDestinationDetailProps.put(REPLY_QUEUE_NAME, outboundDetail.getReplyToQueueName());
            }

            jmsDestinationDetailProps.put(USE_REQUEST_CORRELATION_ID, outboundDetail.useRequestCorrelationId());
            jmsDestinationDetailProps.put(OUTBOUND_MESSAGE_TYPE, outboundDetail.getMessageFormat());

            PoolingType poolingType = outboundDetail.getPoolingType();
            if (CONNECTION.equals(poolingType)) {
                contextPropertiesTemplateProps.put(CONNECTION_POOL_ENABLED, true);
                ConnectionPoolingSettings connectionPoolingSettings = outboundDetail.getConnectionPoolingSettings();
                if (connectionPoolingSettings != null) {
                    if (connectionPoolingSettings.getSize() != null) {
                        contextPropertiesTemplateProps.put(CONNECTION_POOL_SIZE, connectionPoolingSettings.getSize());
                    }

                    if (connectionPoolingSettings.getMinIdle() != null) {
                        contextPropertiesTemplateProps.put(CONNECTION_POOL_MIN_IDLE, connectionPoolingSettings.getMinIdle());
                    }

                    if (connectionPoolingSettings.getMaxWaitMs() != null) {
                        contextPropertiesTemplateProps.put(CONNECTION_POOL_MAX_WAIT, connectionPoolingSettings.getMaxWaitMs());
                    }
                }
            } else {
                contextPropertiesTemplateProps.put(CONNECTION_POOL_ENABLED, false);
                SessionPoolingSettings sessionPoolingSettings = outboundDetail.getSessionPoolingSettings();
                if (sessionPoolingSettings != null) {
                    if (sessionPoolingSettings.getSize() != null) {
                        contextPropertiesTemplateProps.put(SESSION_POOL_SIZE, sessionPoolingSettings.getSize());
                    }

                    if (sessionPoolingSettings.getMaxIdle() != null) {
                        contextPropertiesTemplateProps.put(SESSION_POOL_MAX_IDLE, sessionPoolingSettings.getMaxIdle());
                    }

                    if (sessionPoolingSettings.getMaxWaitMs() != null) {
                        contextPropertiesTemplateProps.put(SESSION_POOL_MAX_WAIT, sessionPoolingSettings.getMaxWaitMs());
                    }
                }
            }
        } else {
            // (kpak) - inbound
            // destinationDetailsProps.put("inbound.acknowledgementType", "");
            // destinationDetailsProps.put("inbound.failureQueueName", "");
            // destinationDetailsProps.put("inbound.maximumSize", "");
        }
        buildAndAppendPropertiesElement(jmsDestinationDetailProps, document, jmsDestinationDetailEle);

        // Build JMS Connection element.
        String jmsConnectionEleId = idGenerator.generate();
        Element jmsConnectionEle = createElementWithAttributesAndChildren(
                document,
                JMS_CONNECTION,
                ImmutableMap.of(ATTRIBUTE_ID, jmsConnectionEleId),
                createElementWithTextContent(document, DESTINATION_TYPE, jmsDestination.getProviderType()),
                createElementWithTextContent(document, TEMPLATE, jmsDestination.isTemplate())
        );

        Map<String, Object> jmsConnectionProps = new HashMap<>();
        jmsConnectionProps.put(JNDI_INITIAL_CONTEXT_FACTORY_CLASSNAME, jmsDestination.getInitialContextFactoryClassName());
        jmsConnectionProps.put(JNDI_PROVIDER_URL, jmsDestination.getJndiUrl());
        jmsConnectionProps.put(CONNECTION_FACTORY_NAME, jmsDestination.getConnectionFactoryName());
        jmsConnectionProps.put(PROPERTY_USERNAME, jmsDestination.getDestinationUsername());
        if (jmsDestination.getDestinationPasswordRef() != null) {
            jmsConnectionProps.put(PROPERTY_PASSWORD, String.format(STORED_PASSWORD_REF_FORMAT, jmsDestination.getDestinationPasswordRef()));
        } else {
            jmsConnectionProps.put(PROPERTY_PASSWORD, jmsDestination.getDestinationPassword());
        }
        buildAndAppendPropertiesElement(jmsConnectionProps, document, jmsConnectionEle);
        buildAndAppendPropertiesElement(contextPropertiesTemplateProps, document, jmsConnectionEle);

        // Build JMS Destination element.
        Element jmsDestinationEle = createElementWithAttributesAndChildren(
                document,
                JMS_DESTINATION,
                ImmutableMap.of(ATTRIBUTE_ID, id));
        jmsDestinationEle.appendChild(jmsDestinationDetailEle);
        jmsDestinationEle.appendChild(jmsConnectionEle);

        return EntityBuilderHelper.getEntityWithNameMapping(EntityTypes.JMS_DESTINATION_TYPE, name, id, jmsDestinationEle);
    }
    
    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
