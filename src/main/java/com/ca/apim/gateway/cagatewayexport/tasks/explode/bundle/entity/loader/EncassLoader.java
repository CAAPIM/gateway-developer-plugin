/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class EncassLoader implements EntityLoader {
    @Override
    public Entity load(Element element) {
        final Element encass = EntityLoaderHelper.getSingleChildElement(EntityLoaderHelper.getSingleChildElement(element, "l7:Resource"), "l7:EncapsulatedAssertion");

        final Element policyReference = EntityLoaderHelper.getSingleChildElement(encass, "l7:PolicyReference");
        final String policyId = policyReference.getAttribute("id");
        Element nameElement = EntityLoaderHelper.getSingleChildElement(encass, "l7:Name");
        final String name = nameElement.getTextContent();
        Element guidElement = EntityLoaderHelper.getSingleChildElement(encass, "l7:Guid");
        final String guid = guidElement.getTextContent();

        return new EncassEntity(name, encass.getAttribute("id"), guid, encass, policyId, getArguments(encass), getResults(encass));
    }

    private List<EncassEntity.EncassParam> getResults(Element encass) {
        Element encapsulatedResultsElement = EntityLoaderHelper.getSingleChildElement(encass, "l7:EncapsulatedResults");
        NodeList encapsulatedAssertionResultElement = encapsulatedResultsElement.getElementsByTagName("l7:EncapsulatedAssertionResult");
        List<EncassEntity.EncassParam> encassResults = new ArrayList<>(encapsulatedAssertionResultElement.getLength());
        for (int i = 0; i < encapsulatedAssertionResultElement.getLength(); i++) {
            if (!(encapsulatedAssertionResultElement.item(i) instanceof Element)) {
                throw new WriteException("Unexpected encass results node: " + encapsulatedResultsElement.getClass());
            }
            Element resultNameElement = EntityLoaderHelper.getSingleChildElement((Element) encapsulatedAssertionResultElement.item(i), "l7:ResultName");
            Element resultTypeElement = EntityLoaderHelper.getSingleChildElement((Element) encapsulatedAssertionResultElement.item(i), "l7:ResultType");
            encassResults.add(new EncassEntity.EncassParam(resultNameElement.getTextContent(), resultTypeElement.getTextContent()));
        }
        return encassResults;
    }

    private List<EncassEntity.EncassParam> getArguments(Element encass) {
        Element encapsulatedArgumentsElement = EntityLoaderHelper.getSingleChildElement(encass, "l7:EncapsulatedArguments");
        NodeList encapsulatedAssertionArgumentElement = encapsulatedArgumentsElement.getElementsByTagName("l7:EncapsulatedAssertionArgument");
        List<EncassEntity.EncassParam> encassArguments = new ArrayList<>(encapsulatedAssertionArgumentElement.getLength());
        for (int i = 0; i < encapsulatedAssertionArgumentElement.getLength(); i++) {
            if (!(encapsulatedAssertionArgumentElement.item(i) instanceof Element)) {
                throw new WriteException("Unexpected encass argument node: " + encapsulatedArgumentsElement.getClass());
            }
            Element argumentNameElement = EntityLoaderHelper.getSingleChildElement((Element) encapsulatedAssertionArgumentElement.item(i), "l7:ArgumentName");
            Element argumentTypeElement = EntityLoaderHelper.getSingleChildElement((Element) encapsulatedAssertionArgumentElement.item(i), "l7:ArgumentType");
            encassArguments.add(new EncassEntity.EncassParam(argumentNameElement.getTextContent(), argumentTypeElement.getTextContent()));
        }
        return encassArguments;
    }
}
