/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.CassandraConnectionEntity;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.security.Security.getAlgorithms;
import static org.junit.jupiter.api.Assertions.*;

class CassandraConnectionLoaderTest {

    private CassandraConnectionLoader loader = new CassandraConnectionLoader();

    @Test
    void load() {
        final CassandraConnectionEntity entity = loader.load(createCassandraXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true, true));

        assertNotNull(entity);
        assertEquals("id", entity.getId());
        assertEquals("Test", entity.getName());
        assertEquals("Test", entity.getKeyspace());
        assertEquals("Test", entity.getContactPoint());
        assertEquals(new Integer(1234), entity.getPort());
        assertEquals("Test", entity.getUsername());
        assertEquals("password", entity.getPasswordId());
        assertEquals("Test", entity.getCompression());
        assertEquals(true, entity.getSsl());
        assertNotNull(entity.getTlsCiphers());
        assertTrue(entity.getTlsCiphers().containsAll(getAlgorithms("Cipher")));
        assertNotNull(entity.getProperties());
        assertFalse(entity.getProperties().isEmpty());
        assertEquals(1, entity.getProperties().size());
        assertEquals("testValue", entity.getProperties().get("testProp"));
    }

    @Test
    void loadNoCiphersNoProperties() {
        final CassandraConnectionEntity entity = loader.load(createCassandraXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), false, false));

        assertNotNull(entity);
        assertEquals("id", entity.getId());
        assertEquals("Test", entity.getName());
        assertEquals("Test", entity.getKeyspace());
        assertEquals("Test", entity.getContactPoint());
        assertEquals(new Integer(1234), entity.getPort());
        assertEquals("Test", entity.getUsername());
        assertEquals("password", entity.getPasswordId());
        assertEquals("Test", entity.getCompression());
        assertEquals(true, entity.getSsl());
        assertNull(entity.getTlsCiphers());
        assertNull(entity.getProperties());

    }

    @Test
    void entityClass() {
        assertEquals(CassandraConnectionEntity.class, loader.entityClass());
    }

    private static Element createCassandraXml(Document document, boolean ciphers, boolean properties) {
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
            buildAndAppendPropertiesElement(ImmutableMap.of("testProp", "testValue"), document, cassandraElement);
        }

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithChildren(
                        document,
                        RESOURCE,
                        cassandraElement
                )
        );
    }
}