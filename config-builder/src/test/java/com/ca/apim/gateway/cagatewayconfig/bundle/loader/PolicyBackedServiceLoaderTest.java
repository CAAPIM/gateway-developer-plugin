/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.beans.PolicyBackedServiceOperation;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class PolicyBackedServiceLoaderTest {

    @Test
    void load() {
        PolicyBackedServiceLoader loader = new PolicyBackedServiceLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument()
        ));

        assertFalse(bundle.getPolicyBackedServices().isEmpty());
        assertEquals(1, bundle.getPolicyBackedServices().size());
        PolicyBackedService policyBackedService = bundle.getPolicyBackedServices().get("service");
        assertNotNull(policyBackedService);
        assertEquals("service", policyBackedService.getName());
        assertEquals("interface", policyBackedService.getInterfaceName());
        assertEquals("id", policyBackedService.getId());
        assertFalse(policyBackedService.getOperations().isEmpty());
        assertEquals(1, policyBackedService.getOperations().size());
        PolicyBackedServiceOperation operation = policyBackedService.getOperations().iterator().next();
        assertEquals("operation", operation.getOperationName());
        assertEquals("policy", operation.getPolicy());
    }

    private static Element createXml(Document document) {
        Element element = createElementWithAttributesAndChildren(
                document,
                POLICY_BACKED_SERVICE,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithTextContent(document, NAME, "service"),
                createElementWithTextContent(document, INTERFACE_NAME, "interface"),
                createElementWithChildren(
                        document,
                        POLICY_BACKED_SERVICE_OPERATIONS,
                        createElementWithChildren(
                            document,
                            POLICY_BACKED_SERVICE_OPERATION,
                            createElementWithTextContent(document, POLICY_ID, "policy"),
                            createElementWithTextContent(document, OPERATION_NAME, "operation")
                        )
                )
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.POLICY_BACKED_SERVICE_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        element
                )
        );
    }
}