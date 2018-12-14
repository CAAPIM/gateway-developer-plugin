/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityTypeRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import java.io.File;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@Extensions({ @ExtendWith(MockitoExtension.class) })
class EntityLoaderRegistryTest {

    @Mock
    private EntityTypeRegistry entityTypeRegistry;

    @Test
    void getLoader() {
        when(entityTypeRegistry.getEntityTypeMap()).thenReturn(Collections.emptyMap());

        EntityLoaderRegistry registry = new EntityLoaderRegistry(
                Stream.of(new TestEntityLoader()).collect(Collectors.toSet()),
                entityTypeRegistry,
                JsonTools.INSTANCE,
                new IdGenerator(),
                FileUtils.INSTANCE
        );

        assertNotNull(registry.getEntityLoaders());
        assertNotNull(registry.getLoader("ENTITY"));
        assertNull(registry.getLoader("LOADER"));
    }

    public static class TestEntityLoader implements EntityLoader {
        @Override
        public Object loadSingle(String name, File entitiesFile) {
            return null;
        }

        @Override
        public void load(Bundle bundle, File rootDir) {
            //
        }

        @Override
        public void load(Bundle bundle, String name, String value) {

        }

        @Override
        public String getEntityType() {
            return "ENTITY";
        }
    }
}