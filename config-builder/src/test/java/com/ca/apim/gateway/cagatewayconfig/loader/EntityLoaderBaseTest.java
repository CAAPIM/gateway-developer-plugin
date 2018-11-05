/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityLoaderBaseTest {

    @Mock
    private JsonTools jsonTools;

    @Test
    void loadSingle() {
        AtomicBoolean putToBundle = new AtomicBoolean(false);
        EntityLoader loader = new TestEntityLoader(putToBundle);

        loader.load(null, "test", "{\"name\":\"custom\"}");
        assertTrue(putToBundle.get());

        putToBundle.set(false);
        loader.load(null, "test.json", "{\"name\":\"custom\"}");
        assertTrue(putToBundle.get());
    }

    @Test
    void loadSingleYaml() {
        AtomicBoolean putToBundle = new AtomicBoolean(false);
        EntityLoader loader = new TestEntityLoader(putToBundle);

        loader.load(null, "test.yaml", "name: custom");
        assertTrue(putToBundle.get());

        putToBundle.set(false);
        loader.load(null, "test.yml", "{\"name\":\"custom\"}");
        assertTrue(putToBundle.get());
    }

    @Test
    void loadFile() {
        AtomicBoolean putToBundle = new AtomicBoolean(false);
        EntityLoader loader = new TestEntityLoader(jsonTools, putToBundle);

        when(jsonTools.getDocumentFileFromConfigDir(any(File.class), any(String.class))).thenReturn(new File(""));
        when(jsonTools.readDocumentFile(any(File.class), any(JavaType.class))).thenReturn(ImmutableMap.<String, TestEntity>builder().put("test", new TestEntity("custom")).build());
        when(jsonTools.getTypeFromFile(any(File.class))).thenReturn(JsonTools.JSON);
        when(jsonTools.getObjectMapper(any(String.class))).thenReturn(JsonTools.INSTANCE.getObjectMapper(JsonTools.JSON));

        loader.load(null, new File(""));
        assertTrue(putToBundle.get());
    }

    private static class TestEntityLoader extends EntityLoaderBase<TestEntity> {
        private final AtomicBoolean putToBundle;

        public TestEntityLoader() {
            this(new AtomicBoolean(false));
        }

        TestEntityLoader(AtomicBoolean putToBundle) {
            this(JsonTools.INSTANCE, putToBundle);
        }

        TestEntityLoader(JsonTools jsonTools, AtomicBoolean putToBundle) {
            super(jsonTools);
            this.putToBundle = putToBundle;
        }

        @Override
        protected Class<TestEntity> getBeanClass() {
            return TestEntity.class;
        }

        @Override
        protected String getFileName() {
            return "test.file";
        }

        @Override
        protected void putToBundle(Bundle bundle, @NotNull Map<String, TestEntity> entitiesMap) {
            putToBundle.set(true);
            assertEquals(1, entitiesMap.size());
            TestEntity entity = entitiesMap.get("test");
            assertNotNull(entity);
            assertEquals("custom", entity.name);
        }

        @Override
        public String getEntityType() {
            return "TEST_LOADER";
        }
    }

    static class TestEntity {
        String name;

        public TestEntity() {
        }

        public TestEntity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}