/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class EntityLoaderRegistry {

    private final Collection<EntityLoader> entityLoaders;

    public EntityLoaderRegistry(FileUtils fileUtils, JsonTools jsonTools, IdGenerator idGenerator) {
        final Collection<EntityLoader> loadersCollection = new HashSet<>();
        loadersCollection.add(new ServiceLoader(jsonTools));
        loadersCollection.add(new EncassLoader(jsonTools, idGenerator));
        loadersCollection.add(new PolicyAndFolderLoader(fileUtils, idGenerator));
        loadersCollection.add(new StaticPropertiesLoader(fileUtils));
        loadersCollection.add(new EnvironmentPropertiesLoader(fileUtils));
        loadersCollection.add(new PolicyBackedServiceLoader(jsonTools));
        loadersCollection.add(new IdentityProviderLoader(jsonTools));
        loadersCollection.add(new ListenPortLoader(jsonTools));
        loadersCollection.add(new StoredPasswordsLoader(fileUtils));

        this.entityLoaders = Collections.unmodifiableCollection(loadersCollection);
    }

    public Collection<EntityLoader> getEntityLoaders() {
        return entityLoaders;
    }
}
