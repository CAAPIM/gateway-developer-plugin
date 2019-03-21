/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.injection;

import org.reflections.Reflections;

/**
 * Contains helper instances and information for other modules provide bindings.
 */
public class InjectionProviderContext {

    private final Reflections defaultReflections;

    public InjectionProviderContext(Reflections defaultReflections) {
        this.defaultReflections = defaultReflections;
    }

    public Reflections getDefaultReflections() {
        return defaultReflections;
    }
}
