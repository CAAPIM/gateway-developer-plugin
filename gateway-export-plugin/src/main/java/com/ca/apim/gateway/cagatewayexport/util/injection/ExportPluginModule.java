/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.injection;

import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.injection.ConfigBuilderModule;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.ExplodeBundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilterRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntitiesLinker;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntityLinkerRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriterRegistry;
import com.ca.apim.gateway.cagatewayexport.util.http.GatewayClient;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import org.reflections.Reflections;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static java.lang.reflect.Modifier.isAbstract;
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
        Reflections reflections = new Reflections(ConfigBuilderModule.BASE_PACKAGE).merge(new Reflections(BASE_PACKAGE));
        // bind the reflections instance so it can be reused anywhere
        bind(Reflections.class).toInstance(reflections);

        // bind default tools to default instances
        bind(DocumentTools.class).toInstance(DocumentTools.INSTANCE);
        bind(FileUtils.class).toInstance(FileUtils.INSTANCE);
        bind(JsonTools.class).toInstance(JsonTools.INSTANCE);
        bind(DocumentFileUtils.class).toInstance(DocumentFileUtils.INSTANCE);
        bind(PolicyXMLSimplifier.class).toInstance(PolicyXMLSimplifier.INSTANCE);
        bind(GatewayClient.class).toInstance(GatewayClient.INSTANCE);

        // bind all entity linkers to the module
        Multibinder<EntitiesLinker> linkersBinder = newSetBinder(binder(), EntitiesLinker.class);
        reflections.getSubTypesOf(EntitiesLinker.class).forEach(l -> {
            // this is needed in order to avoid binding an interface
            if (!isInterface(l.getModifiers())) {
                linkersBinder.addBinding().to(l);
            }
        });
        bind(EntityLinkerRegistry.class);

        // bind the entity filters
        Multibinder<EntityFilter> filtersBinder = newSetBinder(binder(), EntityFilter.class);
        reflections.getSubTypesOf(EntityFilter.class).forEach(l -> filtersBinder.addBinding().to(l));
        bind(EntityFilterRegistry.class);

        // bind the entity writers
        Multibinder<EntityWriter> writersBinder = newSetBinder(binder(), EntityWriter.class);
        reflections.getSubTypesOf(EntityWriter.class).forEach(l -> {
            if (!isAbstract(l.getModifiers())) {
                writersBinder.addBinding().to(l);
            }
        });
        bind(EntityWriterRegistry.class);

        // bind all policy converters to the module
        Multibinder<PolicyConverter> policyConverterBinder = newSetBinder(binder(), PolicyConverter.class);
        reflections.getSubTypesOf(PolicyConverter.class).forEach(l -> policyConverterBinder.addBinding().to(l));
        bind(PolicyConverterRegistry.class);

        // bind the explode task implementation
        bind(ExplodeBundle.class);
    }

    public static Injector getInjector() {
        return ofNullable(injector).orElseGet(ExportPluginModule::create);
    }

    public static <T> T getInstance(Class<T> serviceClass) {
        return getInjector().getInstance(serviceClass);
    }

    @VisibleForTesting
    static Injector create() {
        injector = createInjector(new ExportPluginModule());
        return injector;
    }

}
