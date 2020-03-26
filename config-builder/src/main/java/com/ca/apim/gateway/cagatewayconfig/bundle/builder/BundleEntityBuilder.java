/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils;
import com.google.common.annotations.VisibleForTesting;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableSet;

@Singleton
public class BundleEntityBuilder {
    private static final Logger LOGGER = Logger.getLogger(BundleEntityBuilder.class.getName());
    private final Set<EntityBuilder> entityBuilders;
    private final BundleDocumentBuilder bundleDocumentBuilder;

    @Inject
    BundleEntityBuilder(final Set<EntityBuilder> entityBuilders, final BundleDocumentBuilder bundleDocumentBuilder) {
        // treeset is needed here to sort the builders in the proper order to get a correct bundle builded
        // Ordering is necessary for the bundle, for the gateway to load it properly.
        this.entityBuilders = unmodifiableSet(new TreeSet<>(entityBuilders));
        this.bundleDocumentBuilder = bundleDocumentBuilder;
    }

    public Map<String, Element> build(Bundle bundle, EntityBuilder.BundleType bundleType, Document document) {
        List<Entity> entities = new ArrayList<>();
        entityBuilders.forEach(builder -> entities.addAll(builder.build(bundle, bundleType, document)));
        List<Entity> serviceEntities = new ArrayList<>();
        //TODO this should be for annotated entities(service, encass, policy)
        entityBuilders.forEach(builder -> serviceEntities.addAll(builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, document).stream().filter(e -> e.getType().equals("SERVICE")).collect(Collectors.toList())));

        Map<String, Element> serviceElements = new HashMap<>();
        for (Entity serviceEntity : serviceEntities) {

            List<Entity> entityList = getServiceDependencies(serviceEntity, entities, bundle);
            LOGGER.log(Level.WARNING, "Service dependencies" + entityList);
            if (EntityBuilder.BundleType.DEPLOYMENT == bundleType) {
                entityList.add(serviceEntity);
            }
            serviceElements.put(serviceEntity.getName(), bundleDocumentBuilder.build(document, entityList));
        }
        return serviceElements;

    }

    private List<Entity> getServiceDependencies(Entity entity, List<Entity> entities, Bundle bundle) {
        List<Entity> serviceDependenciesList = new ArrayList<>();
        Map<Dependency, List<Dependency>> dependencyListMap = bundle.getDependencyMap();
        Set<Map.Entry<Dependency, List<Dependency>>> entrySet = dependencyListMap.entrySet();
        for (Map.Entry<Dependency, List<Dependency>> entry : entrySet) {
            Dependency service = entry.getKey();
            if (service.getName().equals(entity.getName())) {
                List<Dependency> dependencyList = entry.getValue();
                for (Dependency dependency : dependencyList) {
                    for (Entity depEntity : entities) {
                        if (dependency.getName().equals(depEntity.getName()) && depEntity.getType().equals(dependency.getEntityType())) {
                            serviceDependenciesList.add(depEntity);
                        }
                    }
                }
                LOGGER.log(Level.WARNING, "serviceDependenciesList" + serviceDependenciesList);
                return serviceDependenciesList;
            }
        }

        return serviceDependenciesList;
    }

    @VisibleForTesting
    public Set<EntityBuilder> getEntityBuilders() {
        return entityBuilders;
    }
}
