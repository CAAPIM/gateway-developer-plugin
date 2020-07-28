/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.MissingGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;

/**
 * Simplifier for encass elements.
 */
@Singleton
public class EncapsulatedAssertionSimplifier implements PolicyAssertionSimplifier {

    private static final Logger LOGGER = Logger.getLogger(EncapsulatedAssertionSimplifier.class.getName());

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) throws DocumentParseException {
        Element encapsulatedAssertionElement = context.getAssertionElement();
        Bundle bundle = context.getBundle();
        Bundle resultantBundle = context.getResultantBundle();

        Element encassGuidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        String encassGuid = encassGuidElement.getAttribute(STRING_VALUE);

        // Look for the referenced encass from the resultant-bundle (i.e., filtered bundle)
        // if it is not found, we should considered it as missing-entity.
        //  - if the entity is found in the original exported bundle, missing-entity will be marked as excluded.
        //  - otherwise, it will be marked as not-excluded, i.e., entity might be missing from the gateway itself.
        // NOTE: Same rule is applicable to policies as well.
        Optional<Encass> resultantEncassEntity = resultantBundle.getEntities(Encass.class).values().stream().filter(e -> encassGuid.equals(e.getGuid())).findAny();
        if (resultantEncassEntity.isPresent()) {
            Optional<Policy> resultantPolicyEntity = resultantBundle.getPolicies().values().stream().filter(p -> resultantEncassEntity.get().getPolicyId().equals(p.getId())).findFirst();
            if (resultantPolicyEntity.isPresent()) {
                encapsulatedAssertionElement.setAttribute("encassName", resultantEncassEntity.get().getName());
                Element encapsulatedAssertionConfigNameElement = getSingleChildElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME, true);
                if (encapsulatedAssertionConfigNameElement != null) {
                    encapsulatedAssertionElement.removeChild(encapsulatedAssertionConfigNameElement);
                }
                encapsulatedAssertionElement.removeChild(encassGuidElement);
            } else {
                Optional<Policy> policyEntity = bundle.getPolicies().values().stream().filter(p -> resultantEncassEntity.get().getPolicyId().equals(p.getId())).findFirst();
                if (!policyEntity.isPresent()) {
                    LOGGER.log(Level.WARNING, "Could not find referenced encass policy with id: {0}", resultantEncassEntity.get().getPolicyId());
                }
                simplifyAssertionElementForMissingEntity(context, encapsulatedAssertionElement, policyEntity.isPresent());
            }
        } else {
            Optional<Encass> encassEntity = bundle.getEntities(Encass.class).values().stream().filter(e -> encassGuid.equals(e.getGuid())).findAny();
            if (!encassEntity.isPresent()) {
                LOGGER.log(Level.WARNING, "Could not find referenced encass with guid: {0}", encassGuid);
            }
            simplifyAssertionElementForMissingEntity(context, encapsulatedAssertionElement, encassEntity.isPresent());
        }
    }

    private void simplifyAssertionElementForMissingEntity(final PolicySimplifierContext context, final Element encassAssertionElement, final boolean excluded) {
        final Element encassGuidElement = getSingleChildElement(encassAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        final Element encassNameElement = getSingleChildElement(encassAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME, true);

        final MissingGatewayEntity missingEntity = new MissingGatewayEntity();
        missingEntity.setType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
        missingEntity.setGuid(encassGuidElement.getAttribute(STRING_VALUE));
        missingEntity.setName(encassNameElement != null ? encassNameElement.getAttribute(STRING_VALUE) : "Encass#" + missingEntity.getGuid());
        missingEntity.setId(missingEntity.getGuid().replace("-", ""));
        missingEntity.setExcluded(excluded);
        context.getResultantBundle().addEntity(missingEntity);

        encassAssertionElement.setAttribute("encassName", missingEntity.getName());
        encassAssertionElement.removeChild(encassGuidElement);
        if (encassNameElement != null) {
            encassAssertionElement.removeChild(encassNameElement);
        }

        LOGGER.log(Level.WARNING, "Recording the referenced encass with guid: {0} as missing entity",
                new Object[] {missingEntity.getGuid(), missingEntity.getName()});
    }

    @Override
    public String getAssertionTagName() {
        return ENCAPSULATED;
    }
}
