/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EncassParam;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

@Singleton
public class EncassLoader implements BundleEntityLoader {

    @Override
    public void load(Bundle bundle, Element element) {
        final Element encassElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), ENCAPSULATED_ASSERTION);

        final String policyId = getSingleChildElementAttribute(encassElement, POLICY_REFERENCE, ATTRIBUTE_ID);
        final String name = getSingleChildElementTextContent(encassElement, NAME);
        final String guid = getSingleChildElementTextContent(encassElement, GUID);

        Encass encass = new Encass();
        encass.setGuid(guid);
        encass.setName(name);

        String policyPath = getPath(bundle, policyId);
        encass.setPolicy(policyPath);
        encass.setArguments(getArguments(encassElement));
        encass.setResults(getResults(encassElement));
        encass.setPolicyId(policyId);
        bundle.getEncasses().put(name, encass);
    }

    private String getPath(Bundle bundle, String policyId) {
        List<Policy> policyList = bundle.getPolicies().values().stream().filter(p -> policyId.equals(p.getId())).collect(Collectors.toList());
        if (policyList.isEmpty()) {
            throw new BundleLoadException("Invalid dependency bundle. Could not find policy with id: " + policyId);
        } else if (policyList.size() > 1) {
            throw new BundleLoadException("Invalid dependency bundle. Found multiple policies with id: " + policyId);
        }
        return policyList.get(0).getPath();
    }

    private Set<EncassParam> getResults(Element encass) {
        Element encapsulatedResultsElement = getSingleChildElement(encass, ENCAPSULATED_RESULTS);
        NodeList encapsulatedAssertionResultElement = encapsulatedResultsElement.getElementsByTagName(ENCAPSULATED_ASSERTION_RESULT);
        Set<EncassParam> encassResults = new LinkedHashSet<>(encapsulatedAssertionResultElement.getLength());
        for (int i = 0; i < encapsulatedAssertionResultElement.getLength(); i++) {
            if (!(encapsulatedAssertionResultElement.item(i) instanceof Element)) {
                throw new BundleLoadException("Unexpected encass results node: " + encapsulatedResultsElement.getClass());
            }
            String resultName = getSingleChildElementTextContent((Element) encapsulatedAssertionResultElement.item(i), RESULT_NAME);
            String resultType = getSingleChildElementTextContent((Element) encapsulatedAssertionResultElement.item(i), RESULT_TYPE);
            encassResults.add(new EncassParam(resultName, resultType));
        }
        return encassResults;
    }

    private Set<EncassParam> getArguments(Element encass) {
        Element encapsulatedArgumentsElement = getSingleChildElement(encass, ENCAPSULATED_ARGUMENTS);
        NodeList encapsulatedAssertionArgumentElement = encapsulatedArgumentsElement.getElementsByTagName(ENCAPSULATED_ASSERTION_ARGUMENT);
        Set<EncassParam> encassArguments = new LinkedHashSet<>(encapsulatedAssertionArgumentElement.getLength());
        for (int i = 0; i < encapsulatedAssertionArgumentElement.getLength(); i++) {
            if (!(encapsulatedAssertionArgumentElement.item(i) instanceof Element)) {
                throw new BundleLoadException("Unexpected encass argument node: " + encapsulatedArgumentsElement.getClass());
            }
            String argumentName = getSingleChildElementTextContent((Element) encapsulatedAssertionArgumentElement.item(i), ARGUMENT_NAME);
            String argumentType = getSingleChildElementTextContent((Element) encapsulatedAssertionArgumentElement.item(i), ARGUMENT_TYPE);
            encassArguments.add(new EncassParam(argumentName, argumentType));
        }
        return encassArguments;
    }

    @Override
    public String getEntityType() {
        return EntityTypes.ENCAPSULATED_ASSERTION_TYPE;
    }
}
