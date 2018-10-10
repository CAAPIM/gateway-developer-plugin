/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Singleton
public class DependencyLoaderRegistry {

    private final Map<String, BundleDependencyLoader> entityLoaders;

    @Inject
    DependencyLoaderRegistry(final Set<BundleDependencyLoader> loaders) {
        entityLoaders = unmodifiableMap(loaders.stream().collect(toMap(BundleDependencyLoader::getEntityType, identity())));
    }

    public BundleDependencyLoader getLoader(String type) {
        return entityLoaders.get(type);
    }

    @VisibleForTesting
    public Map<String, BundleDependencyLoader> getEntityLoaders() {
        return entityLoaders;
    }
}
