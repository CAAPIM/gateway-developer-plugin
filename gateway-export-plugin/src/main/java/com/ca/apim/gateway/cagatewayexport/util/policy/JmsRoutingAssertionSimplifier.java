/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;

/**
 * Simplifier for the JMS Routing Assertion.
 */
@Singleton
public class JmsRoutingAssertionSimplifier implements PolicyAssertionSimplifier {

    private static final Logger LOGGER = Logger.getLogger(JmsRoutingAssertionSimplifier.class.getName());

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) throws DocumentParseException {
        Bundle bundle = context.getBundle();
        Element jmsRoutingAssertionElement = context.getAssertionElement();

        final Element jmsEndpointGoidEle = getSingleElement(jmsRoutingAssertionElement, JMS_ENDPOINT_OID);
        final String jmsEndpointGoid = jmsEndpointGoidEle.getAttribute(GOID_VALUE);
        final Optional<JmsDestination> jmsDestination = bundle.getEntities(JmsDestination.class).values().stream().filter(e -> e.getId().equals(jmsEndpointGoid)).findAny();
        if (!jmsDestination.isPresent()) {
            LOGGER.log(Level.WARNING, "Could not find referenced JMS Destination with id: {0}", jmsEndpointGoid);
        }
        // Remove Goid reference to JMS destination. JMS Routing assertion already has a reference to JMS destination entity by name.
        jmsRoutingAssertionElement.removeChild(jmsEndpointGoidEle);
    }

    @Override
    public String getAssertionTagName() {
        return JMS_ROUTING_ASSERTION;
    }
}
