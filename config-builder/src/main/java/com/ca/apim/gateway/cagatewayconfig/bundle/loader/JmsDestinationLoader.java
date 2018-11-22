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

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

@Singleton
public class JmsDestinationLoader implements BundleEntityLoader {
    @Override
    public void load(Bundle bundle, Element element) {
        
        final Element jmsDestinationEle = getSingleChildElement(getSingleChildElement(element, RESOURCE), JMS_DESTINATION);
        final String id =  jmsDestinationEle.getAttribute(ATTRIBUTE_ID);

        final Element jmsDestinationDetailEle = getSingleChildElement(jmsDestinationEle, JMS_DESTINATION_DETAIL);
        final String name = getSingleChildElementTextContent(jmsDestinationDetailEle, NAME);
        final boolean isInbound = toBoolean(getSingleChildElementTextContent(jmsDestinationDetailEle, INBOUND));
        final boolean isTemplate = toBoolean(getSingleChildElementTextContent(jmsDestinationDetailEle, TEMPLATE));
        
        final Element jmsConnectionEle = getSingleChildElement(jmsDestinationEle, JMS_CONNECTION);
        final String providerType = getSingleChildElementTextContent(jmsConnectionEle, JMS_PROVIDER_TYPE);
        
        JmsDestination jmsDestination = new JmsDestination();
        jmsDestination.setId(id);
        jmsDestination.setName(name);
        jmsDestination.setIsInbound(isInbound);
        jmsDestination.setIsTemplate(isTemplate);
        jmsDestination.setProviderType(providerType);
        
        bundle.getJmsDestinations().put(name, jmsDestination);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.JMS_DESTINATION_TYPE;
    }
}
