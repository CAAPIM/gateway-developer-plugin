/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
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

import java.util.*;

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

    public static Element createServiceXml(Document document, boolean withProperties) {
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
                                withProperties ? ImmutableMap.of("property.prop", "value", "property.ENV.prop", "value2") : Collections.emptyMap(),
                                document
                        )
                ),
                createElementWithChildren(
                        document,
                        RESOURCES,
                        createElementWithAttributesAndChildren(
                                document,
                                RESOURCE_SET,
                                ImmutableMap.of("tag", "policy"),
                                createElementWithAttributesAndTextContent(
                                        document,
                                        RESOURCE,
                                        ImmutableMap.of("type", "policy"),
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

    @NotNull
    public static Folder createFolder(String folderName, String folderId, Folder parent) {
        Folder folder = new Folder();
        folder.setName(folderName);
        folder.setId(folderId);
        folder.setParentFolder(parent);
        return folder;
    }

    public static Policy createPolicy(final String name, final String id, final String guid, final String parentFolderId, Element policyElement, String policyString) {
        return createPolicy(name, id, guid, parentFolderId, policyElement, policyString, null);
    }

    public static Policy createPolicy(final String name, final String id, final String guid, final String parentFolderId, Element policyElement, String policyString, String tag) {
        Policy policy = new Policy();
        policy.setName(name);
        policy.setId(id);
        policy.setGuid(guid);
        policy.setParentFolder(new Folder(parentFolderId, null));
        policy.setPolicyDocument(policyElement);
        policy.setPolicyXML(policyString);
        policy.setTag(tag);
        return policy;
    }

    public static CassandraConnection createCassandraConnection(String name, String id) {
        CassandraConnection cassandraConnection = new CassandraConnection();
        cassandraConnection.setName(name);
        cassandraConnection.setId(id);
        return cassandraConnection;
    }

    public static ClusterProperty createClusterProperty(String name, String value, String id) {
        ClusterProperty clusterProperty = new ClusterProperty();
        clusterProperty.setName(name);
        clusterProperty.setValue(value);
        clusterProperty.setId(id);
        return clusterProperty;
    }

    public static Encass createEncass(final String name, final String id, final String guid, String policyId) {
        return createEncass(name, id, guid, policyId, null, null);
    }

    public static Encass createEncass(final String name, final String id, final String guid, String policyId, Set<EncassArgument> arguments, Set<EncassResult> results) {
        Encass encass = new Encass();
        encass.setName(name);
        encass.setId(id);
        encass.setGuid(guid);
        encass.setPolicyId(policyId);
        encass.setArguments(arguments);
        encass.setResults(results);
        return encass;
    }

    public static PolicyBackedService createPolicyBackedService(final String name, final String id, String interfaceName, final Map<String, String> operations) {
        PolicyBackedService pbs = new PolicyBackedService();
        pbs.setName(name);
        pbs.setId(id);
        pbs.setInterfaceName(interfaceName);
        pbs.setOperations(new LinkedHashSet<>());
        operations.forEach((k,v) -> pbs.getOperations().add(new PolicyBackedServiceOperation(k, v)));
        return pbs;
    }

    public static Service createService(final String name, final String id, final Folder parentFolder, Element serviceDetailsElement, String policy) {
        Service service = new Service();
        service.setName(name);
        service.setId(id);
        service.setParentFolder(parentFolder);
        service.setServiceDetailsElement(serviceDetailsElement);
        service.setPolicy(policy);
        return service;
    }
}
