package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class EntityLoaderRegistry {

    private final Collection<EntityLoader> entityLoaders;

    public EntityLoaderRegistry(FileUtils fileUtils, JsonTools jsonTools) {
        final Collection<EntityLoader> loadersCollection = new HashSet<>();
        loadersCollection.add(new ServiceLoader(fileUtils, jsonTools));
        loadersCollection.add(new PolicyAndFolderLoader(fileUtils));

        this.entityLoaders = Collections.unmodifiableCollection(loadersCollection);
    }

    public Collection<EntityLoader> getEntityLoaders() {
        return entityLoaders;
    }
}
