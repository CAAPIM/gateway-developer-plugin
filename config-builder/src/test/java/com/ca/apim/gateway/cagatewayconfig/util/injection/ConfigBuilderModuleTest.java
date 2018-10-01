/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.injection;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.*;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.BundleDependencyLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.DependencyLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

class ConfigBuilderModuleTest {

    private Injector injector;

    @BeforeEach
    void before() {
        injector = ConfigBuilderModule.create();
    }

    @Test
    void checkDefaultInstances() {
        assertEquals(DocumentTools.INSTANCE, injector.getInstance(DocumentTools.class), "DocumentTools is not the default");
        assertEquals(JsonTools.INSTANCE, injector.getInstance(JsonTools.class), "JsonTools is not the default");
        assertEquals(DocumentFileUtils.INSTANCE, injector.getInstance(DocumentFileUtils.class), "DocumentFileUtils is not the default");
        assertEquals(FileUtils.INSTANCE, injector.getInstance(FileUtils.class), "FileUtils is not the default");
        assertNotNull(injector.getInstance(IdGenerator.class), "IdGenerator is not available");
    }

    @Test
    void checkReflectionsIsSet() {
        assertNotNull(injector.getInstance(Reflections.class));
    }

    @Test
    void checkEntityBuilders() {
        final List<Binding<EntityBuilder>> entityBuilderBindings = injector.findBindingsByType(TypeLiteral.get(EntityBuilder.class));
        assertNotNull(entityBuilderBindings);
        assertFalse(entityBuilderBindings.isEmpty());

        final List<EntityBuilder> builders = entityBuilderBindings.stream().map(b -> injector.getInstance(b.getKey())).collect(toList());
        assertFalse(builders.isEmpty());
        assertEquals(entityBuilderBindings.size(), builders.size());

        final List<EntityBuilder> sortedBuilders = new ArrayList<>(new TreeSet<>(builders));
        if (sortedBuilders.size() != builders.size()) {
            final List<String> missing = new ArrayList<>();
            builders.forEach(b -> {
                if (!sortedBuilders.contains(b)) {
                    missing.add(b.getClass().getSimpleName());
                }
            });
            fail("Following builders are missing while sorted by order: " + Arrays.toString(missing.toArray()) + ". Maybe an order conflict?");
        }

        final BundleEntityBuilder bundleEntityBuilder = injector.getInstance(BundleEntityBuilder.class);
        assertNotNull(bundleEntityBuilder);
        assertNotNull(bundleEntityBuilder.getEntityBuilders());
        assertFalse(bundleEntityBuilder.getEntityBuilders().isEmpty());
        assertEquals(bundleEntityBuilder.getEntityBuilders().size(), builders.size());
        assertTrue(bundleEntityBuilder.getEntityBuilders().containsAll(builders));

        assertNotNull(injector.getInstance(BundleDocumentBuilder.class));
        assertNotNull(injector.getInstance(EnvironmentPropertyEntityBuilder.class));
        assertNotNull(injector.getInstance(EnvironmentBundleBuilder.class));
    }

    @Test
    void checkDependencyLoaders() {
        final List<Binding<BundleDependencyLoader>> loadersBindings = injector.findBindingsByType(TypeLiteral.get(BundleDependencyLoader.class));
        assertNotNull(loadersBindings);
        assertFalse(loadersBindings.isEmpty());

        final List<BundleDependencyLoader> loaders = loadersBindings.stream().map(b -> injector.getInstance(b.getKey())).collect(toList());
        assertFalse(loaders.isEmpty());
        assertEquals(loadersBindings.size(), loaders.size());

        final DependencyLoaderRegistry registry = injector.getInstance(DependencyLoaderRegistry.class);
        assertNotNull(registry);
        assertNotNull(registry.getEntityLoaders());
        assertFalse(registry.getEntityLoaders().isEmpty());
        assertEquals(registry.getEntityLoaders().size(), loaders.size());
        assertTrue(registry.getEntityLoaders().values().containsAll(loaders));
    }

    @Test
    void checkEntityLoaders() {
        final List<Binding<EntityLoader>> loadersBindings = injector.findBindingsByType(TypeLiteral.get(EntityLoader.class));
        assertNotNull(loadersBindings);
        assertFalse(loadersBindings.isEmpty());

        final List<EntityLoader> loaders = loadersBindings.stream().map(b -> injector.getInstance(b.getKey())).collect(toList());
        assertFalse(loaders.isEmpty());
        assertEquals(loadersBindings.size(), loaders.size());

        final EntityLoaderRegistry registry = injector.getInstance(EntityLoaderRegistry.class);
        assertNotNull(registry);
        assertNotNull(registry.getEntityLoaders());
        assertFalse(registry.getEntityLoaders().isEmpty());
        assertEquals(registry.getEntityLoaders().size(), loaders.size());
        assertTrue(registry.getEntityLoaders().containsAll(loaders));
        registry.getEntityLoaders().forEach(l -> assertFalse(Modifier.isAbstract(l.getClass().getModifiers())));
    }

}