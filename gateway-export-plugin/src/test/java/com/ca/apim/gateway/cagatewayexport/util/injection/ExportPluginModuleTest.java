/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.injection;

import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.ExplodeBundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntitiesLinker;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntityLinkerRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriterRegistry;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for dependency injection configuration
 */
class ExportPluginModuleTest {

    private Injector injector;

    @BeforeEach
    void before() {
        injector = ExportPluginModule.create();
    }

    @Test
    void checkDefaultInstances() {
        assertEquals(DocumentTools.INSTANCE, injector.getInstance(DocumentTools.class), "DocumentTools is not the default");
        assertEquals(JsonTools.INSTANCE, injector.getInstance(JsonTools.class), "JsonTools is not the default");
        assertEquals(DocumentFileUtils.INSTANCE, injector.getInstance(DocumentFileUtils.class), "DocumentFileUtils is not the default");
        assertEquals(PolicyXMLSimplifier.INSTANCE, injector.getInstance(PolicyXMLSimplifier.class), "PolicyXMLSimplifier is not the default");
    }

    @Test
    void checkReflectionsIsSet() {
        assertNotNull(injector.getInstance(Reflections.class));
    }

    @Test
    void checkLinkersAndRegistry() {
        final List<Binding<EntitiesLinker>> linkersBindings = injector.findBindingsByType(TypeLiteral.get(EntitiesLinker.class));
        assertNotNull(linkersBindings);
        assertFalse(linkersBindings.isEmpty());

        final List<EntitiesLinker> linkers = linkersBindings.stream().map(b -> injector.getInstance(b.getKey())).collect(toList());
        assertFalse(linkers.isEmpty());
        assertEquals(linkersBindings.size(), linkers.size());

        final EntityLinkerRegistry registry = injector.getInstance(EntityLinkerRegistry.class);
        assertNotNull(registry);
        assertNotNull(registry.getEntityLinkers());
        assertFalse(registry.getEntityLinkers().isEmpty());
        assertEquals(registry.getEntityLinkers().size(), linkers.size());
        assertTrue(registry.getEntityLinkers().containsAll(linkers));
    }

    @Test
    void checkWritersAndRegistry() {
        final List<Binding<EntityWriter>> writersBindings = injector.findBindingsByType(TypeLiteral.get(EntityWriter.class));
        assertNotNull(writersBindings);
        assertFalse(writersBindings.isEmpty());

        final List<EntityWriter> writers = writersBindings.stream().map(b -> injector.getInstance(b.getKey())).collect(toList());
        assertFalse(writers.isEmpty());
        assertEquals(writersBindings.size(), writers.size());

        final EntityWriterRegistry registry = injector.getInstance(EntityWriterRegistry.class);
        assertNotNull(registry);
        assertNotNull(registry.getEntityWriters());
        assertFalse(registry.getEntityWriters().isEmpty());
        assertEquals(registry.getEntityWriters().size(), writers.size());
        assertTrue(registry.getEntityWriters().containsAll(writers));
    }

    @Test
    void checkExplodeBundleImpl() {
        assertNotNull(injector.getInstance(ExplodeBundle.class));
    }
}