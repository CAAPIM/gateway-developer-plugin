package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.JDBC_CONNECTION_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.STRING_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

public class JdbcAssertionBuilder implements PolicyAssertionBuilder {
    @Override
    public void buildAssertionElement(Element assertionElement, PolicyBuilderContext policyBuilderContext) {
        final Bundle bundle = policyBuilderContext.getBundle();
        final Element connectionNameElement = getSingleChildElement(assertionElement, JDBC_CONNECTION_NAME, true);
        if (connectionNameElement != null) {
            final String connectionName =
                    connectionNameElement.getAttributes().getNamedItem(STRING_VALUE).getTextContent();
            connectionNameElement.setAttribute(STRING_VALUE, bundle.applyUniqueName(connectionName, ENVIRONMENT));
        }
    }

    @Override
    public String getAssertionTagName() {
        return PolicyXMLElements.JDBC_QUERY_ASSERTION;
    }
}
