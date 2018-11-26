/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.Entity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER_ID;
import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.security.Security.getAlgorithms;
import static org.junit.Assert.*;

/**
 * Utility methods for testing purposes.
 */
public class TestUtils {

    /**
     * Assert contents of both maps are the same. Does not check ordering.
     *
     * @param expected expected map of elements
     * @param actual actual map of elements
     */
    public static void assertPropertiesContent(Map<String, Object> expected, Map<String, Object> actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());

        expected.forEach((key, value) -> {
            assertTrue(actual.containsKey(key));
            assertEquals(value, actual.get(key));
            actual.remove(key);
        });

        assertTrue(actual.isEmpty());
    }

    public static void testDeploymentBundleWithOnlyMapping(EntityBuilder builder,
                                                           Bundle bundle,
                                                           Document document,
                                                           String entityType,
                                                           List<String> expectedEntityNames) {
        final List<Entity> entities = builder.build(bundle, BundleType.DEPLOYMENT, document);
        assertNotNull(entities);
        assertEquals(expectedEntityNames.size(), entities.size());
        entities.forEach(e -> assertOnlyMappingEntity(entityType, expectedEntityNames, e));
    }

    public static void assertOnlyMappingEntity(String entityType, List<String> expectedEntityNames, Entity e) {
        assertNotNull(e.getId());
        assertNull(e.getXml());
        assertNotNull(e.getName());
        assertTrue(expectedEntityNames.contains(e.getName()));
        assertEquals(MappingActions.NEW_OR_EXISTING, e.getMappingAction());
        assertNotNull(e.getType());
        assertEquals(entityType, e.getType());
        assertNotNull(e.getMappingProperties());
        assertFalse(e.getMappingProperties().isEmpty());
        assertEquals(true, e.getMappingProperties().get(FAIL_ON_NEW));
        assertEquals(MappingProperties.NAME, e.getMappingProperties().get(MAP_BY));
        assertEquals(e.getName(), e.getMappingProperties().get(MAP_TO));
    }

    public static Element createCassandraXml(Document document, boolean ciphers, boolean properties) {
        Element cassandraElement = createElementWithAttributesAndChildren(
                document,
                CASSANDRA_CONNECTION,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithTextContent(document, NAME, "Test"),
                createElementWithTextContent(document, KEYSPACE, "Test"),
                createElementWithTextContent(document, CONTACT_POINT, "Test"),
                createElementWithTextContent(document, PORT, 1234),
                createElementWithTextContent(document, USERNAME, "Test"),
                createElementWithTextContent(document, PASSWORD_ID, "password"),
                createElementWithTextContent(document, COMPRESSION, "Test"),
                createElementWithTextContent(document, SSL, true)
        );
        if (ciphers) {
            cassandraElement.appendChild(createElementWithTextContent(document, TLS_CIPHERS, Joiner.on(",").join(getAlgorithms("Cipher"))));
        }
        if (properties) {
            cassandraElement.appendChild(buildPropertiesElement(ImmutableMap.of("testProp", "testValue"), document, PROPERTIES));
        }

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.CASSANDRA_CONNECTION_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        cassandraElement
                )
        );
    }

    public static Element createJdbcXml(Document document) {
        Element jdbcElement = createElementWithAttributesAndChildren(
                document,
                JDBC_CONNECTION,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithTextContent(document, NAME, "Test"),
                createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString())
        );

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_MIN_POOL_SIZE, 1);
        properties.put(PROPERTY_MAX_POOL_SIZE, 2);
        buildAndAppendPropertiesElement(properties, document, jdbcElement);

        Map<String, Object> connectionProperties = new HashMap<>();
        connectionProperties.put(PROPERTY_USER, "gateway");
        connectionProperties.put(PROPERTY_PASSWORD, String.format("${secpass.%s.plaintext}", "gateway"));

        jdbcElement.appendChild(createElementWithChildren(
                document,
                EXTENSION,
                createElementWithTextContent(document, DRIVER_CLASS, "com.ca.driver.Driver"),
                createElementWithTextContent(document, JDBC_URL, "jdbc://db:1234"),
                buildPropertiesElement(connectionProperties, document, CONNECTION_PROPERTIES)
        ));

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.JDBC_CONNECTION),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        jdbcElement
                )
        );
    }

    public static Element createUnsupportedElement(Document document) {
        Element jdbcElement = createElementWithAttributesAndChildren(
                document,
                "l7:Unsupported",
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithTextContent(document, NAME, "Test")
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, "UNSUPPORTED"),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        jdbcElement
                )
        );
    }

    public static Folder createRoot() {
        return createFolder(ROOT_FOLDER_NAME, ROOT_FOLDER_ID, null);
    }

    public static Element createServiceXml(Document document, boolean withProperties, boolean isSoap, boolean isTagEmpty, boolean isWsdlEmpty) {
        Element resourcesElement;
        Element policyResourceSetElement = createElementWithAttributesAndChildren(
                document,
                RESOURCE_SET,
                ImmutableMap.of(ATTRIBUTE_TAG, TAG_VALUE_POLICY),
                createElementWithAttributesAndTextContent(
                        document,
                        RESOURCE,
                        ImmutableMap.of(ATTRIBUTE_TYPE, TAG_VALUE_POLICY),
                        "policy"
                )
        );

        Element wsdlResourceSetElement;
        Map<String, Object> propertiesMap;
        if(isSoap) {
            if(withProperties) {
                propertiesMap = ImmutableMap.of(PREFIX_PROPERTY + "prop", "value", PREFIX_PROPERTY + PREFIX_ENV + "prop", "value2", KEY_VALUE_SOAP, true, KEY_VALUE_SOAP_VERSION, "1.1", KEY_VALUE_WSS_PROCESSING_ENABLED, true);
            } else {
                propertiesMap = Collections.emptyMap();
            }
            wsdlResourceSetElement = createElementWithAttributesAndChildren(
                    document,
                    RESOURCE_SET,
                    ImmutableMap.of(ATTRIBUTE_ROOT_URL, "test.wsdl", ATTRIBUTE_TAG, (isTagEmpty ? "" : TAG_VALUE_WSDL)),
                    createElementWithAttributesAndTextContent(
                            document,
                            RESOURCE,
                            ImmutableMap.of(ATTRIBUTE_SOURCE_URL, "test.wsdl", ATTRIBUTE_TYPE, TAG_VALUE_WSDL),
                            (isWsdlEmpty ? "" : "wsdl file")
                    )
            );
            resourcesElement = createElementWithChildren(
                    document,
                    RESOURCES,
                    policyResourceSetElement,
                    wsdlResourceSetElement
            );
        } else {
            if(withProperties) {
                propertiesMap = ImmutableMap.of(PREFIX_PROPERTY + "prop", "value", PREFIX_PROPERTY + PREFIX_ENV + "prop", "value2");
            } else {
                propertiesMap = Collections.emptyMap();
            }
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

    @NotNull
    public static Folder createFolder(String folderName, String folderId, Folder parent) {
        Folder folder = new Folder();
        folder.setName(folderName);
        folder.setId(folderId);
        folder.setParentFolder(parent);
        return folder;
    }
}
