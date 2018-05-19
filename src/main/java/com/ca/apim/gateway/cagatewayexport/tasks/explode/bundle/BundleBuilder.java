/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.*;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BundleBuilder {
    private static final Logger LOGGER = Logger.getLogger(BundleBuilder.class.getName());
    private final Bundle bundle;
    private final EntityLoaderRegistry entityLoaderRegistry;

    public BundleBuilder() {
        bundle = new Bundle();
        this.entityLoaderRegistry = new EntityLoaderRegistry();

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

        bundle.setDependencies(buildDependencies(EntityLoaderHelper.getSingleChildElement(EntityLoaderHelper.getSingleChildElement(bundleElement, "l7:DependencyGraph"), "l7:Dependencies")));
    }

    private Map<Dependency, List<Dependency>> buildDependencies(Element dependenciesElement) {
        Map<Dependency, List<Dependency>> dependencyMap = new HashMap<>();
        NodeList bundleDependencies = dependenciesElement.getChildNodes();
        for (int i = 0; i < bundleDependencies.getLength(); i++) {
            Node dependencyNode = bundleDependencies.item(i);
            if (dependencyNode.getNodeType() == Node.ELEMENT_NODE) {
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
        final NodeList dependencyDependenciesNodeList = dependencyNode.getElementsByTagName("l7:Dependencies");
        for (int k = 0; k < dependencyDependenciesNodeList.getLength(); k++) {
            Node dependencyDependenciesNode = dependencyDependenciesNodeList.item(k);
            if (dependencyDependenciesNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList dependencyDependencies = dependencyDependenciesNode.getChildNodes();
                for (int j = 0; j < dependencyDependencies.getLength(); j++) {
                    Node dependencyDependencyNode = dependencyDependencies.item(j);
                    if (dependencyDependencyNode.getNodeType() == Node.ELEMENT_NODE) {
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
        final String id = EntityLoaderHelper.getSingleChildElement(dependencyElement, "l7:Id").getTextContent();
        final String type = EntityLoaderHelper.getSingleChildElement(dependencyElement, "l7:Type").getTextContent();

        Class<? extends Entity> typeClass = convertType(type);
        if (typeClass != null) {
            return new Dependency(id, typeClass);
        } else {
            return null;
        }
    }

    private Class<? extends Entity> convertType(String type) {
        switch (type) {
            case "SERVICE":
                return ServiceEntity.class;
            case "POLICY":
                return PolicyEntity.class;
            case "POLICY_BACKED_SERVICE":
                return PolicyBackedServiceEntity.class;
            case "FOLDER":
                return Folder.class;
            case "ENCAPSULATED_ASSERTION":
                return EncassEntity.class;
            case "CLUSTER_PROPERTY":
                return ClusterProperty.class;
            default:
                return null;
        }
    }

    public Bundle getBundle() {
        return bundle;
    }

    private void handleItem(final Element element) {
        final String type = EntityLoaderHelper.getSingleChildElement(element, "l7:Type").getTextContent();
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
