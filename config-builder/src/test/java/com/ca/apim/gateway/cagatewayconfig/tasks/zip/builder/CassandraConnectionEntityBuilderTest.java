/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.CassandraConnection;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.TestUtils;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.base.Joiner;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Element;

import java.security.Security;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.junit.jupiter.api.Assertions.*;

class CassandraConnectionEntityBuilderTest {

    private static final String TEST_CASSANDRA_CONNECTION = "TestConnection";
    private static final IdGenerator ID_GENERATOR = new IdGenerator();

    @Test
    void buildFromEmptyBundle_noConnections() {
        CassandraConnectionEntityBuilder builder = new CassandraConnectionEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void buildWithConnection_checkBundleContainsConnection() {
        CassandraConnectionEntityBuilder builder = new CassandraConnectionEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        addPasswordIfNecessary(true, bundle);
        bundle.putAllCassandraConnections(ImmutableMap.of(TEST_CASSANDRA_CONNECTION, buildCassandraConnection()));

        final List<Entity> entities = builder.build(bundle, EntityBuilder.BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());
    }

    @Test
    void buildEmptyDeploymentBundle() {
        TestUtils.testDeploymentBundleWithOnlyMapping(
                new CassandraConnectionEntityBuilder(ID_GENERATOR),
                new Bundle(),
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                EntityTypes.CASSANDRA_CONNECTION_TYPE,
                Collections.emptyList()
        );
    }

    @Test
    void buildDeploymentBundle() {
        final Bundle bundle = new Bundle();
        addPasswordIfNecessary(true, bundle);
        bundle.putAllCassandraConnections(ImmutableMap.of(TEST_CASSANDRA_CONNECTION, buildCassandraConnection()));

        TestUtils.testDeploymentBundleWithOnlyMapping(
                new CassandraConnectionEntityBuilder(ID_GENERATOR),
                bundle,
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument(),
                EntityTypes.CASSANDRA_CONNECTION_TYPE,
                Stream.of("TestConnection").collect(Collectors.toList())
        );
    }

    @Test
    void buildCassandraConnectionEntity() {
        buildAndCheckCassandraConnection(true);
    }

    @Test
    void buildCassandraConnectionMissingPassword() {
        assertThrows(EntityBuilderException.class, () -> buildAndCheckCassandraConnection(false));
    }

    private static void buildAndCheckCassandraConnection(boolean password) {
        CassandraConnectionEntityBuilder builder = new CassandraConnectionEntityBuilder(ID_GENERATOR);
        Bundle bundle = new Bundle();

        addPasswordIfNecessary(password, bundle);

        final Entity entity = builder.buildEntity(bundle, TEST_CASSANDRA_CONNECTION, buildCassandraConnection(), DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertNotNull(entity);
        assertEquals(TEST_CASSANDRA_CONNECTION, entity.getName());
        assertNotNull(entity.getId());
        assertNotNull(entity.getXml());
        assertEquals(EntityTypes.CASSANDRA_CONNECTION_TYPE, entity.getType());

        Element xml = entity.getXml();
        assertEquals(CASSANDRA_CONNECTION, xml.getTagName());
        assertChildElementContent(xml, NAME, TEST_CASSANDRA_CONNECTION);
        assertChildElementContent(xml, ENABLED, Boolean.TRUE.toString());
        assertChildElementContent(xml, KEYSPACE, "Keyspace");
        assertChildElementContent(xml, CONTACT_POINT, "ContactPoint");
        assertChildElementContent(xml, PORT, "1234");
        assertChildElementContent(xml, USERNAME, "User");
        assertChildElementContent(xml, PASSWORD_ID, "PasswordID");
        assertChildElementContent(xml, COMPRESSION, "COMP");
        assertChildElementContent(xml, TLS_CIPHERS, Joiner.on(",").join(Security.getAlgorithms("Cipher")));
        assertNotNull(getSingleChildElement(xml, PROPERTIES));
        assertPropertiesContent(
                ImmutableMap.of("Key", "Value"),
                mapPropertiesElements(getSingleChildElement(xml, PROPERTIES), PROPERTIES)
        );
    }

    private static void addPasswordIfNecessary(boolean password, Bundle bundle) {
        if (password) {
            StoredPassword storedPassword = new StoredPassword();
            storedPassword.setId("PasswordID");
            bundle.getStoredPasswords().put("Password", storedPassword);
        }
    }

    private static void assertChildElementContent(Element xml, String elementName, String expectedValue) {
        assertNotNull(getSingleChildElement(xml, elementName));
        assertEquals(expectedValue, getSingleChildElementTextContent(xml, elementName));
    }

    private static CassandraConnection buildCassandraConnection() {
        CassandraConnection conn = new CassandraConnection();
        conn.setKeyspace("Keyspace");
        conn.setContactPoint("ContactPoint");
        conn.setPort(1234);
        conn.setUsername("User");
        conn.setStoredPasswordName("Password");
        conn.setCompression("COMP");
        conn.setTlsCiphers(Security.getAlgorithms("Cipher"));
        conn.setProperties(ImmutableMap.of("Key", "Value"));
        return conn;
    }
}