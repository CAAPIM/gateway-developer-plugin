/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EncassEntityBuilder implements EntityBuilder {
    private final Document document;
    private final IdGenerator idGenerator;

    public EncassEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle) {
        return bundle.getEncasses().entrySet().stream().map(encassEntry ->
                buildEncassEntity(bundle, encassEntry.getKey(), encassEntry.getValue())
        ).collect(Collectors.toList());
    }

    private Entity buildEncassEntity(Bundle bundle, String policyPath, Encass encass) {
        Element encassAssertionElement = document.createElement("l7:EncapsulatedAssertion");

        Policy policy = bundle.getPolicies().get(policyPath);
        if (policy == null) {
            throw new EntityBuilderException("Could not find policy for encass. Policy Path: " + policyPath);
        }

        String id = idGenerator.generate();
        encassAssertionElement.setAttribute("id", id);

        Element nameElement = document.createElement("l7:Name");
        final String name;
        name = policy.getName();

        nameElement.setTextContent(name);

        encassAssertionElement.appendChild(nameElement);

        Element guidElement = document.createElement("l7:Guid");
        guidElement.setTextContent(encass.getGuid());
        encassAssertionElement.appendChild(guidElement);

        Element policyReferenceElement = document.createElement("l7:PolicyReference");
        policyReferenceElement.setAttribute("id", policy.getId());
        encassAssertionElement.appendChild(policyReferenceElement);

        encassAssertionElement.appendChild(buildArguments(encass));
        encassAssertionElement.appendChild(buildResults(encass));

        Element propertiesElement = document.createElement("l7:Properties");
        encassAssertionElement.appendChild(propertiesElement);
        Element propertyElement = document.createElement("l7:Property");
        propertiesElement.appendChild(propertyElement);
        propertyElement.setAttribute("key", "paletteFolder");
        Element stringValueElement = document.createElement("l7:StringValue");
        propertyElement.appendChild(stringValueElement);
        stringValueElement.setTextContent("internalAssertions");

        return new Entity("ENCAPSULATED_ASSERTION", name, id, encassAssertionElement);
    }

    private Element buildResults(Encass encass) {
        Element encapsulatedResultsElement = document.createElement("l7:EncapsulatedResults");
        if (encass.getResults() != null) {
            encass.getResults().forEach(param -> {
                Element encapsulatedAssertionResultElement = document.createElement("l7:EncapsulatedAssertionResult");
                Element resultNameElement = document.createElement("l7:ResultName");
                resultNameElement.setTextContent(param.getName());
                encapsulatedAssertionResultElement.appendChild(resultNameElement);
                Element resultTypeElement = document.createElement("l7:ResultType");
                resultTypeElement.setTextContent(param.getType());
                encapsulatedAssertionResultElement.appendChild(resultTypeElement);
                encapsulatedResultsElement.appendChild(encapsulatedAssertionResultElement);
            });
        }
        return encapsulatedResultsElement;
    }

    private Element buildArguments(Encass encass) {
        Element encapsulatedArgumentsElement = document.createElement("l7:EncapsulatedArguments");
        if (encass.getArguments() != null) {
            AtomicInteger ordinal = new AtomicInteger(1);
            encass.getArguments().forEach(param -> {
                Element encapsulatedAssertionArgumentElement = document.createElement("l7:EncapsulatedAssertionArgument");
                Element ordinalElement = document.createElement("l7:Ordinal");
                ordinalElement.setTextContent(String.valueOf(ordinal.getAndIncrement()));
                encapsulatedAssertionArgumentElement.appendChild(ordinalElement);
                Element argumentNameElement = document.createElement("l7:ArgumentName");
                argumentNameElement.setTextContent(param.getName());
                encapsulatedAssertionArgumentElement.appendChild(argumentNameElement);
                Element argumentTypeElement = document.createElement("l7:ArgumentType");
                argumentTypeElement.setTextContent(param.getType());
                encapsulatedAssertionArgumentElement.appendChild(argumentTypeElement);
                Element guiPromptElement = document.createElement("l7:GuiPrompt");
                guiPromptElement.setTextContent("true");
                encapsulatedAssertionArgumentElement.appendChild(guiPromptElement);
                encapsulatedArgumentsElement.appendChild(encapsulatedAssertionArgumentElement);
            });
        }
        return encapsulatedArgumentsElement;
    }
}
