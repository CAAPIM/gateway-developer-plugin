/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityTypeRegistry;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleEntityLoader;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleEntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static org.w3c.dom.Node.ELEMENT_NODE;

public class BundleBuilder {
    private static final Logger LOGGER = Logger.getLogger(BundleBuilder.class.getName());
    private final BundleEntityLoaderRegistry entityLoaderRegistry;
    private final EntityTypeRegistry entityTypeRegistry;

    public BundleBuilder() {
        this.entityLoaderRegistry = InjectionRegistry.getInstance(BundleEntityLoaderRegistry.class);
        this.entityTypeRegistry = InjectionRegistry.getInstance(EntityTypeRegistry.class);
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

        bundle.buildFolderTree();
        bundle.setDependencyMap(buildDependencies(getSingleChildElement(getSingleChildElement(bundleElement, DEPENDENCY_GRAPH), DEPENDENCIES)));

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

        Class<? extends GatewayEntity> typeClass = entityTypeRegistry.getEntityClass(type);
        if (typeClass != null) {
            return new Dependency(id, typeClass);
        }

        return null;
    }

    private void handleItem(final Element element, final Bundle bundle) {
        if (getSingleChildElement(element, TYPE, true) == null) {
            return;
        }
        final String type = getSingleChildElement(element, TYPE).getTextContent();
        final BundleEntityLoader entityLoader = entityLoaderRegistry.getLoader(type);
        if (entityLoader != null) {
            entityLoader.load(bundle, element);
        } else {
            LOGGER.log(Level.INFO, "No entity loader found for entity type: {0}", type);
            loadUnsupportedEntity(element, bundle);
        }
    }

    private void loadUnsupportedEntity(final Element element, final Bundle bundle) {
        final UnsupportedGatewayEntity entity = new UnsupportedGatewayEntity();
        entity.setType(getSingleChildElement(element, TYPE).getTextContent());
        entity.setId(getSingleChildElement(element, ID).getTextContent());
        entity.setName(getSingleChildElement(element, NAME).getTextContent());
        entity.setElement(element);

        bundle.addEntity(entity);
    }
}
