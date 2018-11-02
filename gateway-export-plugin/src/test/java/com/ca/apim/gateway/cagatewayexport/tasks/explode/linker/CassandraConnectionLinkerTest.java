/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.CassandraConnectionEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.StoredPasswordEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CassandraConnectionLinkerTest {

    private CassandraConnectionLinker linker = new CassandraConnectionLinker();

    @Test
    void testLinkNoPasswordId() {
        CassandraConnectionEntity cassandraConnection = new CassandraConnectionEntity.Builder().build();
        linker.link(cassandraConnection, null, null);

        assertNull(cassandraConnection.getPasswordName());
    }

    @Test
    void testLinkPasswordNotFound() {
        CassandraConnectionEntity cassandraConnection = new CassandraConnectionEntity.Builder().passwordId("123").build();
        assertThrows(LinkerException.class, () -> linker.link(cassandraConnection, new Bundle(), null));
    }

    @Test
    void testLinkWithPassword() {
        CassandraConnectionEntity cassandraConnection = new CassandraConnectionEntity.Builder().passwordId("123").build();
        Bundle bundle = new Bundle();
        bundle.addEntity(new StoredPasswordEntity.Builder().name("name").id("123").build());

        linker.link(cassandraConnection, bundle, null);

        assertNotNull(cassandraConnection.getPasswordName());
        assertEquals("name", cassandraConnection.getPasswordName());
    }

    @Test
    void testGetEntityClass() {
        assertEquals(CassandraConnectionEntity.class, linker.getEntityClass());
    }
}