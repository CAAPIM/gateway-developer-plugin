/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderHelper.getEntityWithNameMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.ENCAPSULATED_ASSERTION_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.DEFAULT_PALETTE_FOLDER_LOCATION;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PALETTE_FOLDER;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.lang.Boolean.FALSE;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

/**
 * Builder for encass - refer to {@link EntityBuilder} javadoc for more information.
 */
@Singleton
public class EncassEntityBuilder implements EntityBuilder {

    private static final int ORDER = 300;

    private final IdGenerator idGenerator;

    @Inject
    EncassEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        return buildEntities(bundle.getEncasses(), bundle, bundleType, document);
    }

    private List<Entity> buildEntities(Map<String, ?> entities, Bundle bundle, BundleType bundleType, Document document) {
        // no encass has to be added to environment bundle
        if (bundleType == ENVIRONMENT) {
            return emptyList();
        }

        return entities.entrySet().stream().map(encassEntry ->
                buildEncassEntity(bundle, encassEntry.getKey(), (Encass) encassEntry.getValue(), document)
        ).collect(Collectors.toList());
    }

    @Override
    public List<Entity> build(Map<Class, Map<String, GatewayEntity>> entityMap, AnnotatedEntity annotatedEntity, Bundle bundle, BundleType bundleType, Document document) {
        Map<String, GatewayEntity> map = Optional.ofNullable(entityMap.get(Encass.class)).orElse(Collections.emptyMap());
        return buildEntities(map, bundle, bundleType, document);
    }

    @NotNull
    @Override
    public Integer getOrder() {
        return ORDER;
    }

    private Entity buildEncassEntity(Bundle bundle, String name, Encass encass, Document document) {
        Policy policy = bundle.getPolicies().get(encass.getPolicy());
        if (policy == null) {
            throw new EntityBuilderException("Could not find policy for encass. Policy Path: " + encass.getPolicy());
        }
        final String id = idGenerator.generate();

        Element encassAssertionElement = createElementWithAttributesAndChildren(
                document,
                ENCAPSULATED_ASSERTION,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, GUID, encass.getGuid()),
                createElementWithAttribute(document, POLICY_REFERENCE, ATTRIBUTE_ID, policy.getId()),
                buildArguments(encass, document),
                buildResults(encass, document)
        );

        final Map<String, Object> properties = Optional.ofNullable(encass.getProperties()).orElse(new HashMap<>());
        properties.putIfAbsent(PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION);
        buildAndAppendPropertiesElement(properties, document, encassAssertionElement);
        return getEntityWithNameMapping(ENCAPSULATED_ASSERTION_TYPE, name, id, encassAssertionElement);
    }

    private Element buildResults(Encass encass, Document document) {
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

    private Element buildArguments(Encass encass, Document document) {
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
                            createElementWithTextContent(document, GUI_LABEL, param.getLabel()),
                            createElementWithTextContent(document, GUI_PROMPT, firstNonNull(param.getRequireExplicit(), FALSE).toString())
                    )
            ));
        }
        return encapsulatedArgumentsElement;
    }
}
