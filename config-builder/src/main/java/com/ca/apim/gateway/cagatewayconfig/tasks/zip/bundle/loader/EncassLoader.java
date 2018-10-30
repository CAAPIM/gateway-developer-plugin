/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class EncassLoader implements BundleDependencyLoader {

    @Override
    public void load(Bundle bundle, Element element) {
        final Element encassElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), ENCAPSULATED_ASSERTION);
        final String encassName = getSingleChildElementTextContent(encassElement, NAME);
        final Element policyReference = getSingleChildElement(encassElement, POLICY_REFERENCE);
        final String policyId = policyReference.getAttribute(ATTRIBUTE_ID);
        Element guidElement = getSingleChildElement(encassElement, GUID);
        final String guid = guidElement.getTextContent();

        Encass encass = new Encass();
        encass.setGuid(guid);

        String policyPath = getPath(bundle, policyId);
        encass.setPolicy(policyPath);
        bundle.getEncasses().put(encassName, encass);
    }

    private String getPath(Bundle bundle, String policyId) {
        List<Policy> policyList = bundle.getPolicies().values().stream().filter(p -> policyId.equals(p.getId())).collect(Collectors.toList());
        if (policyList.isEmpty()) {
            throw new DependencyBundleLoadException("Invalid dependency bundle. Could not find policy with id: " + policyId);
        } else if (policyList.size() > 1) {
            throw new DependencyBundleLoadException("Invalid dependency bundle. Found multiple policies with id: " + policyId);
        }
        return policyList.get(0).getPath();
    }

    @Override
    public String getEntityType() {
        return EntityTypes.ENCAPSULATED_ASSERTION_TYPE;
    }
}
