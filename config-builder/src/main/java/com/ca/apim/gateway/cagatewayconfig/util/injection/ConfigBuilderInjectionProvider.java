/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.injection;

import com.ca.apim.gateway.cagatewayconfig.beans.EntityTypeRegistry;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.PolicyAssertionBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.PolicyXMLBuilder;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleEntityLoader;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleEntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.EntityBundleLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.environment.BundleCache;
import com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleBuilder;
import com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleCreator;
import com.ca.apim.gateway.cagatewayconfig.environment.FullBundleCreator;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLSocketFactory;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.CertificateUtils.createX509CertificateFactory;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.ConnectionUtils.createAcceptAllSocketFactory;
import static com.google.common.collect.ImmutableSet.copyOf;

/**
 * Injection provider for Config Builder module.
 */
public class ConfigBuilderInjectionProvider implements InjectionProvider {

    @Override
    public @Nullable Map<Class, Object> getSingleInstances(InjectionProviderContext context) {
        return ImmutableMap.<Class, Object>builder()
                .put(DocumentTools.class, DocumentTools.INSTANCE)
                .put(JsonTools.class, JsonTools.INSTANCE)
                .put(JsonFileUtils.class, JsonFileUtils.INSTANCE)
                .put(DocumentFileUtils.class, DocumentFileUtils.INSTANCE)
                .put(FileUtils.class, FileUtils.INSTANCE)
                .put(IdGenerator.class, new IdGenerator())
                .put(SSLSocketFactory.class, createAcceptAllSocketFactory())
                .put(CertificateFactory.class, createX509CertificateFactory())
                .build();
    }

    @Override
    public @Nullable List<Class> getSingleBindings(InjectionProviderContext context) {
        return Arrays.asList(
                BundleEntityBuilder.class,
                BundleEntityLoaderRegistry.class,
                BundleCache.class,
                EntityLoaderRegistry.class,
                EntityTypeRegistry.class,
                PolicyConverterRegistry.class,
                EnvironmentBundleBuilder.class,
                EnvironmentBundleCreator.class,
                EnvironmentConfigurationUtils.class,
                EntityBundleLoader.class,
                FullBundleCreator.class,
                PolicyXMLBuilder.class
        );
    }

    @Override
    public @Nullable Map<Class, Set<Class>> getMultiBindings(InjectionProviderContext context) {
        return ImmutableMap.<Class, Set<Class>>builder()
                .put(EntityBuilder.class, copyOf(context.getDefaultReflections().getSubTypesOf(EntityBuilder.class)))
                .put(BundleEntityLoader.class, copyOf(context.getDefaultReflections().getSubTypesOf(BundleEntityLoader.class)))
                .put(EntityLoader.class, copyOf(context.getDefaultReflections().getSubTypesOf(EntityLoader.class)))
                .put(PolicyConverter.class, copyOf(context.getDefaultReflections().getSubTypesOf(PolicyConverter.class)))
                .put(PolicyAssertionBuilder.class, copyOf(context.getDefaultReflections().getSubTypesOf(PolicyAssertionBuilder.class)))
                .build();
    }
}
