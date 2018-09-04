/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.POLICY_BACKED_SERVICE_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

public class PolicyBackedServiceEntityBuilder implements EntityBuilder {
    private final Document document;
    private final IdGenerator idGenerator;

    PolicyBackedServiceEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle) {
        return bundle.getPolicyBackedServices().entrySet().stream().map(pbsEntry ->
                buildPBSEntity(bundle, pbsEntry.getKey(), pbsEntry.getValue())
        ).collect(Collectors.toList());
    }

    private Entity buildPBSEntity(Bundle bundle, String name, PolicyBackedService policyBackedService) {
        String id = idGenerator.generate();
        Element policyBackedServiceElement = createElementWithAttribute(document, POLICY_BACKED_SERVICE, ATTRIBUTE_ID, id);

        policyBackedServiceElement.appendChild(createElementWithTextContent(document, NAME, name));
        policyBackedServiceElement.appendChild(createElementWithTextContent(document, INTERFACE_NAME, policyBackedService.getInterfaceName()));
        policyBackedServiceElement.appendChild(buildOperations(policyBackedService, bundle));

        return new Entity(POLICY_BACKED_SERVICE_TYPE, name, id, policyBackedServiceElement);
    }

    private Element buildOperations(PolicyBackedService policyBackedService, Bundle bundle) {
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
}
