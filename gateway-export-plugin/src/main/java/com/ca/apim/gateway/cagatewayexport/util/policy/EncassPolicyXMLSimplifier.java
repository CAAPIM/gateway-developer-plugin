/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.ServiceAndPolicyLoaderUtil;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.L7_TEMPLATE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;

/**
 * This class is responsible for removing the L7p:ApiPortalEncassIntegration assertion from encass backed policy.
 */
@Singleton
public class EncassPolicyXMLSimplifier {
    private static final Logger LOGGER = Logger.getLogger(EncassPolicyXMLSimplifier.class.getName());
    /**
     *
     * @param policyElement encass policy document.
     * @param  encass encapsulated assertion
     */
    public void simplifyEncassPolicyXML(Element policyElement, Encass encass) {
        encass.getProperties().putIfAbsent(L7_TEMPLATE, "false");

        // look for [Set as Portal Publishable Fragment] assertion
        simplifyPortalManagedAssertion(policyElement, encass);
    }

    private void simplifyPortalManagedAssertion(Element policyElement, Encass encass) {
        try {
            Element portalManagedElement = getSingleElement(policyElement, API_PORTAL_ENCASS_INTEGRATION);
            Element enabledElement = getSingleChildElement(portalManagedElement, ENABLED, true);
            Element parentElement = (Element) portalManagedElement.getParentNode();
            Document document = policyElement.getOwnerDocument();
            boolean isEnabled = (enabledElement == null || Boolean.parseBoolean(enabledElement.getAttribute(BOOLEAN_VALUE)));

            encass.getProperties().put(L7_TEMPLATE, Boolean.toString(isEnabled));
            if (isEnabled && ServiceAndPolicyLoaderUtil.migratePortalIntegrationsAssertions()) {
                LOGGER.info("Migrating [Set as Portal Publishable Fragment] assertion for " + encass.getPath());
                parentElement.insertBefore(
                        DocumentUtils.createElementWithChildren(document, COMMENT_ASSERTION,
                                DocumentUtils.createElementWithAttribute(document, COMMENT, STRING_VALUE, "Migrated: Set as Portal Publishable Fragment")),
                        portalManagedElement);
                parentElement.removeChild(portalManagedElement);
            }
        } catch (DocumentParseException e) {
            // ignoring the exception
        }
    }
}
