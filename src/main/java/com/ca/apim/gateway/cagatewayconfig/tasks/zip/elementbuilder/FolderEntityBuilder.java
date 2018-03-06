/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.elementbuilder;

import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Folder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

public class FolderEntityBuilder implements EntityBuilder<Folder> {
    private static final String ROOT_FOLDER_ID = "0000000000000000ffffffffffffec76";
    private static final String ROOT_FOLDER_NAME = "Root Node";
    private final Document document;

    public FolderEntityBuilder(Document document) {
        this.document = document;
    }

    public Folder buildRootFolder() {
        return buildFolder(ROOT_FOLDER_NAME, ROOT_FOLDER_ID, null);
    }

    public Folder buildFolder(String name, String id, String parentFolderId) {
        Element folder = document.createElement("l7:Folder");
        folder.setAttribute("id", id);
        if (parentFolderId != null) {
            folder.setAttribute("folderId", parentFolderId);
        }
        Element folderName = document.createElement("l7:Name");
        folderName.appendChild(document.createTextNode(name));
        folder.appendChild(folderName);
        return new Folder(name, id, parentFolderId, folder);
    }

    @Override
    public Folder build(String name, String id, Element entityElement, File folder, String parentFolderID) {
        throw new EntityBuilderException("Building a folder entity from an existing entity element is not supported");
    }
}
