/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.string.CharacterBlacklistUtil;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER_ID;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;

@Singleton
public class FolderLoader implements BundleEntityLoader {
    private static final Logger LOGGER = Logger.getLogger(FolderLoader.class.getName());

    @Override
    public void load(Bundle bundle, Element element) {
        final Element folderElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), FOLDER);
        String name = getSingleChildElementTextContent(folderElement, NAME);
        final String id = folderElement.getAttribute(ATTRIBUTE_ID);
        final String parentFolderID = folderElement.getAttribute(ATTRIBUTE_FOLDER_ID);

        final Folder parentFolder;
        if (ROOT_FOLDER_ID.equals(id)) {
            parentFolder = null;
        } else {
            parentFolder = ServiceAndPolicyLoaderUtil.getFolder(bundle, parentFolderID);
        }

        try {
            name = CharacterBlacklistUtil.encodeName(name);
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.WARNING, "unable to encode folder name " + name);
        }

        Folder folder = new Folder();
        folder.setId(id);
        folder.setName(name);
        folder.setParentFolder(parentFolder);
        folder.setPath(getPath(folder).toString());

        bundle.getFolders().put(folder.getPath(), folder);
    }

    private Path getPath(Folder folder) {
        if (folder.getParentFolder() == null || ROOT_FOLDER_ID.equals(folder.getParentFolder().getId())) {
            return Paths.get(folder.getName());
        }
        return getPath(folder.getParentFolder()).resolve(folder.getName());
    }

    @Override
    public String getEntityType() {
        return EntityTypes.FOLDER_TYPE;
    }
}
