/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.CassandraConnection;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.StoredPassword;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CassandraConnectionLinkerTest {

    private CassandraConnectionLinker linker = new CassandraConnectionLinker();

    @Test
    void testLinkNoPasswordId() {
        CassandraConnection cassandraConnection = new CassandraConnection();
        linker.link(cassandraConnection, null, null);

        assertNull(cassandraConnection.getStoredPasswordName());
    }

    @Test
    void testLinkPasswordNotFound() {
        CassandraConnection cassandraConnection = new CassandraConnection.Builder().passwordId("123").build();
        assertThrows(LinkerException.class, () -> linker.link(cassandraConnection, new Bundle(), null));
    }

    @Test
    void testLinkWithPassword() {
        CassandraConnection cassandraConnection = new CassandraConnection.Builder().passwordId("123").build();
        Bundle bundle = new Bundle();
        bundle.addEntity(new StoredPassword.Builder().name("name").id("123").build());

        linker.link(cassandraConnection, bundle, null);

        assertNotNull(cassandraConnection.getStoredPasswordName());
        assertEquals("name", cassandraConnection.getStoredPasswordName());
    }

    @Test
    void testGetEntityClass() {
        assertEquals(CassandraConnection.class, linker.getEntityClass());
    }
}