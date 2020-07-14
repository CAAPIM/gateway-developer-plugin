package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

public class JmsAssertionBuilder implements PolicyAssertionBuilder {
    @Override
    public void buildAssertionElement(Element assertionElement, PolicyBuilderContext policyBuilderContext) {
        final Bundle bundle = policyBuilderContext.getBundle();
        final IdGenerator idGenerator = policyBuilderContext.getIdGenerator();
        final Element connectionNameElement = getSingleChildElement(assertionElement, JMS_ENDPOINT_NAME, true);
        if (connectionNameElement != null) {
            final String connectionName =
                    connectionNameElement.getAttributes().getNamedItem(STRING_VALUE).getTextContent();
            connectionNameElement.setAttribute(STRING_VALUE, bundle.applyUniqueName(connectionName, ENVIRONMENT));
            final JmsDestination jmsDestination = bundle.getJmsDestinations().get(connectionName);
            String id;
            if (jmsDestination != null) {
                if (jmsDestination.getAnnotatedEntity() != null && jmsDestination.getAnnotatedEntity().getId() != null) {
                    id = jmsDestination.getAnnotatedEntity().getId();
                } else {
                    id = jmsDestination.getId();
                }
            } else {
                id = idGenerator.generate();
            }

            Element jmdDestinationGoidElement = createElementWithAttribute(
                    policyBuilderContext.getPolicyDocument(),
                    JMS_ENDPOINT_OID,
                    GOID_VALUE,
                    id
            );
            assertionElement.insertBefore(jmdDestinationGoidElement, connectionNameElement);
        }
    }

    @Override
    public String getAssertionTagName() {
        return JMS_ROUTING_ASSERTION;
    }
}
