/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import javax.inject.Named;

@Named("FOLDER")
public class Folder extends Folderable {

    public static final String ROOT_FOLDER_ID = "0000000000000000ffffffffffffec76";
    public static final String ROOT_FOLDER_NAME = "Root Node";
    public static final Folder ROOT_FOLDER = new Folder(ROOT_FOLDER_ID, ROOT_FOLDER_NAME);

    private String id;
    private String path;
    private String name;

    public Folder() {}

    public Folder(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
