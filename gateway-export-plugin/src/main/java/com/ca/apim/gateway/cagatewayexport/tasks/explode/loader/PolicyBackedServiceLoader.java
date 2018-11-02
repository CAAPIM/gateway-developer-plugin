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
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class PolicyBackedServiceLoader implements EntityLoader<PolicyBackedServiceEntity> {

    @Override
    public PolicyBackedServiceEntity load(Element element) {
        final Element policyBackedService = getSingleChildElement(getSingleChildElement(element, RESOURCE), POLICY_BACKED_SERVICE);

        final String name = getSingleChildElementTextContent(policyBackedService, NAME);
        final String interfaceName = getSingleChildElementTextContent(policyBackedService, INTERFACE_NAME);

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
            String policyId = getSingleChildElementTextContent((Element) policyBackedServiceOperationNodeList.item(i), POLICY_ID);
            String operationName = getSingleChildElementTextContent((Element) policyBackedServiceOperationNodeList.item(i), OPERATION_NAME);
            operations.put(operationName, policyId);
        }

        return operations;
    }

    @Override
    public Class<PolicyBackedServiceEntity> entityClass() {
        return PolicyBackedServiceEntity.class;
    }
}