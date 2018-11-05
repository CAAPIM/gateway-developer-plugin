/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.PolicyBackedServiceOperation;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.inject.Singleton;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class PolicyBackedServiceLoader implements BundleDependencyLoader {

    @Override
    public void load(final Bundle bundle, Element element) {
        final Element policyBackedService = getSingleChildElement(getSingleChildElement(element, RESOURCE), POLICY_BACKED_SERVICE);

        Element nameElement = getSingleChildElement(policyBackedService, NAME);
        final String name = nameElement.getTextContent();
        Element interfaceNameElement = getSingleChildElement(policyBackedService, INTERFACE_NAME);
        final String interfaceName = interfaceNameElement.getTextContent();

        PolicyBackedService pbs = new PolicyBackedService();
        pbs.setId(policyBackedService.getAttribute(ATTRIBUTE_ID));
        pbs.setName(name);
        pbs.setInterfaceName(interfaceName);
        pbs.setOperations(buildOperations(policyBackedService));

        bundle.getPolicyBackedServices().put(name, pbs);
    }

    private Set<PolicyBackedServiceOperation> buildOperations(Element policyBackedService) {
        Set<PolicyBackedServiceOperation> operations = new LinkedHashSet<>();

        Element policyBackedServiceOperationsElement = getSingleChildElement(policyBackedService, POLICY_BACKED_SERVICE_OPERATIONS);
        NodeList policyBackedServiceOperationNodeList = policyBackedServiceOperationsElement.getElementsByTagName(POLICY_BACKED_SERVICE_OPERATION);
        for (int i = 0; i < policyBackedServiceOperationNodeList.getLength(); i++) {
            if (!(policyBackedServiceOperationNodeList.item(i) instanceof Element)) {
                throw new DependencyBundleLoadException("Unexpected Policy Backed Service Operation node: " + policyBackedServiceOperationsElement.getClass());
            }
            Element policyIdElement = getSingleChildElement((Element) policyBackedServiceOperationNodeList.item(i), POLICY_ID);
            Element operationNameElement = getSingleChildElement((Element) policyBackedServiceOperationNodeList.item(i), OPERATION_NAME);

            operations.add(new PolicyBackedServiceOperation(operationNameElement.getTextContent(), policyIdElement.getTextContent()));
        }

        return operations;
    }

    @Override
    public String getEntityType() {
        return EntityTypes.POLICY_BACKED_SERVICE_TYPE;
    }
}
