/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PORTAL_TEMPLATE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.PolicyLinker.getPolicyPath;

@Singleton
public class EncassLinker implements EntityLinker<Encass> {
    @Override
    public Class<Encass> getEntityClass() {
        return Encass.class;
    }

    @Override
    public void link(Encass encass, Bundle bundle, Bundle targetBundle) {
        Policy policy = bundle.getPolicies().values().stream().filter(p -> encass.getPolicyId().equals(p.getId())).findFirst().orElse(null);
        if (policy == null) {
            throw new LinkerException("Could not find policy for Encapsulated Assertion: " + encass.getName() + ". Policy ID: " + encass.getPolicyId());
        }
        Element encassPortalIntegrationElement = null;
        Element encassPortalIntegrationEnabledElement = null;
        try {
            encassPortalIntegrationElement = getSingleElement(policy.getPolicyDocument(), API_PORTAL_ENCASS_INTEGRATION);
        } catch (DocumentParseException ex) {
            // do nothing
        }
        if (encassPortalIntegrationElement != null) {
            encassPortalIntegrationEnabledElement = getSingleChildElement(encassPortalIntegrationElement, ENABLED, true);
            policy.getPolicyDocument().getFirstChild().removeChild(encassPortalIntegrationElement);
        }
        encass.getProperties().put(PORTAL_TEMPLATE, encassPortalIntegrationElement != null && encassPortalIntegrationEnabledElement == null ? "true" : "false");
        encass.setPolicy(policy.getPath());
        encass.setPath(getPolicyPath(policy, bundle, encass));
    }

}
