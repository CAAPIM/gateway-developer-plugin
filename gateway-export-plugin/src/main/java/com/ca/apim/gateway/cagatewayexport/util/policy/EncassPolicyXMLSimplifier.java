/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.API_PORTAL_ENCASS_INTEGRATION;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.ENABLED;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;

/**
 * This class is responsible for removing the L7p:ApiPortalEncassIntegration assertion from encass backed policy.
 */
@Singleton
public class EncassPolicyXMLSimplifier {
    private static final Logger LOGGER = Logger.getLogger(EncassPolicyXMLSimplifier.class.getName());
    /**
     *
     * @param policy encass policy.
     * @return true if ApiPortalEncassIntegration assertion is present and enabled else false.
     */
    public String simplifyEncassPolicyXML(Policy policy) {
        Element encassPortalIntegrationElement = null;
        Element encassPortalIntegrationEnabledElement = null;
        try {
            encassPortalIntegrationElement = getSingleElement(policy.getPolicyDocument(), API_PORTAL_ENCASS_INTEGRATION);
        } catch (DocumentParseException e) {
            LOGGER.log(Level.INFO, "ApiPortalEncassIntegration assertion is not found in encass policy : {0}, setting portalTemplate as false : ", policy.getName());
        }

        if (encassPortalIntegrationElement != null) {
            Element encassPortalIntegrationParentElement = (Element) encassPortalIntegrationElement.getParentNode();
            encassPortalIntegrationEnabledElement = getSingleChildElement(encassPortalIntegrationElement, ENABLED, true);
            encassPortalIntegrationParentElement.removeChild(encassPortalIntegrationElement);
        }
        return encassPortalIntegrationElement != null && encassPortalIntegrationEnabledElement == null ? "true" : "false";
    }
}
