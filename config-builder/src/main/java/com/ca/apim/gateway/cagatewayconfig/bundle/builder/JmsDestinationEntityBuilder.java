/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
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
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.CASSANDRA_CONNECTION_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

@Singleton
public class JmsDestinationEntityBuilder implements EntityBuilder {
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
                        .map(e -> EntityBuilderHelper.getEntityWithOnlyMapping(CASSANDRA_CONNECTION_TYPE, e.getKey(), idGenerator.generate()))
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
        
        // Build JMS Destination element.
        Element jmsDestinationDetailEle = createElementWithAttributesAndChildren(
                document,
                JMS_DESTINATION_DETAIL,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, JMS_DESTINATION_NAME, "TODO"),
                createElementWithTextContent(document, INBOUND, "TODO"),
                createElementWithTextContent(document, ENABLED, "TODO"),
                createElementWithTextContent(document, TEMPLATE, jmsDestination.isTemplate()),
                createElementWithTextContent(document, PROPERTIES, "TODO")
        );
        
        Map<String, Object> destinationDetailsProps = new HashMap<>();
        // TODO (kpak)
        destinationDetailsProps.put("username", "");
        destinationDetailsProps.put("password", "");
        destinationDetailsProps.put("replyType", "");
        destinationDetailsProps.put("replyToQueueName", "");
        destinationDetailsProps.put("useRequestCorrelationId", "");
        destinationDetailsProps.put("inbound.acknowledgementType", "");
        destinationDetailsProps.put("inbound.failureQueueName", "");
        destinationDetailsProps.put("outbound.MessageType", "");
        destinationDetailsProps.put("inbound.maximumSize", "");
        buildAndAppendPropertiesElement(destinationDetailsProps, document, jmsDestinationDetailEle);

        // Build JMS Connnection element.
        String jmsConnectionEleId = idGenerator.generate();
        Element jmsConnectionEle = createElementWithAttributesAndChildren(
                document,
                JMS_CONNECTION,
                ImmutableMap.of(ATTRIBUTE_ID, jmsConnectionEleId),
                createElementWithTextContent(document, DESTINATION_TYPE, jmsDestination.getProviderType()),
                createElementWithTextContent(document, TEMPLATE, jmsDestination.isTemplate())
        );

        Map<String, Object> connectionProps = new HashMap<>();
//        connectionProps.put(JNDI_INITIAL_CONTEXT_FACTORY_CLASSNAME, jmsDestination.getInitialContextFactoryClassname());
//        connectionProps.put(JNDI_PROVIDER_URL, jmsDestination.getJndiUrl());
//        connectionProps.put(QUEUE_CONNECTION_FACTORY_NAME, jmsDestination.getQueueFactoryUrl());
//        connectionProps.put(CONNECTION_FACTORY_NAME, jmsDestination.getDestinationFactoryUrl());
//        connectionProps.put(TOPIC_CONNECTION_FACTORY_NAME, jmsDestination.getTopicFactoryUrl());
//        connectionProps.put(PROPERTY_USERNAME, jmsDestination.getUsername());
//        connectionProps.put(PROPERTY_PASSWORD, jmsDestination.getPassword());
        
        buildAndAppendPropertiesElement(connectionProps, document, jmsConnectionEle);

        Map<String, Object> contextPropertiesTemplateProps = new HashMap<>();
        // TODO (kpak)

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
