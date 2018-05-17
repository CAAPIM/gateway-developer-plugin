/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BundleBuilder {
    private static final Logger LOGGER = Logger.getLogger(BundleBuilder.class.getName());
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

//        bundle.setDependencies(EntityLoaderHelper.getSingleChildElement(bundleElement, "l7:Dependencies"));
    }

    public Bundle getBundle() {
        return bundle;
    }

    private void handleItem(final Element element) {
        final String type = EntityLoaderHelper.getSingleChildElement(element, "l7:Type").getTextContent();
        EntityLoader entityLoader = getEntityLoader(type);
        if(entityLoader != null) {
            final Entity entity = entityLoader.load(element);
            if (entity != null) {
                bundle.addEntity(entity);
            }
        } else {
            LOGGER.log(Level.INFO, "No entity loader found for entity type: {0}", type);
        }
    }

    private EntityLoader getEntityLoader(String entityType) {
        switch (entityType) {
            case "FOLDER":
                return new FolderLoader();
            case "SERVICE":
                return new ServiceLoader();
            case "POLICY":
                return new PolicyLoader();
            case "ENCAPSULATED_ASSERTION":
                return new EncassLoader();
            case "CLUSTER_PROPERTY":
                return new ClusterPropertyLoader();
            case "POLICY_BACKED_SERVICE":
                return new PolicyBackedServiceLoader();
            default:
                return null;
        }
    }
}
