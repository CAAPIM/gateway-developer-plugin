package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.GOID_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

public class Http2AssertionBuilder implements PolicyAssertionBuilder {
    @Override
    public void buildAssertionElement(Element assertionElement, PolicyBuilderContext policyBuilderContext) {
        final Bundle bundle = policyBuilderContext.getBundle();
        final Element http2ClientNameEle = getSingleChildElement(assertionElement, HTTP2_CLIENT_CONFIG_NAME, true);
        if (http2ClientNameEle != null) {
            final String http2ClientName =
                    http2ClientNameEle.getAttributes().getNamedItem(STRING_VALUE).getTextContent();
            http2ClientNameEle.setAttribute(STRING_VALUE, bundle.applyUniqueName(http2ClientName, ENVIRONMENT, false));
            final GenericEntity http2Client = bundle.getGenericEntities().get(http2ClientName);
            final String id = getIdFromAnnotableEntity(http2Client, policyBuilderContext.getIdGenerator());
            Element http2ClientGoidElement = createElementWithAttribute(
                    policyBuilderContext.getPolicyDocument(),
                    HTTP2_CLIENT_CONFIG_GOID,
                    GOID_VALUE,
                    id
            );
            assertionElement.insertBefore(http2ClientGoidElement, http2ClientNameEle);
        }
    }

    @Override
    public String getAssertionTagName() {
        return HTTP2_ROUTING_ASSERTION;
    }
}
