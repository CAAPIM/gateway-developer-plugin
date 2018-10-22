/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.CassandraConnectionEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans.CassandraConnection;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TemporaryFolderExtension.class)
class CassandraConnectionWriterTest {

    private CassandraConnectionWriter writer = new CassandraConnectionWriter(DocumentFileUtils.INSTANCE, JsonTools.INSTANCE);

    @Test
    void testGetBean() {
        CassandraConnectionEntity entity = new CassandraConnectionEntity.Builder()
                .username("username")
                .keyspace("keyspace")
                .contactPoint("contactPoint")
                .port(1234)
                .compression("compression")
                .ssl(true)
                .tlsCiphers(Stream.of("Cipher").collect(Collectors.toSet()))
                .properties(ImmutableMap.of("prop1", "value1"))
                .build();
        entity.setPasswordName("passwordName");

        final CassandraConnection bean = writer.getBean(entity);
        assertNotNull(bean);
        assertEquals(entity.getUsername(), bean.getUsername());
        assertEquals(entity.getKeyspace(), bean.getKeyspace());
        assertEquals(entity.getContactPoint(), bean.getContactPoint());
        assertEquals(entity.getPort(), bean.getPort());
        assertEquals(entity.getCompression(), bean.getCompression());
        assertEquals(entity.getSsl(), bean.getSsl());
        assertNotNull(bean.getTlsCiphers());
        assertTrue(bean.getTlsCiphers().containsAll(entity.getTlsCiphers()));
        assertNotNull(bean.getProperties());
        assertFalse(bean.getProperties().isEmpty());
        assertEquals(1, bean.getProperties().size());
        assertEquals("value1", bean.getProperties().get("prop1"));
    }
}