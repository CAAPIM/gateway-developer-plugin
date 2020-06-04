/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;
import javax.inject.Singleton;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

/**
 * Simplifier for the HTTP Routing Assertion.
 */
@Singleton
public class HttpRoutingAssertionSimplifier implements PolicyAssertionSimplifier {

    private static final Logger LOGGER = Logger.getLogger(HttpRoutingAssertionSimplifier.class.getName());

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) throws DocumentParseException {
        Element httpRoutingAssertionElement = context.getAssertionElement();
        final Element httpRoutingCertGoidElement = getSingleChildElement(httpRoutingAssertionElement, TLS_TRUSTED_CERT_ID, true);

        // Remove trusted cert goid reference from routing assertion.
        if (httpRoutingCertGoidElement != null) {
            httpRoutingAssertionElement.removeChild(httpRoutingCertGoidElement);
        }
    }

    @Override
    public String getAssertionTagName() {
        return HTTP_ROUTING_ASSERTION;
    }
}
