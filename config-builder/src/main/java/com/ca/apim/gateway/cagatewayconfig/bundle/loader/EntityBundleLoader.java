/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.ITEM;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

@Singleton
public class EntityBundleLoader {
    private static final Logger LOGGER = Logger.getLogger(EntityBundleLoader.class.getName());

    private final DocumentTools documentTools;
    private final BundleEntityLoaderRegistry entityLoaderRegistry;

    @Inject
    EntityBundleLoader(final DocumentTools documentTools, final BundleEntityLoaderRegistry entityLoaderRegistry) {
        this.documentTools = documentTools;
        this.entityLoaderRegistry = entityLoaderRegistry;
    }

    public Bundle load(List<File> fileSet, BundleLoadingMode loadingMode, String... entityTypes) {
        final Bundle bundle = new Bundle();
        fileSet.forEach(f -> loadBundleFile(f, bundle, loadingMode, entityTypes));
        return bundle;
    }

    public Bundle load(File dependencyBundlePath, BundleLoadingMode loadingMode, String... entityTypes) {
        final Bundle bundle = new Bundle();

        loadBundleFile(dependencyBundlePath, bundle, loadingMode, entityTypes);

        return bundle;
    }

    private void loadBundleFile(File dependencyBundlePath, Bundle bundle, BundleLoadingMode loadingMode, String... entityTypes) {
        bundle.setLoadingMode(loadingMode);

        final Document bundleDocument;
        try {
            bundleDocument = documentTools.parse(dependencyBundlePath);
        } catch (DocumentParseException e) {
            throw new BundleLoadException("Could not parse dependency bundle '" + dependencyBundlePath + "': " + e.getMessage(), e);
        }

        final NodeList nodeList = bundleDocument.getElementsByTagName(ITEM);
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                handleItem(bundle, (Element) node, entityTypes);
            }
        }
    }

    private void handleItem(Bundle bundle, final Element element, String[] entityTypes) {
        final String type = getSingleChildElement(element, TYPE).getTextContent();
        if (isNotEmpty(entityTypes) && !contains(entityTypes, type)) {
            return;
        }

        final BundleEntityLoader entityLoader = entityLoaderRegistry.getLoader(type);
        if (entityLoader != null) {
            entityLoader.load(bundle, element);
        } else {
            LOGGER.log(Level.FINE, "No entity loader found for entity type: {0}", type);
        }
    }
}
