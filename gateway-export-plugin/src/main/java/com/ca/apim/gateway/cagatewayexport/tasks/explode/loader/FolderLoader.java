/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class FolderLoader implements EntityLoader<Folder> {

    @Override
    public Folder load(final Element element) {
        final Element folder = getSingleChildElement(getSingleChildElement(element, RESOURCE), FOLDER);
        final String name = getSingleChildElement(folder, NAME).getTextContent();
        final String id = folder.getAttribute(ATTRIBUTE_ID);
        final String parentFolderID = folder.getAttribute(ATTRIBUTE_FOLDER_ID);
        return new Folder(name, id, parentFolderID);
    }

    @Override
    public Class<Folder> entityClass() {
        return Folder.class;
    }
}
