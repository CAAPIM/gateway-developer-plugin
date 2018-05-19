/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import java.util.*;

public class EntityLoaderRegistry {

    private final Map<String, EntityLoader> entityLoaders;

    public EntityLoaderRegistry() {
        this.entityLoaders = Map.of(
                "SERVICE", new ServiceLoader(),
                "POLICY", new PolicyLoader(),
                "POLICY_BACKED_SERVICE", new PolicyBackedServiceLoader(),
                "FOLDER", new FolderLoader(),
                "ENCAPSULATED_ASSERTION", new EncassLoader(),
                "CLUSTER_PROPERTY", new ClusterPropertyLoader());
    }

    public EntityLoader getLoader(String type) {
        return entityLoaders.get(type);
    }
}
