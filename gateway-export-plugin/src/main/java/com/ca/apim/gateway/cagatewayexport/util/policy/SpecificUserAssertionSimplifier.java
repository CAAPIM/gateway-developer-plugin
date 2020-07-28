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
 * Simplifier for Specific User assertion.
 */
@Singleton
public class SpecificUserAssertionSimplifier implements PolicyAssertionSimplifier {
    private static final Logger LOGGER = Logger.getLogger(SpecificUserAssertionSimplifier.class.getName());

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) throws DocumentParseException {
        Element specificUserAssertionElement = context.getAssertionElement();
        Bundle bundle = context.getBundle();

        final Element idProviderGoidElement = getSingleElement(specificUserAssertionElement, ID_PROV_OID);
        final String idProviderGoid = idProviderGoidElement.getAttribute(GOID_VALUE);
        final Optional<IdentityProvider> idProv = bundle.getEntities(IdentityProvider.class).values().stream().filter(e -> e.getId().equals(idProviderGoid)).findAny();
        if (idProv.isPresent()) {
            updateSpecificUserAssertionElement(specificUserAssertionElement, idProviderGoidElement, idProv.get().getName());
        } else if (INTERNAL_IDP_ID.equals(idProviderGoid)) {
            updateSpecificUserAssertionElement(specificUserAssertionElement, idProviderGoidElement, INTERNAL_IDP_NAME);
        } else {
            LOGGER.log(Level.WARNING, "Could not find referenced identity provider with id: {0}", idProviderGoid);
        }
    }

    private static void updateSpecificUserAssertionElement(Element specificUserAssertionElement, Element goidElementToRemove, String internalIdpName) {
        final Node firstChild = specificUserAssertionElement.getFirstChild();
        final Element idProviderNameElement = createElementWithAttribute(specificUserAssertionElement.getOwnerDocument(), ID_PROV_NAME, STRING_VALUE, internalIdpName);
        if (firstChild != null) {
            specificUserAssertionElement.insertBefore(idProviderNameElement, firstChild);
        } else {
            specificUserAssertionElement.appendChild(idProviderNameElement);
        }
        specificUserAssertionElement.removeChild(goidElementToRemove);
    }

    @Override
    public String getAssertionTagName() {
        return SPECIFIC_USER;
    }
}
