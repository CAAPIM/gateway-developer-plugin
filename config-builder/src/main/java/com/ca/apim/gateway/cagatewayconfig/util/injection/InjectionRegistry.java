/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.injection;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.reflections.Reflections;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.EXTENSION_CONFIG_FILE;
import static com.google.inject.Guice.createInjector;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isInterface;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

/**
 * Dependency injection module for the gateway developer plugins.
 */
public class InjectionRegistry extends AbstractModule {

    private static final InjectionRegistry INSTANCE = new InjectionRegistry();
    private static final String INJECTION_BASE_PACKAGE_KEY = "injection.base.package";
    private static Injector injector;

    private InjectionRegistry() {
        //
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        // find the configuration files and load the packages to be scanned from them
        findPackagesToScan().forEach(p -> {
            // for each package, create a reflections object to find the providers
            // and allow the scanning
            Reflections refl = new Reflections(p);
            InjectionProviderContext context = new InjectionProviderContext(refl);

            refl.getSubTypesOf(InjectionProvider.class).forEach(c -> {
                // create the provider
                InjectionProvider provider;
                try {
                    provider = c.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new InjectionConfigurationException("Unable to instantiate configuration provider " + c.getName(), e);
                }

                // set up reflections instance of this provider
                bind(Reflections.class).annotatedWith(Names.named("Reflections_" + provider.getClass().getSimpleName())).toInstance(refl);

                // bind all provided singleton instances
                ofNullable(provider.getSingleInstances(context))
                        .orElse(emptyMap())
                        .forEach((clazz, object) -> bind(clazz).toInstance(object));

                // bind all single bindings
                ofNullable(provider.getSingleBindings(context))
                        .orElse(emptyList())
                        .forEach(this::bind);

                // add all the multibindings
                ofNullable(provider.getMultiBindings(context))
                        .orElse(emptyMap())
                        .forEach((baseClass, subClasses) -> {
                            Multibinder<?> multibinder = newSetBinder(binder(), baseClass);
                            subClasses.stream()
                                    // filter out unwanted subclasses: abstracts, interfaces and inner classes
                                    .filter(l -> !isAbstract(l.getModifiers()) && !isInterface(l.getModifiers()) && l.getEnclosingClass() == null)
                                    .forEach(l -> multibinder.addBinding().to(l));
                        });
            });
        });
    }

    private static Set<String> findPackagesToScan() {
        Set<String> packagesToScan = new HashSet<>();
        try {
            final Enumeration<URL> resources = InjectionRegistry.class.getClassLoader().getResources(EXTENSION_CONFIG_FILE);
            while (resources.hasMoreElements()) {
                Properties properties = new Properties();
                properties.load(resources.nextElement().openStream());
                if (properties.containsKey(INJECTION_BASE_PACKAGE_KEY)) {
                    packagesToScan.add(properties.getProperty(INJECTION_BASE_PACKAGE_KEY));
                }
            }
        } catch (IOException e) {
            throw new InjectionConfigurationException("Could not load plugin configuration files: " + e.getMessage(), e);
        }
        return packagesToScan;
    }

    public static Injector getInjector() {
        return ofNullable(injector).orElseGet(InjectionRegistry::create);
    }

    public static <T> T getInstance(Class<T> serviceClass) {
        return getInjector().getInstance(serviceClass);
    }

    @VisibleForTesting
    static Injector create() {
        injector = createInjector(INSTANCE);
        return injector;
    }

}
