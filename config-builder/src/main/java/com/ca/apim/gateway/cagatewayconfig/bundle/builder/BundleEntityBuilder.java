/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
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
        List<Entity> annotatedEntities = new ArrayList<>();
        //TODO this should be for annotated entities(service, encass, policy)
        entityBuilders.forEach(builder -> annotatedEntities.addAll(builder.build(bundle, EntityBuilder.BundleType.DEPLOYMENT, document).stream().filter(e -> e.getType().equals("POLICY")).collect(Collectors.toList())));

        Map<String, Element> annotatedElements = new HashMap<>();
        for (Entity annotatedEntity : annotatedEntities) {

            List<Entity> entityList = getEntityDependencies(annotatedEntity, entities, bundle);
            LOGGER.log(Level.WARNING, "Annotated entity dependencies" + entityList);
            if (EntityBuilder.BundleType.DEPLOYMENT == bundleType) {
                entityList.add(annotatedEntity);
            }
            annotatedElements.put(annotatedEntity.getName(), bundleDocumentBuilder.build(document, entityList));
        }
        return annotatedElements;

    }

    private void populatedDependentFolders(List<Entity> entityDependenciesList, List<Entity> entities, GatewayEntity annotatedItem) {

        if (annotatedItem instanceof Folderable) {
            Folder folder = ((Folderable) annotatedItem).getParentFolder();
            while (folder != null) {
                final String id = folder.getId();
                Optional<Entity> optionalEntity = entities.stream().filter(e -> id.equals(e.getId())).findFirst();
                if (optionalEntity.isPresent()) {
                    entityDependenciesList.add(optionalEntity.get());
                }
                folder = folder.getParentFolder();
            }
        }
    }

    private List<Entity> getEntityDependencies(Entity entity, List<Entity> entities, Bundle bundle) {
        List<Entity> entityDependenciesList = new ArrayList<>();
        Map<Dependency, List<Dependency>> dependencyListMap = bundle.getDependencyMap();
        if (dependencyListMap != null) {
            Set<Map.Entry<Dependency, List<Dependency>>> entrySet = dependencyListMap.entrySet();
            for (Map.Entry<Dependency, List<Dependency>> entry : entrySet) {
                Dependency annotatedEntity = entry.getKey();
                if (annotatedEntity.getName().equals(entity.getName())) {
                    List<Dependency> dependencyList = entry.getValue();
                    for (Dependency dependency : dependencyList) {
                        for (Entity depEntity : entities) {
                            if (dependency.getName().equals(depEntity.getName()) && depEntity.getType().equals(dependency.getEntityType())) {
                                entityDependenciesList.add(depEntity);
                            }
                        }
                    }

                    Map<String, ? extends GatewayEntity> entityMap = bundle.getEntities(annotatedEntity.getType());
                    if (entityMap != null) {
                        GatewayEntity annotatedItem = entityMap.get(annotatedEntity.getName());
                        populatedDependentFolders(entityDependenciesList, entities, annotatedItem);
                    }

                    LOGGER.log(Level.WARNING, "entityDependenciesList" + entityDependenciesList);
                    return entityDependenciesList;
                }
            }
        }

        return entityDependenciesList;
    }

    @VisibleForTesting
    public Set<EntityBuilder> getEntityBuilders() {
        return entityBuilders;
    }
}
