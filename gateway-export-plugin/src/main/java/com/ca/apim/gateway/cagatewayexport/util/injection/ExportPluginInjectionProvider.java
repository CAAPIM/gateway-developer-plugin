/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.injection;

import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.connection.GatewayClient;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionProvider;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionProviderContext;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.ExplodeBundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.EntityFilterRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntitiesLinker;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntityLinkerRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.EntityWriterRegistry;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyAssertionSimplifier;
import com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLSimplifier;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.copyOf;

/**
 * Provide the export plugin beans to be bounded to the injection registry.
 */
public class ExportPluginInjectionProvider implements InjectionProvider {

    @Override
    public @Nullable Map<Class, Object> getSingleInstances(InjectionProviderContext context) {
        return ImmutableMap.of(GatewayClient.class, GatewayClient.INSTANCE);
    }

    @Override
    public @Nullable List<Class> getSingleBindings(InjectionProviderContext context) {
        return Arrays.asList(
                EntityLinkerRegistry.class,
                EntityWriterRegistry.class,
                EntityFilterRegistry.class,
                PolicyConverterRegistry.class,
                ExplodeBundle.class,
                PolicyXMLSimplifier.class
        );
    }

    @Override
    public @Nullable Map<Class, Set<Class>> getMultiBindings(InjectionProviderContext context) {
        return ImmutableMap.<Class, Set<Class>>builder()
                .put(EntitiesLinker.class, copyOf(context.getDefaultReflections().getSubTypesOf(EntitiesLinker.class)))
                .put(EntityFilter.class, copyOf(context.getDefaultReflections().getSubTypesOf(EntityFilter.class)))
                .put(EntityWriter.class, copyOf(context.getDefaultReflections().getSubTypesOf(EntityWriter.class)))
                .put(PolicyAssertionSimplifier.class, copyOf(context.getDefaultReflections().getSubTypesOf(PolicyAssertionSimplifier.class)))
                .build();
    }
}
