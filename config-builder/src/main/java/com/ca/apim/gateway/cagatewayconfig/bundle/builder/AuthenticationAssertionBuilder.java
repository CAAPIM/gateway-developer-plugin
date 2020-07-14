package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_ID;
import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

public class AuthenticationAssertionBuilder implements PolicyAssertionBuilder {

    @Override
    public void buildAssertionElement(Element assertionElement, PolicyBuilderContext policyBuilderContext) {
        final Bundle bundle = policyBuilderContext.getBundle();
        final IdGenerator idGenerator = policyBuilderContext.getIdGenerator();
        final Element idProviderNameElement = getSingleChildElement(assertionElement, ID_PROV_NAME, true);
        if (idProviderNameElement != null) {
            final String idProviderName =
                    idProviderNameElement.getAttributes().getNamedItem(STRING_VALUE).getTextContent();
            String id;
            if (idProviderName.equals(INTERNAL_IDP_NAME)) {
                id = INTERNAL_IDP_ID;
            } else {
                final IdentityProvider identityProvider = bundle.getIdentityProviders().get(idProviderName);
                if (identityProvider != null) {
                    if (identityProvider.getAnnotatedEntity() != null && identityProvider.getAnnotatedEntity().getId() != null) {
                        id = identityProvider.getAnnotatedEntity().getId();
                    } else {
                        id = identityProvider.getId();
                    }
                } else {
                    id = idGenerator.generate();
                }
            }

            Element idProviderGoidElement = createElementWithAttribute(
                    policyBuilderContext.getPolicyDocument(),
                    ID_PROV_OID,
                    GOID_VALUE,
                    id
            );
            assertionElement.insertBefore(idProviderGoidElement, idProviderNameElement);
            assertionElement.removeChild(idProviderNameElement);
        }
    }

    @Override
    public String getAssertionTagName() {
        return AUTHENTICATION;
    }
}
