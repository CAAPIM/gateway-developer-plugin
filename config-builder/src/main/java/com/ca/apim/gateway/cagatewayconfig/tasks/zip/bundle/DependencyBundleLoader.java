/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.BundleDependencyLoader;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.DependencyBundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.DependencyLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.ITEM;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;

@Singleton
public class DependencyBundleLoader {
    private static final Logger LOGGER = Logger.getLogger(DependencyBundleLoader.class.getName());

    private final DocumentTools documentTools;
    private final DependencyLoaderRegistry entityLoaderRegistry;

    @Inject
    DependencyBundleLoader(final DocumentTools documentTools, final DependencyLoaderRegistry entityLoaderRegistry) {
        this.documentTools = documentTools;
        this.entityLoaderRegistry = entityLoaderRegistry;
    }

    public Bundle load(File dependencyBundlePath) {
        final Bundle bundle = new Bundle();

        final Document bundleDocument;
        try {
            bundleDocument = documentTools.parse(dependencyBundlePath);
        } catch (DocumentParseException e) {
            throw new DependencyBundleLoadException("Could not parse dependency bundle: " + e.getMessage(), e);
        }

        final NodeList nodeList = bundleDocument.getElementsByTagName(ITEM);
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                handleItem(bundle, (Element) node);
            }
        }

        return bundle;
    }

    private void handleItem(Bundle bundle, final Element element) {
        final String type = getSingleChildElement(element, TYPE).getTextContent();
        final BundleDependencyLoader entityLoader = entityLoaderRegistry.getLoader(type);
        if (entityLoader != null) {
            entityLoader.load(bundle, element);
        } else {
            LOGGER.log(Level.FINE, "No entity loader found for entity type: {0}", type);
        }
    }
}
