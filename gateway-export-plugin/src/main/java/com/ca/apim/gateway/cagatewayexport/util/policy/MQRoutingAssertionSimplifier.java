package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;

/**
 * Simplifier for the MQ Routing Assertion.
 */
@Singleton
public class MQRoutingAssertionSimplifier implements PolicyAssertionSimplifier {

    private static final Logger LOGGER = Logger.getLogger(MQRoutingAssertionSimplifier.class.getName());

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) throws DocumentParseException {
        Element mqRoutingAssertionElement = context.getAssertionElement();

        final Element activeConnectorId = getSingleElement(mqRoutingAssertionElement, ACTIVE_CONNECTOR_ID);
        final Element activeConnectorGoId = getSingleElement(mqRoutingAssertionElement, ACTIVE_CONNECTOR_GOID);
        // Remove active connector id and goid reference from routing assertion.
        if (activeConnectorId != null) {
            mqRoutingAssertionElement.removeChild(activeConnectorId);
        }
        if (activeConnectorGoId != null) {
            mqRoutingAssertionElement.removeChild(activeConnectorGoId);
        }
    }

    @Override
    public String getAssertionTagName() {
        return MQ_ROUTING_ASSERTION;
    }
}
