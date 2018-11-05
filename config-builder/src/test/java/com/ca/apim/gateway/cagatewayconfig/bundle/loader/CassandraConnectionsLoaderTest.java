/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.CassandraConnection;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.createCassandraXml;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static java.security.Security.getAlgorithms;
import static org.junit.jupiter.api.Assertions.*;

class CassandraConnectionsLoaderTest {

    private CassandraConnectionsLoader loader = new CassandraConnectionsLoader();

    @Test
    void load() {
        Bundle bundle = new Bundle();
        loader.load(bundle, createCassandraXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true, true));

        assertFalse(bundle.getCassandraConnections().isEmpty());
        assertEquals(1, bundle.getCassandraConnections().size());
        assertNotNull(bundle.getCassandraConnections().get("Test"));

        CassandraConnection entity = bundle.getCassandraConnections().get("Test");
        assertNotNull(entity);
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
        Bundle bundle = new Bundle();
        loader.load(bundle, createCassandraXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), false, false));

        assertFalse(bundle.getCassandraConnections().isEmpty());
        assertEquals(1, bundle.getCassandraConnections().size());
        assertNotNull(bundle.getCassandraConnections().get("Test"));

        CassandraConnection entity = bundle.getCassandraConnections().get("Test");
        assertNotNull(entity);
        assertEquals("Test", entity.getKeyspace());
        assertEquals("Test", entity.getContactPoint());
        assertEquals(new Integer(1234), entity.getPort());
        assertEquals("Test", entity.getUsername());
        assertEquals("password", entity.getPasswordId());
        assertEquals("Test", entity.getCompression());
        assertEquals(true, entity.getSsl());
        assertNull(entity.getTlsCiphers());
        assertNotNull(entity.getProperties());
        assertTrue(entity.getProperties().isEmpty());

    }

}