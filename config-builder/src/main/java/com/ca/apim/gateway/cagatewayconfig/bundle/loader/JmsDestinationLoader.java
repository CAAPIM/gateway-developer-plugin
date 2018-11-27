/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

@Singleton
public class JmsDestinationLoader implements BundleEntityLoader {
    @Override
    public void load(Bundle bundle, Element element) {
        
        final Element jmsDestinationEle = getSingleChildElement(getSingleChildElement(element, RESOURCE), JMS_DESTINATION);
        final String id = jmsDestinationEle.getAttribute(ATTRIBUTE_ID);

        final Element jmsDestinationDetailEle = getSingleChildElement(jmsDestinationEle, JMS_DESTINATION_DETAIL);
        final String name = getSingleChildElementTextContent(jmsDestinationDetailEle, NAME);
        final boolean isInbound = toBoolean(getSingleChildElementTextContent(jmsDestinationDetailEle, INBOUND));
        final boolean isTemplate = toBoolean(getSingleChildElementTextContent(jmsDestinationDetailEle, TEMPLATE));
        final Map<String, Object> jmsDestinationProperties = mapPropertiesElements(getSingleChildElement(jmsDestinationDetailEle, PROPERTIES, false), PROPERTIES);
        
        final Element jmsConnectionEle = getSingleChildElement(jmsDestinationEle, JMS_CONNECTION);
        final String providerType = getSingleChildElementTextContent(jmsConnectionEle, JMS_PROVIDER_TYPE);

        final Map<String, Object> jmsConnectionProperties = mapPropertiesElements(getSingleChildElement(jmsConnectionEle, PROPERTIES, false), PROPERTIES);
        final String initialContextFactoryClassName = (String) jmsConnectionProperties.remove(JNDI_INITIAL_CONTEXT_FACTORY_CLASSNAME);
        final String jndiUrl = (String) jmsConnectionProperties.remove(JNDI_PROVIDER_URL);

        final Map<String, Object> contextPropertiesTemplate = mapPropertiesElements(getSingleChildElement(jmsConnectionEle, CONTEXT_PROPERTIES_TEMPLATE, false), CONTEXT_PROPERTIES_TEMPLATE);
        final String jndiUsername = (String) contextPropertiesTemplate.remove(JNDI_USERNAME);
        final String jndiPassword = (String) contextPropertiesTemplate.remove(JNDI_PASSWORD);
        
        final Map<String, Object> jndiProperties = contextPropertiesTemplate.entrySet().stream()
                .filter(map -> 
                        !map.getKey().startsWith("com.l7tech.server.jms.prop.") && 
                        !"com.l7tech.server.jms.soapAction.msgPropName".equals(map.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final String destinationType = (String) jmsDestinationProperties.remove(DESTINATION_TYPE);
        final String connectionFactoryName = (String) jmsConnectionProperties.remove(CONNECTION_FACTORY_NAME);
        final String destinationName = getSingleChildElementTextContent(jmsDestinationDetailEle, JMS_DESTINATION_NAME);
        final String destinationUsername = (String) jmsDestinationProperties.remove(PROPERTY_USERNAME);
        final String destinationPassword = (String) jmsDestinationProperties.remove(PROPERTY_PASSWORD);
        
        JmsDestination jmsDestination = new JmsDestination();
        jmsDestination.setId(id);
        jmsDestination.setName(name);
        jmsDestination.setIsInbound(isInbound);
        jmsDestination.setIsTemplate(isTemplate);
        jmsDestination.setProviderType(providerType);
        jmsDestination.setInitialContextFactoryClassName(initialContextFactoryClassName);
        jmsDestination.setJndiUrl(jndiUrl);
        jmsDestination.setJndiUsername(jndiUsername);
        jmsDestination.setJndiPassword(jndiPassword);
        jmsDestination.setJndiProperties(jndiProperties);
        
        jmsDestination.setDestinationType(JmsDestination.DestinationType.fromType(destinationType));
        jmsDestination.setConnectionFactoryName(connectionFactoryName);
        jmsDestination.setDestinationName(destinationName);
        jmsDestination.setDestinationUsername(destinationUsername);
        jmsDestination.setDestinationPassword(destinationPassword);
        
        bundle.getJmsDestinations().put(name, jmsDestination);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.JMS_DESTINATION_TYPE;
    }
}
