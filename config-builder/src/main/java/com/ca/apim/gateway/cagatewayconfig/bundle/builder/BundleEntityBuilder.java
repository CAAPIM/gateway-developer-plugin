/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

        Map<String, Pair<String, Entity>> annotatedEntities = new HashMap<>();
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
                        annotatedEntities.put(annotatedEntityName != null ? annotatedEntityName + '-' + bundleVersion : encassEntry.getKey() + '-' + bundleVersion, ImmutablePair.of(encass.getPolicy(), filteredEntity));
                    }
                });
            }
        });

        if (!annotatedEntities.isEmpty()) {
            Map<String, Element> annotatedElements = new HashMap<>();
            annotatedEntities.entrySet().parallelStream().forEach(annotatedEntity -> {
                List<Entity> entityList = getEntityDependencies(annotatedEntity.getValue().getRight().getName(), annotatedEntity.getValue().getRight().getType(), annotatedEntity.getValue().getLeft(), entities, bundle);
                LOGGER.log(Level.FINE, "Annotated entity dependencies" + entityList);
                if (EntityBuilder.BundleType.DEPLOYMENT == bundleType) {
                    entityList.add(annotatedEntity.getValue().getRight());
                }
                annotatedElements.put(annotatedEntity.getKey(), bundleDocumentBuilder.build(document, entityList));
            });
            return annotatedElements;
        }

        Map<String, Element> bundles = new HashMap<>();
        bundles.put(bundleName + '-' + bundleVersion, bundleDocumentBuilder.build(document, entities));
        return bundles;
    }

    private List<Entity> getEntityDependencies(String annotatedEntityName, String annotatedEntityType, String encassPolicyNameWithPath, List<Entity> entities, Bundle bundle) {
        List<Entity> entityDependenciesList = new ArrayList<>();
        Map<Dependency, List<Dependency>> dependencyListMap = bundle.getDependencyMap();
        if (dependencyListMap != null) {
            int pathIndex = encassPolicyNameWithPath.lastIndexOf("/");
            final String encassPolicyName = pathIndex > -1 ? encassPolicyNameWithPath.substring(pathIndex + 1) : encassPolicyNameWithPath;
            Set<Map.Entry<Dependency, List<Dependency>>> entrySet = dependencyListMap.entrySet();
            for (Map.Entry<Dependency, List<Dependency>> entry : entrySet) {
                final Dependency dependencyParent = entry.getKey();
                if (dependencyParent.getName().equals(encassPolicyName)) {
                    //Add the dependant folders first
                    final Map<String, Policy> entityMap = bundle.getPolicies();
                    if (entityMap != null) {
                        final GatewayEntity policyEntity = entityMap.get(encassPolicyNameWithPath);
                        populatedDependentFolders(entityDependenciesList, entities, policyEntity);
                    }

                    //Add the dependencies
                    for (Dependency dependency : entry.getValue()) {
                        if (!dependency.getName().equals(annotatedEntityName) && !dependency.getType().equals(annotatedEntityType)) {
                            for (Entity entity : entities) {
                                int index = entity.getName().lastIndexOf("/");
                                final String entityName = index > -1 ? entity.getName().substring(index + 1) : entity.getName();
                                if (dependency.getName().equals(entityName) && entity.getType().equals(dependency.getType())) {
                                    entityDependenciesList.add(entity);
                                }
                            }
                        }
                    }

                    //Add the parent policy
                    final Entity parentPolicyEntity = entities.parallelStream().filter(entity -> encassPolicyNameWithPath.equals(entity.getName())).findAny().get();
                    entityDependenciesList.add(parentPolicyEntity);

                    return entityDependenciesList;
                }
            }
        }

        return entityDependenciesList;
    }

    private void populatedDependentFolders(List<Entity> entityDependenciesList, List<Entity> entities, GatewayEntity policyEntity) {
        if (policyEntity instanceof Folderable) {
            Folder folder = ((Folderable) policyEntity).getParentFolder();
            final Set<String> folderIds = new HashSet<>();
            while (folder != null) {
                folderIds.add(folder.getId());
                folder = folder.getParentFolder();
            }
            final List<Entity> folderDependencies = entities.stream().filter(e -> folderIds.contains(e.getId())).collect(Collectors.toList());
            entityDependenciesList.addAll(folderDependencies);
        }
    }

    @VisibleForTesting
    public Set<EntityBuilder> getEntityBuilders() {
        return entityBuilders;
    }
}
