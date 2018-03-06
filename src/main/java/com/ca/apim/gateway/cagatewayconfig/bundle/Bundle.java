/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle;

import com.ca.apim.gateway.cagatewayconfig.bundle.entity.FolderTree;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Bundle {
    private Map<Class<? extends Entity>, Map<String, Entity>> entities = new HashMap<>();
    private FolderTree folderTree;

    public void addEntity(final Entity entity) {
        entities.compute(entity.getClass(), (k, v) -> {
            if (v == null) {
                Map<String, Entity> entitiesOfType = new HashMap<>();
                entitiesOfType.put(entity.getId(), entity);
                return entitiesOfType;
            } else {
                v.put(entity.getId(), entity);
                return v;
            }
        });
    }

    public <E extends Entity> Map<String, E> getEntities(Class<E> entityType) {
        return (Map<String, E>) entities.getOrDefault(entityType, Collections.emptyMap());
    }

    public FolderTree getFolderTree() {
        return folderTree;
    }

    public void setFolderTree(FolderTree folderTree) {
        this.folderTree = folderTree;
    }

    public Map<Class<? extends Entity>, Map<String, Entity>> getAllEntities() {
        return entities;
    }
}
