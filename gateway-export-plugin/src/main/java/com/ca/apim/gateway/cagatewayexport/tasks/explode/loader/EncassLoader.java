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

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class EncassLoader implements EntityLoader<EncassEntity> {

    @Override
    public EncassEntity load(Element element) {
        final Element encass = getSingleChildElement(getSingleChildElement(element, RESOURCE), ENCAPSULATED_ASSERTION);

        final String policyId = getSingleChildElementAttribute(encass, POLICY_REFERENCE, ATTRIBUTE_ID);
        final String name = getSingleChildElementTextContent(encass, NAME);
        final String guid = getSingleChildElementTextContent(encass, GUID);

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
            String resultName = getSingleChildElementTextContent((Element) encapsulatedAssertionResultElement.item(i), RESULT_NAME);
            String resultType = getSingleChildElementTextContent((Element) encapsulatedAssertionResultElement.item(i), RESULT_TYPE);
            encassResults.add(new EncassEntity.EncassParam(resultName, resultType));
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
            String argumentName = getSingleChildElementTextContent((Element) encapsulatedAssertionArgumentElement.item(i), ARGUMENT_NAME);
            String argumentType = getSingleChildElementTextContent((Element) encapsulatedAssertionArgumentElement.item(i), ARGUMENT_TYPE);
            encassArguments.add(new EncassEntity.EncassParam(argumentName, argumentType));
        }
        return encassArguments;
    }

    @Override
    public Class<EncassEntity> entityClass() {
        return EncassEntity.class;
    }
}
