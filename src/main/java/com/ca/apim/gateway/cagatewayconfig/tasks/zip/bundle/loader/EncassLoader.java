/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Element;

import java.util.List;
import java.util.stream.Collectors;

public class EncassLoader implements BundleEntityLoader {

    private final DocumentTools documentTools;

    EncassLoader(DocumentTools documentTools) {
        this.documentTools = documentTools;
    }

    @Override
    public void load(Bundle bundle, Element element) {
        final Element encassElement = documentTools.getSingleChildElement(documentTools.getSingleChildElement(element, "l7:Resource"), "l7:EncapsulatedAssertion");

        final Element policyReference = documentTools.getSingleChildElement(encassElement, "l7:PolicyReference");
        final String policyId = policyReference.getAttribute("id");
        Element guidElement = documentTools.getSingleChildElement(encassElement, "l7:Guid");
        final String guid = guidElement.getTextContent();

        Encass encass = new Encass();
        encass.setGuid(guid);

        String policyPath = getPath(bundle, policyId);
        bundle.getEncasses().put(policyPath, encass);
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
}
