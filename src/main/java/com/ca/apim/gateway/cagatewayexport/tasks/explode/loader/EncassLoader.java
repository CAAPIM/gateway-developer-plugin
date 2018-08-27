/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElement;

public class EncassLoader implements EntityLoader<EncassEntity> {

    @Override
    public EncassEntity load(Element element) {
        final Element encass = getSingleChildElement(getSingleChildElement(element, RESOURCE), ENCAPSULATED_ASSERTION);

        final Element policyReference = getSingleChildElement(encass, POLICY_REFERENCE);
        final String policyId = policyReference.getAttribute(ATTRIBUTE_ID);
        Element nameElement = getSingleChildElement(encass, NAME);
        final String name = nameElement.getTextContent();
        Element guidElement = getSingleChildElement(encass, GUID);
        final String guid = guidElement.getTextContent();

        return new EncassEntity(name, encass.getAttribute(ATTRIBUTE_ID), guid, policyId, getArguments(encass), getResults(encass));
    }

    private List<EncassEntity.EncassParam> getResults(Element encass) {
        Element encapsulatedResultsElement = getSingleChildElement(encass, ENCAPSULATED_RESULTS);
        NodeList encapsulatedAssertionResultElement = encapsulatedResultsElement.getElementsByTagName(ENCAPSULATED_ASSERTION_RESULT);
        List<EncassEntity.EncassParam> encassResults = new ArrayList<>(encapsulatedAssertionResultElement.getLength());
        for (int i = 0; i < encapsulatedAssertionResultElement.getLength(); i++) {
            if (!(encapsulatedAssertionResultElement.item(i) instanceof Element)) {
                throw new WriteException("Unexpected encass results node: " + encapsulatedResultsElement.getClass());
            }
            Element resultNameElement = getSingleChildElement((Element) encapsulatedAssertionResultElement.item(i), RESULT_NAME);
            Element resultTypeElement = getSingleChildElement((Element) encapsulatedAssertionResultElement.item(i), RESULT_TYPE);
            encassResults.add(new EncassEntity.EncassParam(resultNameElement.getTextContent(), resultTypeElement.getTextContent()));
        }
        return encassResults;
    }

    private List<EncassEntity.EncassParam> getArguments(Element encass) {
        Element encapsulatedArgumentsElement = getSingleChildElement(encass, ENCAPSULATED_ARGUMENTS);
        NodeList encapsulatedAssertionArgumentElement = encapsulatedArgumentsElement.getElementsByTagName(ENCAPSULATED_ASSERTION_ARGUMENT);
        List<EncassEntity.EncassParam> encassArguments = new ArrayList<>(encapsulatedAssertionArgumentElement.getLength());
        for (int i = 0; i < encapsulatedAssertionArgumentElement.getLength(); i++) {
            if (!(encapsulatedAssertionArgumentElement.item(i) instanceof Element)) {
                throw new WriteException("Unexpected encass argument node: " + encapsulatedArgumentsElement.getClass());
            }
            Element argumentNameElement = getSingleChildElement((Element) encapsulatedAssertionArgumentElement.item(i), ARGUMENT_NAME);
            Element argumentTypeElement = getSingleChildElement((Element) encapsulatedAssertionArgumentElement.item(i), ARGUMENT_TYPE);
            encassArguments.add(new EncassEntity.EncassParam(argumentNameElement.getTextContent(), argumentTypeElement.getTextContent()));
        }
        return encassArguments;
    }
}
