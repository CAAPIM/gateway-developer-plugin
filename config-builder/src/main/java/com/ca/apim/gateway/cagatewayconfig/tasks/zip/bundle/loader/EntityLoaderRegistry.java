/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.*;

public class EntityLoaderRegistry {

    private final Map<String, BundleEntityLoader> entityLoaders;

    public EntityLoaderRegistry() {
        entityLoaders = new HashMap<>();
        entityLoaders.put(POLICY_TYPE, new PolicyLoader());
        entityLoaders.put(FOLDER_TYPE, new FolderLoader());
        entityLoaders.put(ENCAPSULATED_ASSERTION_TYPE, new EncassLoader());
        entityLoaders.put(LISTEN_PORT_TYPE, new ListenPortLoader());
    }

    public BundleEntityLoader getLoader(String type) {
        return entityLoaders.get(type);
    }
}
