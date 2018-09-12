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
import com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.ENCAPSULATED_ASSERTION_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

public class EncassEntityBuilder implements EntityBuilder {

    private static final String PALETTE_FOLDER = "paletteFolder";
    private static final String INTERNAL_ASSERTIONS = "internalAssertions";

    private final Document document;
    private final IdGenerator idGenerator;

    EncassEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle) {
        return bundle.getEncasses().entrySet().stream().map(encassEntry ->
                buildEncassEntity(bundle, encassEntry.getKey(), encassEntry.getValue())
        ).collect(Collectors.toList());
    }

    private Entity buildEncassEntity(Bundle bundle, String policyPath, Encass encass) {
        Policy policy = bundle.getPolicies().get(policyPath);
        if (policy == null) {
            throw new EntityBuilderException("Could not find policy for encass. Policy Path: " + policyPath);
        }
        final String name = policy.getName();
        final String id = idGenerator.generate();

        Element encassAssertionElement = createElementWithAttributesAndChildren(
                document,
                ENCAPSULATED_ASSERTION,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, GUID, encass.getGuid()),
                createElementWithAttribute(document, POLICY_REFERENCE, ATTRIBUTE_ID, policy.getId()),
                buildArguments(encass),
                buildResults(encass)
        );

        buildAndAppendPropertiesElement(ImmutableMap.of(PALETTE_FOLDER, INTERNAL_ASSERTIONS), document, encassAssertionElement);

        return new Entity(ENCAPSULATED_ASSERTION_TYPE, name, id, encassAssertionElement);
    }

    private Element buildResults(Encass encass) {
        Element encapsulatedResultsElement = document.createElement(ENCAPSULATED_RESULTS);
        if (encass.getResults() != null) {
            encass.getResults().forEach(param -> encapsulatedResultsElement.appendChild(
                    createElementWithChildren(
                            document,
                            ENCAPSULATED_ASSERTION_RESULT,
                            createElementWithTextContent(document, RESULT_NAME, param.getName()),
                            createElementWithTextContent(document, RESULT_TYPE, param.getType())
                    )));
        }
        return encapsulatedResultsElement;
    }

    private Element buildArguments(Encass encass) {
        Element encapsulatedArgumentsElement = document.createElement(ENCAPSULATED_ARGUMENTS);
        if (encass.getArguments() != null) {
            AtomicInteger ordinal = new AtomicInteger(1);
            encass.getArguments().forEach(param -> encapsulatedArgumentsElement.appendChild(
                    createElementWithChildren(
                            document,
                            ENCAPSULATED_ASSERTION_ARGUMENT,
                            createElementWithTextContent(document, ORDINAL, String.valueOf(ordinal.getAndIncrement())),
                            createElementWithTextContent(document, ARGUMENT_NAME, param.getName()),
                            createElementWithTextContent(document, ARGUMENT_TYPE, param.getType()),
                            createElementWithTextContent(document, GUI_PROMPT, Boolean.TRUE.toString())
                    )
            ));
        }
        return encapsulatedArgumentsElement;
    }
}
