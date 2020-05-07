/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderHelper.getEntityWithNameMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.POLICY_BACKED_SERVICE_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.Collections.emptyList;

@Singleton
public class PolicyBackedServiceEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 600;
    private final IdGenerator idGenerator;

    @Inject
    PolicyBackedServiceEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        return buildEntities(bundle.getPolicyBackedServices(), bundle, bundleType, document);
    }

    private List<Entity> buildEntities(Map<String, ?> entities, Bundle bundle, BundleType bundleType, Document document) {
        // no pbs has to be added to environment bundle
        if (bundleType == ENVIRONMENT) {
            return emptyList();
        }

        return entities.entrySet().stream().map(pbsEntry ->
                buildPBSEntity(bundle, pbsEntry.getKey(), (PolicyBackedService)pbsEntry.getValue(), document)
        ).collect(Collectors.toList());
    }

    @Override
    public List<Entity> build(Map<Class, Map<String, GatewayEntity>> entityMap, AnnotatedEntity annotatedEntity, Bundle bundle, BundleType bundleType, Document document) {
        Map<String, GatewayEntity> map = entityMap.get(PolicyBackedService.class);
        return buildEntities(map, bundle, bundleType, document);
    }

    private Entity buildPBSEntity(Bundle bundle, String name, PolicyBackedService policyBackedService, Document document) {
        String id = idGenerator.generate();
        Element policyBackedServiceElement = createElementWithAttributesAndChildren(
                document,
                POLICY_BACKED_SERVICE,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, INTERFACE_NAME, policyBackedService.getInterfaceName()),
                buildOperations(policyBackedService, bundle, document)
        );

        return getEntityWithNameMapping(POLICY_BACKED_SERVICE_TYPE, name, id, policyBackedServiceElement);
    }

    private Element buildOperations(PolicyBackedService policyBackedService, Bundle bundle, Document document) {
        Element policyBackedServiceOperationsElement = document.createElement(POLICY_BACKED_SERVICE_OPERATIONS);
        if (policyBackedService.getOperations() != null) {
            policyBackedService.getOperations().forEach(operation -> {
                Policy policy = bundle.getPolicies().get(operation.getPolicy());
                if (policy == null) {
                    throw new EntityBuilderException("Could not find policy for policy backed service. Policy Path: " + operation.getPolicy());
                }

                policyBackedServiceOperationsElement.appendChild(
                        createElementWithChildren(
                                document,
                                POLICY_BACKED_SERVICE_OPERATION,
                                createElementWithTextContent(document, POLICY_ID, policy.getId()),
                                createElementWithTextContent(document, OPERATION_NAME, operation.getOperationName())
                        )
                );
            });
        }
        return policyBackedServiceOperationsElement;
    }

    @Override
    public Integer getOrder() {
        return ORDER;
    }
}
