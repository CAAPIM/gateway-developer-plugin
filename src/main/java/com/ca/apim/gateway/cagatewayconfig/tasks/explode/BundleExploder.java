/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.explode;

import com.ca.apim.gateway.cagatewayconfig.bundle.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.BundleBuilderException;
import com.ca.apim.gateway.cagatewayconfig.bundle.Entity;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Service;
import com.ca.apim.gateway.cagatewayconfig.tasks.explode.writer.EntityWriter;
import com.ca.apim.gateway.cagatewayconfig.tasks.explode.writer.FolderWriter;
import com.ca.apim.gateway.cagatewayconfig.tasks.explode.writer.ServiceWriter;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BundleExploder {
    private final Bundle bundle;
    private final DocumentTools documentTools;
    private final DocumentFileUtils documentFileUtils;

    private final Map<Class<? extends Entity>, EntityWriter> entityWriters = new HashMap<>();

    public BundleExploder(Bundle bundle, DocumentTools documentTools, DocumentFileUtils documentFileUtils) {
        this.bundle = bundle;
        this.documentTools = documentTools;
        this.documentFileUtils = documentFileUtils;
    }

    public void persist(Path path) {
        FolderTree folderTree = bundle.getFolderTree();
        EntityWriter<Folder> folderWriter = getEntityWriter(folderTree, Folder.class);
        folderTree.stream().forEach(f -> folderWriter.write(path, f));

        Map<Class<? extends Entity>, Map<String, Entity>> entities = bundle.getAllEntities();
        entities.forEach((entityType, entitiesMap) -> {
            if (entityType != Folder.class) {
                entitiesMap.values().forEach(e -> getEntityWriter(folderTree, entityType).write(path, e));
            }
        });

        entityWriters.values().forEach(entityWriter -> entityWriter.finalizeWrite(path));
    }


    private EntityWriter getEntityWriter(FolderTree folderTree, Class<? extends Entity> entityType) {
        return entityWriters.computeIfAbsent(entityType, k -> getNewEntityWriter(folderTree, k));

    }

    private EntityWriter getNewEntityWriter(FolderTree folderTree, Class<? extends Entity> entityType) {
        if (Folder.class == entityType) {
            return new FolderWriter(folderTree);
        } else if (Service.class == entityType) {
            return new ServiceWriter(folderTree, documentTools, documentFileUtils);
        } else
            throw new BundleBuilderException("No entity loader found for entity type: " + entityType);
    }
}
