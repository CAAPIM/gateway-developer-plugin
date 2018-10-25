/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyBackedServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.inject.Singleton;
import java.util.Map;
import java.util.TreeMap;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class PolicyBackedServiceLoader implements EntityLoader<PolicyBackedServiceEntity> {

    @Override
    public PolicyBackedServiceEntity load(Element element) {
        final Element policyBackedService = getSingleChildElement(getSingleChildElement(element, RESOURCE), POLICY_BACKED_SERVICE);

        Element nameElement = getSingleChildElement(policyBackedService, NAME);
        final String name = nameElement.getTextContent();
        Element interfaceNameElement = getSingleChildElement(policyBackedService, INTERFACE_NAME);
        final String interfaceName = interfaceNameElement.getTextContent();

        return new PolicyBackedServiceEntity(name, policyBackedService.getAttribute(ATTRIBUTE_ID), interfaceName, buildOperationsMap(policyBackedService));
    }

    private Map<String, String> buildOperationsMap(Element policyBackedService) {
        Map<String, String> operations = new TreeMap<>();

        Element policyBackedServiceOperationsElement = getSingleChildElement(policyBackedService, POLICY_BACKED_SERVICE_OPERATIONS);
        NodeList policyBackedServiceOperationNodeList = policyBackedServiceOperationsElement.getElementsByTagName(POLICY_BACKED_SERVICE_OPERATION);
        for (int i = 0; i < policyBackedServiceOperationNodeList.getLength(); i++) {
            if (!(policyBackedServiceOperationNodeList.item(i) instanceof Element)) {
                throw new WriteException("Unexpected Policy Backed Service Operation node: " + policyBackedServiceOperationsElement.getClass());
            }
            Element policyIdElement = getSingleChildElement((Element) policyBackedServiceOperationNodeList.item(i), POLICY_ID);
            Element operationNameElement = getSingleChildElement((Element) policyBackedServiceOperationNodeList.item(i), OPERATION_NAME);
            operations.put(operationNameElement.getTextContent(), policyIdElement.getTextContent());
        }

        return operations;
    }

    @Override
    public Class<PolicyBackedServiceEntity> entityClass() {
        return PolicyBackedServiceEntity.class;
    }
}