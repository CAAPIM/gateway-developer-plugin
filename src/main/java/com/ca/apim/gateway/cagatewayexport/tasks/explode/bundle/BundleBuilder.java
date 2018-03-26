/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader.EntityLoaderHelper;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader.FolderLoader;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader.ServiceLoader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BundleBuilder {
    private final Bundle bundle;

    public BundleBuilder() {
        bundle = new Bundle();
    }

    public void buildBundle(final Element bundleElement) {
        final NodeList nodeList = bundleElement.getElementsByTagName("l7:Item");
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                handleItem((Element) node);
            }
        }
        FolderTree folderTree = new FolderTree(bundle.getEntities(Folder.class).values());
        bundle.setFolderTree(folderTree);
    }

    public Bundle getBundle() {
        return bundle;
    }

    private void handleItem(final Element element) {
        final String type = EntityLoaderHelper.getSingleElement(element, "l7:Type").getTextContent();
        final Entity entity = getEntityLoader(type).load(element);
        bundle.addEntity(entity);
    }

    private EntityLoader getEntityLoader(String entityType) {
        switch (entityType) {
            case "FOLDER":
                return new FolderLoader();
            case "SERVICE":
                return new ServiceLoader();
            default:
                throw new BundleBuilderException("No entity loader found for entity type: " + entityType);
        }
    }
}
