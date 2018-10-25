/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Dependency;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EntityTypeRegistry;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleChildElement;
import static org.w3c.dom.Node.ELEMENT_NODE;

public class BundleBuilder {
    private static final Logger LOGGER = Logger.getLogger(BundleBuilder.class.getName());
    private final EntityLoaderRegistry entityLoaderRegistry;
    private final EntityTypeRegistry entityTypeRegistry;

    @Inject
    public BundleBuilder(final EntityLoaderRegistry entityLoaderRegistry, final EntityTypeRegistry entityTypeRegistry) {
        this.entityLoaderRegistry = entityLoaderRegistry;
        this.entityTypeRegistry = entityTypeRegistry;
    }

    public Bundle buildBundle(final Element bundleElement) {
        Bundle bundle = new Bundle();

        final NodeList nodeList = bundleElement.getElementsByTagName(ITEM);
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);

            if (node.getNodeType() == ELEMENT_NODE) {
                handleItem((Element) node, bundle);
            }
        }
        FolderTree folderTree = new FolderTree(bundle.getEntities(Folder.class).values());
        bundle.setFolderTree(folderTree);
        bundle.setDependencies(buildDependencies(getSingleChildElement(getSingleChildElement(bundleElement, DEPENDENCY_GRAPH), DEPENDENCIES)));

        return bundle;
    }

    private Map<Dependency, List<Dependency>> buildDependencies(Element dependenciesElement) {
        Map<Dependency, List<Dependency>> dependencyMap = new HashMap<>();
        NodeList bundleDependencies = dependenciesElement.getChildNodes();
        for (int i = 0; i < bundleDependencies.getLength(); i++) {
            Node dependencyNode = bundleDependencies.item(i);
            if (dependencyNode.getNodeType() == ELEMENT_NODE) {
                Dependency dependency = buildDependency((Element) dependencyNode);
                if (dependency != null) {
                    List<Dependency> dependencyList = getDependenciesFromNode((Element) dependencyNode);
                    dependencyMap.put(dependency, dependencyList);
                }
            }
        }
        return dependencyMap;
    }

    private List<Dependency> getDependenciesFromNode(Element dependencyNode) {
        List<Dependency> dependencyList = new ArrayList<>();
        final NodeList dependencyDependenciesNodeList = dependencyNode.getElementsByTagName(DEPENDENCIES);
        for (int k = 0; k < dependencyDependenciesNodeList.getLength(); k++) {
            Node dependencyDependenciesNode = dependencyDependenciesNodeList.item(k);
            if (dependencyDependenciesNode.getNodeType() == ELEMENT_NODE) {
                NodeList dependencyDependencies = dependencyDependenciesNode.getChildNodes();
                for (int j = 0; j < dependencyDependencies.getLength(); j++) {
                    Node dependencyDependencyNode = dependencyDependencies.item(j);
                    if (dependencyDependencyNode.getNodeType() == ELEMENT_NODE) {
                        Dependency dependencyDependency = buildDependency((Element) dependencyDependencyNode);
                        if (dependencyDependency != null) {
                            dependencyList.add(dependencyDependency);
                        }
                    }
                }
            }
        }
        return dependencyList;
    }

    private Dependency buildDependency(Element dependencyElement) {
        final String id = getSingleChildElement(dependencyElement, ID).getTextContent();
        final String type = getSingleChildElement(dependencyElement, TYPE).getTextContent();

        Class<? extends Entity> typeClass = entityTypeRegistry.getEntityClass(type);
        if (typeClass != null) {
            return new Dependency(id, typeClass);
        }

        return null;
    }

    private void handleItem(final Element element, final Bundle bundle) {
        final String type = getSingleChildElement(element, TYPE).getTextContent();
        final EntityLoader entityLoader = entityLoaderRegistry.getLoader(type);
        if (entityLoader != null) {
            final Entity entity = entityLoader.load(element);
            if (entity != null) {
                bundle.addEntity(entity);
            }
        } else {
            LOGGER.log(Level.INFO, "No entity loader found for entity type: {0}", type);
        }
    }
}
