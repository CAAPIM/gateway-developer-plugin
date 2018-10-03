/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.injection;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.BundleDependencyLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.DependencyLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import org.reflections.Reflections;

import javax.net.ssl.SSLSocketFactory;
import java.lang.reflect.Modifier;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.ConnectionUtils.createAcceptAllSocketFactory;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static java.util.Optional.ofNullable;

/**
 * Dependency injection module for the config builder developer.
 */
public class ConfigBuilderModule extends AbstractModule {

    private static final String BASE_PACKAGE = "com.ca.apim.gateway.cagatewayconfig";
    private static Injector injector;

    private ConfigBuilderModule() {
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
        bind(FileUtils.class).toInstance(FileUtils.INSTANCE);
        bind(IdGenerator.class).toInstance(new IdGenerator());
        bind(SSLSocketFactory.class).toInstance(createAcceptAllSocketFactory());

        // bind all entity builders to the module
        Multibinder<EntityBuilder> buildersBinder = newSetBinder(binder(), EntityBuilder.class);
        reflections.getSubTypesOf(EntityBuilder.class).forEach(l -> buildersBinder.addBinding().to(l));

        bind(BundleEntityBuilder.class);

        // bind all bundle dependency loaders to the module
        Multibinder<BundleDependencyLoader> depLoadersBinder = newSetBinder(binder(), BundleDependencyLoader.class);
        reflections.getSubTypesOf(BundleDependencyLoader.class).forEach(l -> depLoadersBinder.addBinding().to(l));
        bind(DependencyLoaderRegistry.class);

        // bind all entity loaders to the module
        Multibinder<EntityLoader> entityLoadersBinder = newSetBinder(binder(), EntityLoader.class);
        reflections.getSubTypesOf(EntityLoader.class).forEach(l -> {
            // avoid trying to bind abstract classes
            if (!Modifier.isAbstract(l.getModifiers())) {
                entityLoadersBinder.addBinding().to(l);
            }
        });
        bind(EntityLoaderRegistry.class);
    }

    public static Injector getInjector() {
        return ofNullable(injector).orElseGet(ConfigBuilderModule::create);
    }

    @VisibleForTesting
    static Injector create() {
        injector = createInjector(new ConfigBuilderModule());
        return injector;
    }
}
