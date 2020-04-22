/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.Dependency;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.Folderable;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.google.common.annotations.VisibleForTesting;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants.ANNOTATION_TYPE_BUNDLE;
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

    public Map<String, Element> build(Bundle bundle, EntityBuilder.BundleType bundleType, Document document, String bundleName, String bundleVersion) {
        List<Entity> entities = new ArrayList<>();
        entityBuilders.forEach(builder -> entities.addAll(builder.build(bundle, bundleType, document)));

        Map<String, Entity> annotatedEntities = new HashMap<>();
        // Filter the bundle to export only annotated entities
        // TODO : Enhance this logic to support services and policies
        final Map<String, Encass> encassEntities = bundle.getEntities(Encass.class);
        encassEntities.entrySet().parallelStream().forEach(encassEntry -> {
            Encass encass = encassEntry.getValue();
            if (encass.getAnnotations() != null) {
                encass.getAnnotations().forEach(entityAnnotation -> {
                    if (entityAnnotation.getType().equals(ANNOTATION_TYPE_BUNDLE)) {
                        final String annotatedEntityName = entityAnnotation.getName();
                        final String annotatedEntityDesc = entityAnnotation.getDescription();
                        final Entity filteredEntity = entities.parallelStream().filter(entity -> encassEntry.getKey().equals(entity.getName())).findAny().get();
                        annotatedEntities.put(annotatedEntityName != null ? annotatedEntityName + '-' + bundleVersion : encassEntry.getKey() + '-' + bundleVersion, filteredEntity);
                    }
                });
            }
        });

        if (annotatedEntities.size() > 0) {
            Map<String, Element> annotatedElements = new HashMap<>();
            annotatedEntities.entrySet().parallelStream().forEach(annotatedEntity -> {
                List<Entity> entityList = getEntityDependencies(annotatedEntity.getValue(), entities, bundle);
                LOGGER.log(Level.WARNING, "Annotated entity dependencies" + entityList);
                if (EntityBuilder.BundleType.DEPLOYMENT == bundleType) {
                    entityList.add(annotatedEntity.getValue());
                }
                annotatedElements.put(annotatedEntity.getKey(), bundleDocumentBuilder.build(document, entityList));
            });
            return annotatedElements;
        }

        return new HashMap<String, Element>() {
            {
                put(bundleName + '-' + bundleVersion, bundleDocumentBuilder.build(document, entities));
            }
        };
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

    @VisibleForTesting
    public Set<EntityBuilder> getEntityBuilders() {
        return entityBuilders;
    }
}
