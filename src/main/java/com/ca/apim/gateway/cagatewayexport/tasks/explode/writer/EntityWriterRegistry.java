package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class EntityWriterRegistry {

    private final Collection<EntityWriter> entityLoaders;

    public EntityWriterRegistry(final DocumentTools documentTools, final DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        final Collection<EntityWriter> loadersCollection = new HashSet<>();
        loadersCollection.add(new PolicyWriter(documentFileUtils, documentTools));
        loadersCollection.add(new ServiceWriter(documentFileUtils, jsonTools));

        this.entityLoaders = Collections.unmodifiableCollection(loadersCollection);
    }

    public Collection<EntityWriter> getEntityWriters() {
        return entityLoaders;
    }
}
