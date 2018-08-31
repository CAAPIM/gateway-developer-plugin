/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.BundleEntityLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.DependencyBundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools.getSingleChildElement;

public class DependencyBundleLoader {
    private static final Logger LOGGER = Logger.getLogger(DependencyBundleLoader.class.getName());

    private final DocumentTools documentTools;
    private final EntityLoaderRegistry entityLoaderRegistry;

    public DependencyBundleLoader(final DocumentTools documentTools) {
        this.documentTools = documentTools;
        this.entityLoaderRegistry = new EntityLoaderRegistry(documentTools);
    }

    public Bundle load(File dependencyBundlePath) {
        final Bundle bundle = new Bundle();

        final Document bundleDocument;
        try {
            bundleDocument = documentTools.parse(dependencyBundlePath);
        } catch (DocumentParseException e) {
            throw new DependencyBundleLoadException("Could not parse dependency bundle: " + e.getMessage(), e);
        }

        final NodeList nodeList = bundleDocument.getElementsByTagName("l7:Item");
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                handleItem(bundle, (Element) node);
            }
        }

        return bundle;
    }

    private void handleItem(Bundle bundle, final Element element) {
        final String type = getSingleChildElement(element, "l7:Type").getTextContent();
        final BundleEntityLoader entityLoader = entityLoaderRegistry.getLoader(type);
        if (entityLoader != null) {
            entityLoader.load(bundle, element);
        } else {
            LOGGER.log(Level.FINE, "No entity loader found for entity type: {0}", type);
        }
    }
}
