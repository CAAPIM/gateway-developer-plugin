/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;

import java.util.Map;

public class EntityLoaderRegistry {

    private final Map<String, BundleEntityLoader> entityLoaders;

    public EntityLoaderRegistry(DocumentTools documentTools) {
        this.entityLoaders = Map.of(
                "POLICY", new PolicyLoader(documentTools),
                "FOLDER", new FolderLoader(documentTools),
                "ENCAPSULATED_ASSERTION", new EncassLoader(documentTools));
    }

    public BundleEntityLoader getLoader(String type) {
        return entityLoaders.get(type);
    }
}
