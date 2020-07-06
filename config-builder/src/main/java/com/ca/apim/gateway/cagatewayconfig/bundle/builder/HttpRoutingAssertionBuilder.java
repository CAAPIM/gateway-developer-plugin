package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

public class HttpRoutingAssertionBuilder implements PolicyAssertionBuilder {
    @Override
    public void buildAssertionElement(Element assertionElement, PolicyBuilderContext policyBuilderContext) throws DocumentParseException {
        final Bundle bundle = policyBuilderContext.getBundle();
        final Element trustedCertNameElement = getSingleChildElement(assertionElement, TLS_TRUSTED_CERT_NAMES, true);
        if (trustedCertNameElement != null && trustedCertNameElement.getChildNodes().getLength() > 0) {
            Element trustedCertGoidElement = createElementWithAttribute(
                    policyBuilderContext.getPolicyDocument(),
                    TLS_TRUSTED_CERT_IDS,
                    GOID_ARRAY_VALUE,
                    "included"
            );
            assertionElement.insertBefore(trustedCertGoidElement, trustedCertNameElement);

            final NodeList trustedCertNamesList = trustedCertNameElement.getChildNodes();
            for (int i = 0; i < trustedCertNamesList.getLength(); i++) {
                final String trustedCertName = trustedCertNamesList.item(i).getAttributes().getNamedItem(STRING_VALUE).getTextContent();
                final TrustedCert trustedCert = bundle.getTrustedCerts().get(trustedCertName);
                final String trustedCertId = getIdFromAnnotableEntity(trustedCert, policyBuilderContext.getIdGenerator());

                Element trustedCertGoidItem = createElementWithAttribute(
                        policyBuilderContext.getPolicyDocument(),
                        PolicyXMLElements.ITEM,
                        GOID_VALUE,
                        trustedCertId
                );
                trustedCertGoidElement.appendChild(trustedCertGoidItem);
            }
        }
    }

    @Override
    public String getAssertionTagName() {
        return HTTP_ROUTING_ASSERTION;
    }
}
