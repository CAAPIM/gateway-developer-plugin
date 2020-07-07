package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

public class JmsAssertionBuilder implements PolicyAssertionBuilder {
    @Override
    public void buildAssertionElement(Element assertionElement, PolicyBuilderContext policyBuilderContext) throws DocumentParseException {
        final Bundle bundle = policyBuilderContext.getBundle();
        final Element connectionNameElement = getSingleChildElement(assertionElement, JMS_ENDPOINT_NAME, true);
        if (connectionNameElement != null) {
            final String connectionName =
                    connectionNameElement.getAttributes().getNamedItem(STRING_VALUE).getTextContent();
            connectionNameElement.setAttribute(STRING_VALUE, bundle.applyUniqueName(connectionName, ENVIRONMENT, false));
        }
    }

    @Override
    public String getAssertionTagName() {
        return JMS_ROUTING_ASSERTION;
    }
}
