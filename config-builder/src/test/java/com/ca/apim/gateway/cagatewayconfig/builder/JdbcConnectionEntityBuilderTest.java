/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.TestUtils;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

class JdbcConnectionEntityBuilderTest {

    private static final String GATEWAY_USER = "gateway";
    private static final String PASSWORD_REF = "gateway";
    private static final String TEST_DRIVER_CLASS = "com.database.Driver";
    private static final String TEST_JDBC_CONNECTION = "TestConnection";
    private static final String TEST_JDBC_URL = "jdbc:test//localhost:12345/testdb";
    private static final IdGenerator ID_GENERATOR = new IdGenerator();

    @Test
    void buildFromEmptyBundle_noConnections() {
        JdbcConnectionEntityBuilder builder = new JdbcConnectionEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void buildWithConnection_checkBundleContainsConnection() {
        JdbcConnectionEntityBuilder builder = new JdbcConnectionEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        bundle.putAllJdbcConnections(ImmutableMap.of(TEST_JDBC_CONNECTION, buildJdbcConnection(emptyMap())));

        final List<Entity> entities = builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());
    }

    @Test
    void buildJdbcConnectionEntityDefaults() {
        buildAndCheckJdbcConnection(emptyMap());
    }

    @Test
    void buildJdbcConnectionEntityCustomProperties() {
        buildAndCheckJdbcConnection(ImmutableMap.of(PROPERTY_MIN_POOL_SIZE, 10, PROPERTY_MAX_POOL_SIZE, 50));
    }

    private static void buildAndCheckJdbcConnection(Map<String, Object> properties) {
        JdbcConnectionEntityBuilder builder = new JdbcConnectionEntityBuilder(ID_GENERATOR);
        final Entity entity = builder.buildEntity(TEST_JDBC_CONNECTION, buildJdbcConnection(properties), DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertNotNull(entity);
        assertEquals(TEST_JDBC_CONNECTION, entity.getName());
        assertNotNull(entity.getId());
        assertNotNull(entity.getXml());
        assertEquals(EntityTypes.JDBC_CONNECTION, entity.getType());

        Element xml = entity.getXml();
        assertEquals(JDBC_CONNECTION, xml.getTagName());
        assertNotNull(getSingleChildElement(xml, NAME));
        assertEquals(TEST_JDBC_CONNECTION, getSingleChildElementTextContent(xml, NAME));
        assertNotNull(getSingleChildElement(xml, ENABLED));
        assertEquals(Boolean.TRUE.toString(), getSingleChildElementTextContent(xml, ENABLED));
        assertNotNull(getSingleChildElement(xml, PROPERTIES));
        assertPropertiesContent(
                properties,
                mapPropertiesElements(getSingleChildElement(xml, PROPERTIES), PROPERTIES)
        );
        Element extension = getSingleChildElement(xml, EXTENSION);
        assertNotNull(extension);
        assertNotNull(getSingleChildElement(extension, DRIVER_CLASS));
        assertEquals(TEST_DRIVER_CLASS, getSingleChildElementTextContent(extension, DRIVER_CLASS));
        assertNotNull(getSingleChildElement(extension, JDBC_URL));
        assertEquals(TEST_JDBC_URL, getSingleChildElementTextContent(extension, JDBC_URL));
        assertNotNull(getSingleChildElement(extension, CONNECTION_PROPERTIES));
        assertPropertiesContent(
                ImmutableMap.of(
                        PROPERTY_USER, GATEWAY_USER,
                        PROPERTY_PASSWORD, "${secpass." + PASSWORD_REF + ".plaintext}"
                ),
                mapPropertiesElements(getSingleChildElement(extension, CONNECTION_PROPERTIES), CONNECTION_PROPERTIES)
        );
    }

    private static JdbcConnection buildJdbcConnection(Map<String, Object> properties) {
        JdbcConnection jdbcConnection = new JdbcConnection();
        jdbcConnection.setUser(GATEWAY_USER);
        jdbcConnection.setPasswordRef(PASSWORD_REF);
        jdbcConnection.setDriverClass(TEST_DRIVER_CLASS);
        jdbcConnection.setJdbcUrl(TEST_JDBC_URL);
        jdbcConnection.setMinimumPoolSize((Integer) properties.get(PROPERTY_MIN_POOL_SIZE));
        jdbcConnection.setMaximumPoolSize((Integer) properties.get(PROPERTY_MAX_POOL_SIZE));

        return jdbcConnection;
    }

    @Test
    void buildEmptyDeploymentBundle() {
        TestUtils.testDeploymentBundleWithOnlyMapping(
                new JdbcConnectionEntityBuilder(ID_GENERATOR),
                new Bundle(),
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                EntityTypes.JDBC_CONNECTION,
                Collections.emptyList()
        );
    }

    @Test
    void buildDeploymentBundle() {
        final Bundle bundle = new Bundle();
        bundle.putAllJdbcConnections(ImmutableMap.of(TEST_JDBC_CONNECTION, buildJdbcConnection(emptyMap())));

        TestUtils.testDeploymentBundleWithOnlyMapping(
                new JdbcConnectionEntityBuilder(ID_GENERATOR),
                bundle,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                EntityTypes.JDBC_CONNECTION,
                Stream.of(TEST_JDBC_CONNECTION).collect(Collectors.toList())
        );
    }
}