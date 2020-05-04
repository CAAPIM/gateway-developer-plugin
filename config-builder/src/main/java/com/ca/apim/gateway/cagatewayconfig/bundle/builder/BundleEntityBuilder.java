/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants.*;
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

    public Map<String, Element> build(Bundle bundle, EntityBuilder.BundleType bundleType,
                                                            Document document, String bundleName,
                                                            String bundleVersion) {
        List<Entity> entities = new ArrayList<>();
        entityBuilders.forEach(builder -> entities.addAll(builder.build(bundle, bundleType, document)));

        Map<String, Element> artifacts = buildAnnotatedEntities(entities, bundle, bundleType, document, bundleName, bundleVersion);
        if (artifacts.isEmpty()) {
            artifacts.put(bundleName + '-' + bundleVersion, bundleDocumentBuilder.build(document, entities));
        }

        return artifacts;
    }

    private Map<String, Element> buildAnnotatedEntities(List<Entity> entities, Bundle bundle, EntityBuilder.BundleType bundleType,
                                                        Document document, String bundleName,
                                                        String bundleVersion) {
        Map<String, Element> annotatedElements = new LinkedHashMap<>();

        // Filter the bundle to export only annotated entities
        // TODO : Enhance this logic to support services and policies
        bundle.getEntities(Encass.class).entrySet().stream()
                .filter(entry -> entry.getValue().hasAnnotated())
                .map(entry -> createAnnotatedEntity(entry.getValue(), bundleVersion, entry.getKey()))
                .forEach(annotatedEntity -> {
                    if (annotatedEntity.isBundleTypeEnabled()) {
                        // buildEncassDependencies
                        List<Entity> entityList = getEntityDependencies(annotatedEntity.getPolicyName(), entities, bundle);
                        LOGGER.log(Level.FINE, "Entity list : " + entityList);
                        // Create bundle
                        final Element annotatedBundle = bundleDocumentBuilder.build(document, entityList);
                        annotatedElements.put(annotatedEntity.getBundleName(), annotatedBundle);
                    }
                });

        return annotatedElements;
    }

    private String getPolicyName(final String fullPath) {
        final int index = fullPath.lastIndexOf("/");
        return index > -1 ? fullPath.substring(index + 1) : fullPath;
    }

    private List<Entity> buildEncassDependencies(final AnnotatedEntity<Encass> annotatedEntity, final List<Entity> entities, final Bundle bundle) {
        Map<Dependency, List<Dependency>> dependencyListMap = bundle.getDependencyMap();
        Set<Entity> filteredEntities = new HashSet<>();

        if (dependencyListMap != null) {
            collectDependencies(filteredEntities, entities, bundle, new Dependency(getPolicyName(annotatedEntity.getEntity().getPolicy()), EntityTypes.POLICY_TYPE));
        }

        return new ArrayList<>(filteredEntities);
    }

    private void collectDependencies(final Set<Entity> filteredEntities, final List<Entity> entities, final Bundle bundle, Dependency dependency) {
        Map<Dependency, List<Dependency>> dependencyListMap = bundle.getDependencyMap();
        if (dependencyListMap != null) {
            List<Dependency> dependencies = dependencyListMap.get(dependency);
            if (dependencies != null) {
                dependencies.forEach(dependencyItem -> {
                    for (Entity entity : entities) {
                        int index = entity.getName().lastIndexOf("/");
                        final String entityName = index > -1 ? entity.getName().substring(index + 1) : entity.getName();
                        if (dependencyItem.getName().equals(entityName) && dependencyItem.getEntityType().equals(entity.getType())) {
                            filteredEntities.add(entity);
                            // TODO: I expect to call collectDependencies recursively for every filtered entity.
                            // collectDependencies(...)
                            break;
                        }
                    }
                });
            }
        }
    }

    private List<Entity> getEntityDependencies(String policyNameWithPath, List<Entity> entities, Bundle bundle) {
        List<Entity> entityDependenciesList = new ArrayList<>();
        Set<String> filteredEntityIds = new HashSet<>();
        Map<Dependency, List<Dependency>> dependencyListMap = bundle.getDependencyMap();
        if (dependencyListMap != null) {
            int pathIndex = policyNameWithPath.lastIndexOf("/");
            final String policyName = pathIndex > -1 ? policyNameWithPath.substring(pathIndex + 1) : policyNameWithPath;
            Set<Map.Entry<Dependency, List<Dependency>>> entrySet = dependencyListMap.entrySet();
            for (Map.Entry<Dependency, List<Dependency>> entry : entrySet) {
                final Dependency dependencyParent = entry.getKey();
                if (dependencyParent.getName().equals(policyName)) {
                    //Add the policy dependant folders
                    final Map<String, Policy> entityMap = bundle.getPolicies();
                    if (!entityMap.isEmpty()) {
                        final GatewayEntity policyEntity = entityMap.get(policyNameWithPath);
                        populateDependentFolders(filteredEntityIds, policyEntity);
                        filteredEntityIds.add(policyEntity.getId());
                    }

                    //Add the policy dependencies
                    for (Dependency dependency : entry.getValue()) {
                        for (Entity entity : entities) {
                            int index = entity.getName().lastIndexOf("/");
                            final String entityName = index > -1 ? entity.getName().substring(index + 1) : entity.getName();
                            if (dependency.getName().equals(entityName) && dependency.getType().equals(entity.getType())) {
                                filteredEntityIds.add(entity.getId());
                            }
                        }
                    }

                    final List<Entity> filteredEntities = entities.stream().filter(entity -> filteredEntityIds.contains(entity.getId())).collect(Collectors.toList());
                    entityDependenciesList.addAll(filteredEntities);

                    return entityDependenciesList;
                }
            }
        }

        return entityDependenciesList;
    }

    private void populateDependentFolders(Set<String> filteredEntityIds, GatewayEntity policyEntity) {
        if (policyEntity instanceof Folderable) {
            Folder folder = ((Folderable) policyEntity).getParentFolder();
            while (folder != null) {
                filteredEntityIds.add(folder.getId());
                folder = folder.getParentFolder();
            }
        }
    }

    /**
     * Creates AnnotatedEntity object by scanning all the annotations and gathering all the information required to
     * generate the bundle and its metadata.
     *
     * @param encass Encapsulated assertion
     * @param bundleVersion Bundle version
     * @return AnnotatedEntity
     */
    private AnnotatedEntity<Encass> createAnnotatedEntity(final Encass encass, final String bundleVersion, final String entityName) {
        AnnotatedEntity<Encass> annotatedEntity = new AnnotatedEntity<>(encass);
        encass.getAnnotations().forEach(annotation -> {
            switch (annotation.getType()) {
                case ANNOTATION_TYPE_BUNDLE:
                    String annotatedBundleName = annotation.getName();
                    if (StringUtils.isBlank(annotatedBundleName)) {
                        annotatedBundleName = entityName;
                    }
                    String description = annotation.getDescription();
                    if (StringUtils.isBlank(annotatedBundleName)) {
                        description = encass.getProperties().getOrDefault("description", "").toString();
                    }
                    annotatedEntity.setTags(annotation.getTags());
                    annotatedEntity.setBundleType(true);
                    annotatedEntity.setEntityName(entityName);
                    annotatedEntity.setDescription(description);
                    annotatedEntity.setEntityType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
                    annotatedEntity.setBundleName(annotatedBundleName + "-" + bundleVersion);
                    annotatedEntity.setPolicyName(encass.getPolicy());
                    break;
                case ANNOTATION_TYPE_REUSABLE:
                    annotatedEntity.setReusableType(true);
                    break;
                case ANNOTATION_TYPE_REDEPLOYABLE:
                    annotatedEntity.setRedeployableType(true);
                    break;
                case ANNOTATION_TYPE_EXCLUDE:
                    annotatedEntity.setExcludeType(true);
                    break;
                default:
                    break;
            }
        });
        return annotatedEntity;
    }

    @VisibleForTesting
    public Set<EntityBuilder> getEntityBuilders() {
        return entityBuilders;
    }
}
