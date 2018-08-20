/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import java.util.HashMap;
import java.util.Map;

public class EntityLoaderRegistry {

    private final Map<String, EntityLoader> entityLoaders;

    public EntityLoaderRegistry() {
        entityLoaders = new HashMap<>();
        entityLoaders.put("SERVICE", new ServiceLoader());
        entityLoaders.put("POLICY", new PolicyLoader());
        entityLoaders.put("POLICY_BACKED_SERVICE", new PolicyBackedServiceLoader());
        entityLoaders.put("FOLDER", new FolderLoader());
        entityLoaders.put("ENCAPSULATED_ASSERTION", new EncassLoader());
        entityLoaders.put("CLUSTER_PROPERTY", new ClusterPropertyLoader());
        entityLoaders.put("SSG_CONNECTOR", new ListenPortLoader());
    }

    public EntityLoader getLoader(String type) {
        return entityLoaders.get(type);
    }
}
