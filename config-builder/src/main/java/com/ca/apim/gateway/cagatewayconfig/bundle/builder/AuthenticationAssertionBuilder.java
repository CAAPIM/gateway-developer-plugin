package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_NAME;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.STRING_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

public class AuthenticationAssertionBuilder implements PolicyAssertionBuilder {

    @Override
    public void buildAssertionElement(Element assertionElement, PolicyBuilderContext policyBuilderContext) {
        final Bundle bundle = policyBuilderContext.getBundle();
        final Element idProviderNameElement = getSingleChildElement(assertionElement, ID_PROV_NAME, true);
        if (idProviderNameElement != null) {
            final String idProviderName =
                    idProviderNameElement.getAttributes().getNamedItem(STRING_VALUE).getTextContent();
            if(!idProviderName.equals(INTERNAL_IDP_NAME)){
                idProviderNameElement.setAttribute(STRING_VALUE, bundle.applyUniqueName(idProviderName, ENVIRONMENT,
                        false));
            }
        }
    }

    @Override
    public String getAssertionTagName() {
        return AUTHENTICATION;
    }
}
