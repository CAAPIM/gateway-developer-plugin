/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.google.common.annotations.VisibleForTesting;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Singleton
public class BundleEntityLoaderRegistry {

    private final Map<String, BundleEntityLoader> entityLoaders;

    @Inject
    @VisibleForTesting
    public BundleEntityLoaderRegistry(final Set<BundleEntityLoader> loaders) {
        entityLoaders = unmodifiableMap(loaders.stream().collect(toMap(BundleEntityLoader::getEntityType, identity())));
    }

    public BundleEntityLoader getLoader(String type) {
        return entityLoaders.get(type);
    }

    @VisibleForTesting
    public Map<String, BundleEntityLoader> getEntityLoaders() {
        return entityLoaders;
    }
}
