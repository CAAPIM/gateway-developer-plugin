/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
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
        if (bundle instanceof AnnotatedBundle) {
            AnnotatedBundle annotatedBundle = ((AnnotatedBundle) bundle);
            Map<String, Encass> map = Optional.ofNullable(bundle.getEncasses()).orElse(Collections.emptyMap());
            return buildEntities(map, annotatedBundle, annotatedBundle.getFullBundle(), bundleType, document);
        } else {
            return buildEntities(bundle.getEncasses(), null, bundle, bundleType, document);
        }
    }

    private List<Entity> buildEntities(Map<String, ?> entities, AnnotatedBundle annotatedBundle, Bundle bundle, BundleType bundleType, Document document) {
        // no encass has to be added to environment bundle
        if (bundleType == ENVIRONMENT) {
            return emptyList();
        }

        return entities.entrySet().stream().map(encassEntry ->
                buildEncassEntity(annotatedBundle, bundle, encassEntry.getKey(), (Encass) encassEntry.getValue(), document)
        ).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Integer getOrder() {
        return ORDER;
    }

    private boolean isAnnotatedEntity(Encass encass, AnnotatedEntity annotatedEntity) {
        return annotatedEntity != null && annotatedEntity.getEntityName().equals(encass.getName()) &&
                EntityTypes.ENCAPSULATED_ASSERTION_TYPE.equals(annotatedEntity.getEntityType());
    }

    private Entity buildEncassEntity(AnnotatedBundle annotatedBundle, Bundle bundle, String name, Encass encass, Document document) {
        Policy policy = bundle.getPolicies().get(encass.getPolicy());
        if (policy == null) {
            throw new EntityBuilderException("Could not find policy for encass. Policy Path: " + encass.getPolicy());
        }
        String encassName = name;
        String guid = encass.getGuid();
        String id = encass.getId();
        AnnotatedEntity annotatedEntity = annotatedBundle != null ? annotatedBundle.getAnnotatedEntity() : null;
        final boolean isRedeployableBundle = annotatedEntity != null && annotatedEntity.isBundle() && annotatedEntity.isRedeployable();
        if (!encass.isReusable() && !isAnnotatedEntity(encass, annotatedEntity)) {
            if(annotatedBundle != null){
                encassName = annotatedBundle.getUniquePrefix() + name + annotatedBundle.getUniqueSuffix();
            }
            //guid and id are regenerated in policy entity builder if this encass is referred by policy
            //no need to regenerate ids here
        }

        Element encassAssertionElement = createElementWithAttributesAndChildren(
                document,
                ENCAPSULATED_ASSERTION,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, encassName),
                createElementWithTextContent(document, GUID, guid),
                createElementWithAttribute(document, POLICY_REFERENCE, ATTRIBUTE_ID, policy.getId()),
                buildArguments(encass, document),
                buildResults(encass, document)
        );

        final Map<String, Object> properties = Optional.ofNullable(encass.getProperties()).orElse(new HashMap<>());
        properties.putIfAbsent(PALETTE_FOLDER, DEFAULT_PALETTE_FOLDER_LOCATION);
        buildAndAppendPropertiesElement(properties, document, encassAssertionElement);
        Entity entity = getEntityWithNameMapping(ENCAPSULATED_ASSERTION_TYPE, encassName, id, encassAssertionElement);
        if ((!encass.isReusable() && !isAnnotatedEntity(encass, annotatedEntity)) || isRedeployableBundle) {
            entity.setMappingAction(MappingActions.NEW_OR_UPDATE);
        } else {
            entity.setMappingAction(MappingActions.NEW_OR_EXISTING);
        }
        return entity;

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
