/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.*;
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
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(encassElement, PROPERTIES, true), PROPERTIES);
        //Removing redundant policyGuid, since encass already has a reference to policyId
        properties.remove("policyGuid");

        Encass encass = new Encass();
        encass.setGuid(guid);
        encass.setName(name);
        encass.setProperties(properties);

        String policyPath = getPath(bundle, policyId);
        encass.setId(encassElement.getAttribute(ATTRIBUTE_ID));
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

    private Set<EncassResult> getResults(Element encass) {
        Element encapsulatedResultsElement = getSingleChildElement(encass, ENCAPSULATED_RESULTS);
        NodeList encapsulatedAssertionResultElement = encapsulatedResultsElement.getElementsByTagName(ENCAPSULATED_ASSERTION_RESULT);
        Set<EncassResult> encassResults = new LinkedHashSet<>(encapsulatedAssertionResultElement.getLength());
        for (Node node : nodeList(encapsulatedAssertionResultElement)) {
            if (!(node instanceof Element)) {
                throw new BundleLoadException("Unexpected encass results node: " + encapsulatedResultsElement.getClass());
            }
            final Element encassResultElement = (Element) node;
            String resultName = getSingleChildElementTextContent(encassResultElement, RESULT_NAME);
            String resultType = getSingleChildElementTextContent(encassResultElement, RESULT_TYPE);
            encassResults.add(new EncassResult(resultName, resultType));
        }
        return encassResults;
    }

    private Set<EncassArgument> getArguments(Element encass) {
        Element encapsulatedArgumentsElement = getSingleChildElement(encass, ENCAPSULATED_ARGUMENTS);
        NodeList encapsulatedAssertionArgumentElement = encapsulatedArgumentsElement.getElementsByTagName(ENCAPSULATED_ASSERTION_ARGUMENT);
        Set<EncassArgument> encassArguments = new LinkedHashSet<>(encapsulatedAssertionArgumentElement.getLength());
        for (Node node : nodeList(encapsulatedAssertionArgumentElement)) {
            if (!(node instanceof Element)) {
                throw new BundleLoadException("Unexpected encass argument node: " + encapsulatedArgumentsElement.getClass());
            }
            final Element encassArgElement = (Element) node;
            String argumentName = getSingleChildElementTextContent(encassArgElement, ARGUMENT_NAME);
            String argumentType = getSingleChildElementTextContent(encassArgElement, ARGUMENT_TYPE);
            Boolean guiPrompt = Boolean.parseBoolean(getSingleChildElementTextContent(encassArgElement, GUI_PROMPT));
            encassArguments.add(new EncassArgument(argumentName, argumentType, guiPrompt));
        }
        return encassArguments;
    }

    @Override
    public String getEntityType() {
        return EntityTypes.ENCAPSULATED_ASSERTION_TYPE;
    }
}
