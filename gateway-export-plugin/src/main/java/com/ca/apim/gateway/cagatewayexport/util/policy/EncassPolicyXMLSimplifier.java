/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.API_PORTAL_ENCASS_INTEGRATION;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.ENABLED;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;

/**
 * This class is responsible for removing the L7p:ApiPortalEncassIntegration assertion from encass backed policy.
 */
@Singleton
public class EncassPolicyXMLSimplifier {
    /**
     *
     * @param policyElement
     * @return true if ApiPortalEncassIntegration assertion is present and enabled else false
     * @throws DocumentParseException
     */
    public String simplifyEncassPolicyXML(Element policyElement) throws DocumentParseException {
        Element encassPortalIntegrationElement = null;
        Element encassPortalIntegrationEnabledElement = null;
        encassPortalIntegrationElement = getSingleElement(policyElement, API_PORTAL_ENCASS_INTEGRATION);

        if (encassPortalIntegrationElement != null) {
            encassPortalIntegrationEnabledElement = getSingleChildElement(encassPortalIntegrationElement, ENABLED, true);
            policyElement.getFirstChild().removeChild(encassPortalIntegrationElement);
        }
        return encassPortalIntegrationElement != null && encassPortalIntegrationEnabledElement == null ? "true" : "false";
    }
}
