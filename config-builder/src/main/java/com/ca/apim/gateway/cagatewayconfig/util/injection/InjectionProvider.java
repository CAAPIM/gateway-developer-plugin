/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.injection;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

/**
 * Implementations of this interface can provide extra instances to be binded to injection context.
 * Just override the required methods.
 * Implementation requires an empty constructor as well.
 */
public interface InjectionProvider {

    /**
     * Return a map containing classes x single instances to be bound in the registry.
     *
     * @param context injection context
     * @return Map of classes to instances, if no single instances, return null.
     */
    @Nullable
    default Map<Class, Object> getSingleInstances(InjectionProviderContext context) {
        return emptyMap();
    }

    /**
     * Return a list of classes to be bounded to the registry. Those are intended where no pre-baked instances are needed.
     *
     * @param context injection context
     * @return single bindings
     */
    @Nullable
    default List<Class> getSingleBindings(InjectionProviderContext context) {
        return emptyList();
    }

    /**
     * Return a map containing classes x set of subclasses to be bound in the registry.
     *
     * @param context  injection context
     * @return Map of classes to list of subclasses, if not happen, return null.
     */
    @Nullable
    default Map<Class, Set<Class>> getMultiBindings(InjectionProviderContext context) {
        return emptyMap();
    }
}
