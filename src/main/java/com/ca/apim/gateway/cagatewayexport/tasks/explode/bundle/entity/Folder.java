/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;

@Named("FOLDER")
public class Folder implements Entity {
    private final String name;
    private final String id;
    private final String parentFolderId;

    public Folder(final String name, final String id, final String parentFolderId) {
        this.name = name;
        this.id = id;
        this.parentFolderId = parentFolderId == null || parentFolderId.isEmpty() ? null : parentFolderId;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getParentFolderId() {
        return parentFolderId;
    }

    @Override
    public String toString() {
        return id + ":" + name;
    }
}
