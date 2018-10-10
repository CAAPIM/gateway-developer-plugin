/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class FolderLoader implements BundleDependencyLoader {
    private static final String ROOT_FOLDER_ID = "0000000000000000ffffffffffffec76";

    @Override
    public void load(Bundle bundle, Element element) {
        final Element folderElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), FOLDER);
        final String name = getSingleChildElementTextContent(folderElement, NAME);
        final String id = folderElement.getAttribute(ATTRIBUTE_ID);
        final String parentFolderID = folderElement.getAttribute(ATTRIBUTE_FOLDER_ID);

        final Folder parentFolder;
        if (ROOT_FOLDER_ID.equals(id)) {
            parentFolder = null;
        } else {
            List<Folder> parentFolderList = bundle.getFolders().values().stream().filter(f -> parentFolderID.equals(f.getId())).collect(Collectors.toList());
            if (parentFolderList.isEmpty()) {
                throw new DependencyBundleLoadException("Invalid dependency bundle. Could not find folder with id: " + parentFolderID);
            } else if (parentFolderList.size() > 1) {
                throw new DependencyBundleLoadException("Invalid dependency bundle. Found multiple folders with id: " + parentFolderID);
            }
            parentFolder = parentFolderList.get(0);
        }

        Folder folder = new Folder();
        folder.setId(id);
        folder.setName(name);
        folder.setParentFolder(parentFolder);
        folder.setPath(getPath(folder).toString());

        bundle.getFolders().put(folder.getPath(), folder);
    }

    private Path getPath(Folder folder) {
        if (folder.getParentFolder() == null || ROOT_FOLDER_ID.equals(folder.getParentFolder().getId()))
            return Paths.get(folder.getName());
        return getPath(folder.getParentFolder()).resolve(folder.getName());
    }

    @Override
    public String getEntityType() {
        return EntityTypes.FOLDER_TYPE;
    }
}
