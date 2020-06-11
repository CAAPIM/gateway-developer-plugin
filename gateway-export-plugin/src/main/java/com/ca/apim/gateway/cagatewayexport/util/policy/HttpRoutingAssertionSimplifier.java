/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import org.w3c.dom.Element;
import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

/**
 * Simplifier for the HTTP Routing Assertion.
 */
@Singleton
public class HttpRoutingAssertionSimplifier implements PolicyAssertionSimplifier {

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) {
        Element httpRoutingAssertionElement = context.getAssertionElement();
        final Element httpRoutingCertGoidsElement = getSingleChildElement(httpRoutingAssertionElement, TLS_TRUSTED_CERT_IDS, true);

        // Remove trusted cert goid reference from routing assertion.
        if (httpRoutingCertGoidsElement != null) {
            httpRoutingAssertionElement.removeChild(httpRoutingCertGoidsElement);
        }
    }

    @Override
    public String getAssertionTagName() {
        return HTTP_ROUTING_ASSERTION;
    }
}
