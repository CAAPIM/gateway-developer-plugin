package com.ca.apim.gateway.cagatewayexport.util.policy;

import org.w3c.dom.Element;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.HTTP2_CLIENT_CONFIG_GOID;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.HTTP2_ROUTING_ASSERTION;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

/**
 * Simplifier for the HTTP/2 Routing Assertion.
 */
@Singleton
public class Http2AssertionSimplifier implements PolicyAssertionSimplifier {

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) {
        Element http2RoutingElement = context.getAssertionElement();

        final Element http2ClientConfigGoid = getSingleChildElement(http2RoutingElement,
                HTTP2_CLIENT_CONFIG_GOID, true);
        // Remove HTTP2 Client Config Goid reference from the routing assertion.
        if (http2ClientConfigGoid != null) {
            http2RoutingElement.removeChild(http2ClientConfigGoid);
        }
    }

    @Override
    public String getAssertionTagName() {
        return HTTP2_ROUTING_ASSERTION;
    }
}
