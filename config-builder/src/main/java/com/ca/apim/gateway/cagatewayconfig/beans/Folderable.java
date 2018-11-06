/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

public abstract class Folderable extends GatewayEntity {
    private Folder parentFolder;

    public Folder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }

    public String getParentFolderId() {
        return this.parentFolder != null ? this.parentFolder.getId() : null;
    }
}
