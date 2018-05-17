/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyBackedServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Map;
import java.util.TreeMap;

public class PolicyBackedServiceLoader implements EntityLoader {
    @Override
    public Entity load(Element element) {
        final Element policyBackedService = EntityLoaderHelper.getSingleChildElement(EntityLoaderHelper.getSingleChildElement(element, "l7:Resource"), "l7:PolicyBackedService");

        Element nameElement = EntityLoaderHelper.getSingleChildElement(policyBackedService, "l7:Name");
        final String name = nameElement.getTextContent();
        Element interfaceNameElement = EntityLoaderHelper.getSingleChildElement(policyBackedService, "l7:InterfaceName");
        final String interfaceName = interfaceNameElement.getTextContent();

        return new PolicyBackedServiceEntity(name, policyBackedService.getAttribute("id"), interfaceName, policyBackedService, buildOperationsMap(policyBackedService));
    }

    private Map<String, String> buildOperationsMap(Element policyBackedService) {
        Map<String, String> operations = new TreeMap<>();

        Element policyBackedServiceOperationsElement = EntityLoaderHelper.getSingleChildElement(policyBackedService, "l7:PolicyBackedServiceOperations");
        NodeList policyBackedServiceOperationNodeList = policyBackedServiceOperationsElement.getElementsByTagName("l7:PolicyBackedServiceOperation");
        for (int i = 0; i < policyBackedServiceOperationNodeList.getLength(); i++) {
            if (!(policyBackedServiceOperationNodeList.item(i) instanceof Element)) {
                throw new WriteException("Unexpected Policy Backed Service Operation node: " + policyBackedServiceOperationsElement.getClass());
            }
            Element policyIdElement = EntityLoaderHelper.getSingleChildElement((Element) policyBackedServiceOperationNodeList.item(i), "l7:PolicyId");
            Element operationNameElement = EntityLoaderHelper.getSingleChildElement((Element) policyBackedServiceOperationNodeList.item(i), "l7:OperationName");
            operations.put(operationNameElement.getTextContent(), policyIdElement.getTextContent());
        }

        return operations;
    }
}