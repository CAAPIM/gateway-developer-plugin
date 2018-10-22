/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Dependency;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;

import java.util.*;

public class Bundle {
    private Map<Class<? extends Entity>, Map<String, Entity>> entities = new HashMap<>();
    private FolderTree folderTree;
    private Map<Dependency, List<Dependency>> dependencies;

    public void addEntity(final Entity entity) {
        entities.compute(entity.getClass(), (k, v) -> {
            if (v == null) {
                Map<String, Entity> entitiesOfType = new LinkedHashMap<>();
                entitiesOfType.put(entity.getId(), entity);
                return entitiesOfType;
            } else {
                v.put(entity.getId(), entity);
                return v;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <E extends Entity> Map<String, E> getEntities(Class<E> entityType) {
        try {
            return (Map<String, E>) entities.getOrDefault(entityType, Collections.emptyMap());
        } catch (ClassCastException e) {
            throw new BundleBuilderException("Unable to cast entities properly");
        }
    }

    public FolderTree getFolderTree() {
        return folderTree;
    }

    public void setFolderTree(FolderTree folderTree) {
        this.folderTree = folderTree;
    }

    public Map<Dependency, List<Dependency>> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<Dependency, List<Dependency>> dependencies) {
        this.dependencies = dependencies;
    }
}
