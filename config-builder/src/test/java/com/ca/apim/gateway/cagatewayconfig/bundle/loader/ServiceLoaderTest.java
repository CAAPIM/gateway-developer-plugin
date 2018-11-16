/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.beans.PolicyBackedServiceOperation;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.TestUtils;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class ServiceLoaderTest {

    @Test
    void load() {
        ServiceLoader loader = new ServiceLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createCommonXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), false
        ));

        assertFalse(bundle.getServices().isEmpty());
        assertEquals(1, bundle.getServices().size());
        Service service = bundle.getServices().get("service");
        assertNotNull(service);
        assertEquals("service", service.getName());
        assertEquals("/service", service.getUrl());
        assertEquals("id", service.getId());
        assertNotNull(service.getServiceDetailsElement());
        assertFalse(service.getHttpMethods().isEmpty());
        assertEquals(4, service.getHttpMethods().size());
        assertTrue(service.getHttpMethods().containsAll(Arrays.asList("GET", "POST", "PUT", "DELETE")));
        assertNotNull(service.getParentFolder());
        assertEquals("folder", service.getParentFolder().getId());
        assertEquals("policy", service.getPolicy());
        Map<String, Object> expected = new HashMap<>();
        expected.put("prop", "value");
        expected.put("ENV.prop", null);
        assertPropertiesContent(expected, service.getProperties());
    }

    @Test
    void soapServiceLoad() {
        ServiceLoader loader = new ServiceLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createCommonXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true
        ));

        assertFalse(bundle.getServices().isEmpty());
        assertEquals(1, bundle.getServices().size());
        Service service = bundle.getServices().get("service");
        assertNotNull(service);
        assertEquals("service", service.getName());
        assertEquals("/soap-service", service.getUrl());
        assertEquals("soapId", service.getId());
        assertNotNull(service.getServiceDetailsElement());
        assertFalse(service.getHttpMethods().isEmpty());
        assertEquals(4, service.getHttpMethods().size());
        assertTrue(service.getHttpMethods().containsAll(Arrays.asList("GET", "POST", "PUT", "DELETE")));
        assertNotNull(service.getParentFolder());
        assertEquals("folder", service.getParentFolder().getId());
        assertEquals("wsdl file", service.getWsdl().getWsdlXml());
        assertEquals("1.1", service.getWsdl().getSoapVersion());
        Map<String, Object> expected = new HashMap<>();
        expected.put("prop", "value");
        expected.put("ENV.prop", null);
        assertPropertiesContent(expected, service.getProperties());
    }

    private static Element createCommonXml(Document document, boolean isSoap) {
        Element resourcesElement = null;
        Element policyResourceSetElement = createElementWithAttributesAndChildren(
                document,
                RESOURCE_SET,
                ImmutableMap.of(ATTRIBUTE_TAG, ServiceLoader.TAG_VALUE_POLICY),
                createElementWithAttributesAndTextContent(
                        document,
                        RESOURCE,
                        ImmutableMap.of(ATTRIBUTE_TYPE, "policy"),
                        "policy"
                )
        );

        Element wsdlResourceSetElement = null;
        Map<String, Object> propertiesMap = null;
        if(isSoap) {
            propertiesMap = ImmutableMap.of("property.prop", "value", "property.ENV.prop", "value2", ServiceLoader.KEY_VALUE_SOAP, true, ServiceLoader.KEY_VALUE_SOAP_VERSION, "1.1");
            wsdlResourceSetElement = createElementWithAttributesAndChildren(
                    document,
                    RESOURCE_SET,
                    ImmutableMap.of(ATTRIBUTE_ROOT_URL, "test.wsdl", ATTRIBUTE_TAG, ServiceLoader.TAG_VALUE_WSDL),
                    createElementWithAttributesAndTextContent(
                            document,
                            RESOURCE,
                            ImmutableMap.of(ATTRIBUTE_SOURCE_URL, "test.wsdl", ATTRIBUTE_TYPE, "wsdl"),
                            "wsdl file"
                    )
            );
            resourcesElement = createElementWithChildren(
                    document,
                    RESOURCES,
                    policyResourceSetElement,
                    wsdlResourceSetElement
            );
        } else {
            propertiesMap = ImmutableMap.of("property.prop", "value", "property.ENV.prop", "value2");
            resourcesElement = createElementWithChildren(
                    document,
                    RESOURCES,
                    policyResourceSetElement
            );
        }
        Element element = createElementWithAttributesAndChildren(
                document,
                SERVICE,
                ImmutableMap.of(ATTRIBUTE_ID, (isSoap ? "soapId" : "id")),
                createElementWithAttributesAndChildren(
                        document,
                        SERVICE_DETAIL,
                        ImmutableMap.of(ATTRIBUTE_ID, (isSoap ? "soapId" : "id"), ATTRIBUTE_FOLDER_ID, "folder"),
                        createElementWithTextContent(document, NAME, "service"),
                        createElementWithChildren(
                                document,
                                SERVICE_MAPPINGS,
                                createElementWithChildren(
                                        document,
                                        HTTP_MAPPING,
                                        createElementWithTextContent(document, URL_PATTERN, (isSoap ? "/soap-service" : "/service")),
                                        createElementWithChildren(
                                                document,
                                                VERBS,
                                                createElementWithTextContent(document, VERB, "GET"),
                                                createElementWithTextContent(document, VERB, "POST"),
                                                createElementWithTextContent(document, VERB, "PUT"),
                                                createElementWithTextContent(document, VERB, "DELETE")
                                        )
                                )
                        ),
                        buildPropertiesElement(
                                propertiesMap,
                                document
                        )
                ),
                resourcesElement
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, (isSoap ? "soapId" : "id")),
                createElementWithTextContent(document, TYPE, EntityTypes.SERVICE_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        element
                )
        );
    }

    private static Element createXml(Document document) {
        Element element = createElementWithAttributesAndChildren(
                document,
                SERVICE,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithAttributesAndChildren(
                        document,
                        SERVICE_DETAIL,
                        ImmutableMap.of(ATTRIBUTE_ID, "id", ATTRIBUTE_FOLDER_ID, "folder"),
                        createElementWithTextContent(document, NAME, "service"),
                        createElementWithChildren(
                                document,
                                SERVICE_MAPPINGS,
                                createElementWithChildren(
                                        document,
                                        HTTP_MAPPING,
                                        createElementWithTextContent(document, URL_PATTERN, "/service"),
                                        createElementWithChildren(
                                                document,
                                                VERBS,
                                                createElementWithTextContent(document, VERB, "GET"),
                                                createElementWithTextContent(document, VERB, "POST"),
                                                createElementWithTextContent(document, VERB, "PUT"),
                                                createElementWithTextContent(document, VERB, "DELETE")
                                        )
                                )
                        ),
                        buildPropertiesElement(
                                ImmutableMap.of("property.prop", "value", "property.ENV.prop", "value2"),
                                document
                        )
                ),
                createElementWithChildren(
                        document,
                        RESOURCES,
                        createElementWithAttributesAndChildren(
                                document,
                                RESOURCE_SET,
                                ImmutableMap.of(ATTRIBUTE_TAG, "policy"),
                                createElementWithAttributesAndTextContent(
                                        document,
                                        RESOURCE,
                                        ImmutableMap.of(ATTRIBUTE_TYPE, "policy"),
                                        "policy"
                                )
                        )
                )
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.SERVICE_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        element
                )
        );
    }

    private static Element createSoapXml(Document document) {
        Element element = createElementWithAttributesAndChildren(
                document,
                SERVICE,
                ImmutableMap.of(ATTRIBUTE_ID, "soapId"),
                createElementWithAttributesAndChildren(
                        document,
                        SERVICE_DETAIL,
                        ImmutableMap.of(ATTRIBUTE_ID, "soapId", ATTRIBUTE_FOLDER_ID, "folder"),
                        createElementWithTextContent(document, NAME, "service"),
                        createElementWithChildren(
                                document,
                                SERVICE_MAPPINGS,
                                createElementWithChildren(
                                        document,
                                        HTTP_MAPPING,
                                        createElementWithTextContent(document, URL_PATTERN, "/soap-service"),
                                        createElementWithChildren(
                                                document,
                                                VERBS,
                                                createElementWithTextContent(document, VERB, "GET"),
                                                createElementWithTextContent(document, VERB, "POST"),
                                                createElementWithTextContent(document, VERB, "PUT"),
                                                createElementWithTextContent(document, VERB, "DELETE")
                                        )
                                )
                        ),
                        buildPropertiesElement(
                                ImmutableMap.of("property.prop", "value", "property.ENV.prop", "value2", ServiceLoader.KEY_VALUE_SOAP, true, ServiceLoader.KEY_VALUE_SOAP_VERSION, "1.1"),
                                document
                        )
                ),
                createElementWithChildren(
                        document,
                        RESOURCES,
                        createElementWithAttributesAndChildren(
                                document,
                                RESOURCE_SET,
                                ImmutableMap.of(ATTRIBUTE_TAG, ServiceLoader.TAG_VALUE_POLICY),
                                createElementWithAttributesAndTextContent(
                                        document,
                                        RESOURCE,
                                        ImmutableMap.of(ATTRIBUTE_TYPE, "policy"),
                                        "policy"
                                )
                        ),
                        createElementWithAttributesAndChildren(
                                document,
                                RESOURCE_SET,
                                ImmutableMap.of(ATTRIBUTE_ROOT_URL, "test.wsdl", ATTRIBUTE_TAG, ServiceLoader.TAG_VALUE_WSDL),
                                createElementWithAttributesAndTextContent(
                                        document,
                                        RESOURCE,
                                        ImmutableMap.of(ATTRIBUTE_SOURCE_URL, "test.wsdl", ATTRIBUTE_TYPE, "wsdl"),
                                        "wsdl file"
                                )
                        )
                )
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "soapId"),
                createElementWithTextContent(document, TYPE, EntityTypes.SERVICE_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        element
                )
        );
    }
}