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

public class PolicyBackedServiceEntityBuilder implements EntityBuilder {
    private final Document document;
    private final IdGenerator idGenerator;

    public PolicyBackedServiceEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle) {
        return bundle.getPolicyBackedServices().entrySet().stream().map(pbsEntry ->
                buildPBSEntity(bundle, pbsEntry.getKey(), pbsEntry.getValue())
        ).collect(Collectors.toList());
    }

    private Entity buildPBSEntity(Bundle bundle, String name, PolicyBackedService policyBackedService) {
        Element policyBackedServiceElement = document.createElement("l7:PolicyBackedService");

        String id = idGenerator.generate();
        policyBackedServiceElement.setAttribute("id", id);

        Element nameElement = document.createElement("l7:Name");
        nameElement.setTextContent(name);
        policyBackedServiceElement.appendChild(nameElement);

        Element interfaceNameElement = document.createElement("l7:InterfaceName");
        interfaceNameElement.setTextContent(policyBackedService.getInterfaceName());
        policyBackedServiceElement.appendChild(interfaceNameElement);

        policyBackedServiceElement.appendChild(buildOperations(policyBackedService, bundle));

        return new Entity("POLICY_BACKED_SERVICE", name, id, policyBackedServiceElement);
    }

    private Element buildOperations(PolicyBackedService policyBackedService, Bundle bundle) {
        Element policyBackedServiceOperationsElement = document.createElement("l7:PolicyBackedServiceOperations");
        if (policyBackedService.getOperations() != null) {
            policyBackedService.getOperations().forEach(operation -> {
                Policy policy = bundle.getPolicies().get(operation.getPolicy());
                if (policy == null) {
                    throw new EntityBuilderException("Could not find policy for policy backed service. Policy Path: " + operation.getPolicy());
                }

                Element policyBackedServiceOperationElement = document.createElement("l7:PolicyBackedServiceOperation");
                Element policyIdElement = document.createElement("l7:PolicyId");
                policyIdElement.setTextContent(String.valueOf(policy.getId()));
                policyBackedServiceOperationElement.appendChild(policyIdElement);
                Element operationNameElement = document.createElement("l7:OperationName");
                operationNameElement.setTextContent(operation.getOperationName());
                policyBackedServiceOperationElement.appendChild(operationNameElement);
                policyBackedServiceOperationsElement.appendChild(policyBackedServiceOperationElement);
            });
        }
        return policyBackedServiceOperationsElement;
    }
}
