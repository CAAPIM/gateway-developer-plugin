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
import com.ca.apim.gateway.cagatewayexport.util.policy.EncassPolicyXMLSimplifier;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PORTAL_TEMPLATE;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.PolicyLinker.getPolicyPath;

@Singleton
public class EncassLinker implements EntityLinker<Encass> {
    private static final Logger LOGGER = Logger.getLogger(EncassLinker.class.getName());
    private final EncassPolicyXMLSimplifier encassPolicyXMLSimplifier;

    @Inject
    EncassLinker(EncassPolicyXMLSimplifier encassPolicyXMLSimplifier) {
        this.encassPolicyXMLSimplifier = encassPolicyXMLSimplifier;
    }

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

        String portalTemplate = "false";
        try {
            portalTemplate = encassPolicyXMLSimplifier.simplifyEncassPolicyXML(policy.getPolicyDocument());
        } catch (DocumentParseException e) {
            LOGGER.log(Level.INFO, "ApiPortalEncassIntegration assertion is not found in encass policy : {0}, setting portalTemplate as false : ", policy.getName());
        }
        encass.getProperties().put(PORTAL_TEMPLATE, portalTemplate);
        encass.setPolicy(policy.getPath());
        encass.setPath(getPolicyPath(policy, bundle, encass));
    }

}
