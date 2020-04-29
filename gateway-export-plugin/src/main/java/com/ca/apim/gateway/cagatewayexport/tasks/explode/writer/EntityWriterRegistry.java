/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.EntityTypeRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriterHelper.write;
import static java.util.Collections.unmodifiableCollection;

@Singleton
public class EntityWriterRegistry {

    private final Collection<EntityWriter> entityWriters;

    @Inject
    public EntityWriterRegistry(final Set<EntityWriter> writers, final DocumentFileUtils documentFileUtils, final JsonTools jsonTools) {
        // add the implemented writers
        Set<EntityWriter> allWriters = new HashSet<>(writers);

        // create generic writers for the entities configured to not have a specific implementation
        EntityTypeRegistry entityTypeRegistry = InjectionRegistry.getInstance(EntityTypeRegistry.class);
        entityTypeRegistry.getEntityTypeMap().values().forEach(info -> {
            if (info.getFileName() != null && info.getFileType() != null) {
                allWriters.add((bundle, rootFolder, rawBundle) -> write(bundle, rootFolder, info, documentFileUtils, jsonTools));
            }
        });

        this.entityWriters = unmodifiableCollection(allWriters);
    }

    public Collection<EntityWriter> getEntityWriters() {
        return entityWriters;
    }
}
