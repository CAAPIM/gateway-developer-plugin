/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.injection;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.ExplodeBundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EntityTypeRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntitiesLinker;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntityLinkerRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriterRegistry;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import org.reflections.Reflections;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static java.lang.reflect.Modifier.isInterface;
import static java.util.Optional.ofNullable;

/**
 * Dependency injection configuration for the export
 */
public class ExportPluginModule extends AbstractModule {

    private static final String BASE_PACKAGE = "com.ca.apim.gateway.cagatewayexport";
    private static Injector injector;

    private ExportPluginModule() {
        //
    }

    @Override
    protected void configure() {
        // scan everything under the plugin package
        Reflections reflections = new Reflections(BASE_PACKAGE);
        // bind the reflections instance so it can be reused anywhere
        bind(Reflections.class).toInstance(reflections);

        // bind default tools to default instances
        bind(DocumentTools.class).toInstance(DocumentTools.INSTANCE);
        bind(JsonTools.class).toInstance(JsonTools.INSTANCE);
        bind(DocumentFileUtils.class).toInstance(DocumentFileUtils.INSTANCE);
        bind(PolicyXMLSimplifier.class).toInstance(PolicyXMLSimplifier.INSTANCE);

        // bind all entity linkers to the module
        Multibinder<EntitiesLinker> linkersBinder = newSetBinder(binder(), EntitiesLinker.class);
        reflections.getSubTypesOf(EntitiesLinker.class).forEach(l -> {
            // this is needed in order to avoid binding an interface
            if (!isInterface(l.getModifiers())) {
                linkersBinder.addBinding().to(l);
            }
        });
        bind(EntityLinkerRegistry.class);

        // bind the entity loaders
        Multibinder<EntityLoader> loadersBinder = newSetBinder(binder(), EntityLoader.class);
        reflections.getSubTypesOf(EntityLoader.class).forEach(l -> loadersBinder.addBinding().to(l));
        bind(EntityLoaderRegistry.class);

        // bind the entity writers
        Multibinder<EntityWriter> writersBinder = newSetBinder(binder(), EntityWriter.class);
        reflections.getSubTypesOf(EntityWriter.class).forEach(l -> writersBinder.addBinding().to(l));
        bind(EntityWriterRegistry.class);

        // bind the entity type registry
        bind(EntityTypeRegistry.class);

        // bind the explode task implementation
        bind(ExplodeBundle.class);
    }

    public static Injector getInjector() {
        return ofNullable(injector).orElseGet(ExportPluginModule::create);
    }

    @VisibleForTesting
    static Injector create() {
        injector = createInjector(new ExportPluginModule());
        return injector;
    }

}
