/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_ID;
import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;

/**
 * Simplifier for authentication assertion.
 */
@Singleton
public class AuthenticationAssertionSimplifier implements PolicyAssertionSimplifier {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationAssertionSimplifier.class.getName());

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) throws DocumentParseException {
        Element authenticationAssertionElement = context.getAssertionElement();
        Bundle bundle = context.getBundle();

        final Element idProviderGoidElement = getSingleElement(authenticationAssertionElement, ID_PROV_OID);
        final String idProviderGoid = idProviderGoidElement.getAttribute(GOID_VALUE);
        final Optional<IdentityProvider> idProv = bundle.getEntities(IdentityProvider.class).values().stream().filter(e -> e.getId().equals(idProviderGoid)).findAny();
        if (idProv.isPresent()) {
            updateAuthenticationAssertionElement(authenticationAssertionElement, idProviderGoidElement, idProv.get().getName());
        } else if (INTERNAL_IDP_ID.equals(idProviderGoid)) {
            updateAuthenticationAssertionElement(authenticationAssertionElement, idProviderGoidElement, INTERNAL_IDP_NAME);
        } else {
            LOGGER.log(Level.WARNING, "Could not find referenced identity provider with id: {0}", idProviderGoid);
        }
    }

    private static void updateAuthenticationAssertionElement(Element authenticationAssertionElement, Element goidElementToRemove, String internalIdpName) {
        final Node firstChild = authenticationAssertionElement.getFirstChild();
        final Element idProviderNameElement = createElementWithAttribute(authenticationAssertionElement.getOwnerDocument(), ID_PROV_NAME, STRING_VALUE, internalIdpName);
        if (firstChild != null) {
            authenticationAssertionElement.insertBefore(idProviderNameElement, firstChild);
        } else {
            authenticationAssertionElement.appendChild(idProviderNameElement);
        }
        authenticationAssertionElement.removeChild(goidElementToRemove);
    }

    @Override
    public String getAssertionTagName() {
        return AUTHENTICATION;
    }
}
