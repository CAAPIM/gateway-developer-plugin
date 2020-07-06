package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.SsgActiveConnector;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

public class MQRoutingAssertionBuilder implements PolicyAssertionBuilder {
    @Override
    public void buildAssertionElement(Element assertionElement, PolicyBuilderContext policyBuilderContext) throws DocumentParseException {
        final Bundle bundle = policyBuilderContext.getBundle();
        final IdGenerator idGenerator = policyBuilderContext.getIdGenerator();
        final Element activeConnectorNameElement = getSingleChildElement(assertionElement, ACTIVE_CONNECTOR_NAME, true);
        if (activeConnectorNameElement != null) {
            final String activeConnectorName = activeConnectorNameElement.getAttributes().getNamedItem(STRING_VALUE).getTextContent();
            activeConnectorNameElement.setAttribute(STRING_VALUE, bundle.applyUniqueName(activeConnectorName, ENVIRONMENT, false));
            final SsgActiveConnector ssgActiveConnector = bundle.getSsgActiveConnectors().get(activeConnectorName);
            final String id = ssgActiveConnector != null && ssgActiveConnector.getAnnotatedEntity() != null && ssgActiveConnector.getAnnotatedEntity().getId() != null ?
                    ssgActiveConnector.getAnnotatedEntity().getId() : idGenerator.generate();
            Element activeConnectorGoidElement = createElementWithAttribute(
                    policyBuilderContext.getPolicyDocument(),
                    ACTIVE_CONNECTOR_GOID,
                    GOID_VALUE,
                    id
            );
            assertionElement.insertBefore(activeConnectorGoidElement, activeConnectorNameElement);

            Element activeConnectorIdElement = createElementWithAttribute(
                    policyBuilderContext.getPolicyDocument(),
                    ACTIVE_CONNECTOR_ID,
                    GOID_VALUE,
                    id
            );
            assertionElement.insertBefore(activeConnectorIdElement, activeConnectorNameElement);
        }
    }

    @Override
    public String getAssertionTagName() {
        return MQ_ROUTING_ASSERTION;
    }
}
