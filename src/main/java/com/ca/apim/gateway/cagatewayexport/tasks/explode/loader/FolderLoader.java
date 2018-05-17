/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import org.w3c.dom.Element;

public class FolderLoader implements EntityLoader<Folder> {
    @Override
    public Folder load(final Element element) {
        final Element folder = EntityLoaderHelper.getSingleChildElement(EntityLoaderHelper.getSingleChildElement(element, "l7:Resource"), "l7:Folder");
        final String name = EntityLoaderHelper.getSingleChildElement(folder, "l7:Name").getTextContent();
        final String id = folder.getAttribute("id");
        final String parentFolderID = folder.getAttribute("folderId");
        return new Folder(name, id, parentFolderID);
    }
}
