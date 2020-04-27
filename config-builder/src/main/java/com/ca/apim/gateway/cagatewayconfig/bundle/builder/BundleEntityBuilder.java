/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections4.CollectionUtils;
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

    public Map<String, Element> build(Bundle bundle, EntityBuilder.BundleType bundleType, Document document, String bundleName, String bundleVersion) {
        List<Entity> entities = new ArrayList<>();
        entityBuilders.forEach(builder -> entities.addAll(builder.build(bundle, bundleType, document)));

        List<AnnotatedEntity> annotatedEntities = new ArrayList<>();
        // Filter the bundle to export only annotated entities
        // TODO : Enhance this logic to support services and policies
        final Map<String, Encass> encassEntities = bundle.getEntities(Encass.class);
        encassEntities.entrySet().parallelStream().forEach(encassEntry -> {
            Encass encass = encassEntry.getValue();
            if (encass.getAnnotations() != null) {
                encass.getAnnotations().forEach(annotation -> {
                    AnnotatedEntity annotatedEntity = new AnnotatedEntity();
                    if (annotation instanceof Map) {
                        if (((Map) annotation).containsKey("type")) {
                            try {
                                switch (((Map) annotation).get("type").toString()) {
                                    case ANNOTATION_TYPE_BUNDLE:
                                        final String annotatedBundleName = ((Map) annotation).containsKey("name") ? ((Map) annotation).get("name").toString() : null;
                                        final ArrayList<String> tags = ((Map) annotation).containsKey("tags") ? (ArrayList<String>) ((Map) annotation).get("tags") : null;
                                        if (CollectionUtils.isNotEmpty(tags)) {
                                            //ToDo : Tags logic
                                        }
                                        annotatedEntity.setBundleType(true);
                                        annotatedEntity.setEntityName(encassEntry.getKey());
                                        annotatedEntity.setEntityType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
                                        annotatedEntity.setBundleName(annotatedBundleName != null ? annotatedBundleName + '-' + bundleVersion : encassEntry.getKey() + '-' + bundleVersion);
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
                            } catch (ClassCastException ex) {
                                throw new EntityBuilderException("Unable to build bundle for entity : " + encassEntry.getKey() + ", please check the annotations syntax.", ex);
                            }
                        }
                    }

                    if (annotation instanceof String) {
                        switch (annotation.toString()) {
                            case ANNOTATION_TYPE_BUNDLE:
                                annotatedEntity.setBundleType(true);
                                annotatedEntity.setEntityName(encassEntry.getKey());
                                annotatedEntity.setEntityType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
                                annotatedEntity.setBundleName(encassEntry.getKey() + '-' + bundleVersion);
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
                    }
                    annotatedEntities.add(annotatedEntity);
                });
            }
        });

        if(!annotatedEntities.isEmpty()) {
            Map<String, Element> annotatedElements = new LinkedHashMap<>();
            if (EntityBuilder.BundleType.DEPLOYMENT == bundleType) {
                annotatedEntities.stream().forEach(annotatedEntity -> {
                    if (annotatedEntity.isBundleTypeEnabled()) {
                        List<Entity> entityList = getEntityDependencies(annotatedEntity.getEntityName(), annotatedEntity.getEntityType(), annotatedEntity.getPolicyName(), entities, bundle);
                        LOGGER.log(Level.FINE, "Annotated entity list : " + entityList);
                        annotatedElements.put(annotatedEntity.getBundleName(), bundleDocumentBuilder.build(document, entityList));
                    }
                });
            }
            return annotatedElements;
        }

        Map<String, Element> bundles = new HashMap<>();
        bundles.put(bundleName + '-' + bundleVersion, bundleDocumentBuilder.build(document, entities));
        return bundles;
    }

    private List<Entity> getEntityDependencies(String annotatedEntityName, String annotatedEntityType, String policyNameWithPath, List<Entity> entities, Bundle bundle) {
        List<Entity> entityDependenciesList = new ArrayList<>();
        Map<Dependency, List<Dependency>> dependencyListMap = bundle.getDependencyMap();
        if (dependencyListMap != null) {
            int pathIndex = policyNameWithPath.lastIndexOf("/");
            final String policyName = pathIndex > -1 ? policyNameWithPath.substring(pathIndex + 1) : policyNameWithPath;
            Set<Map.Entry<Dependency, List<Dependency>>> entrySet = dependencyListMap.entrySet();
            for (Map.Entry<Dependency, List<Dependency>> entry : entrySet) {
                final Dependency dependencyParent = entry.getKey();
                if (dependencyParent.getName().equals(policyName)) {
                    //Add the dependant folders first
                    final Map<String, Policy> entityMap = bundle.getPolicies();
                    if (entityMap != null) {
                        final GatewayEntity policyEntity = entityMap.get(policyNameWithPath);
                        populatedDependentFolders(entityDependenciesList, entities, policyEntity);
                    }

                    //Add the policy dependencies
                    for (Dependency dependency : entry.getValue()) {
                        if (!dependency.getName().equals(annotatedEntityName) && !dependency.getType().equals(annotatedEntityType)) {
                            for (Entity entity : entities) {
                                int index = entity.getName().lastIndexOf("/");
                                final String entityName = index > -1 ? entity.getName().substring(index + 1) : entity.getName();
                                if (dependency.getName().equals(entityName) && dependency.getType().equals(entity.getType())) {
                                    entityDependenciesList.add(entity);
                                }
                            }
                        }
                    }

                    //Add the parent entities
                    final List<Entity> parentEntities = entities.stream().filter(entity -> policyNameWithPath.equals(entity.getName()) || annotatedEntityName.equals(entity.getName())).collect(Collectors.toList());
                    entityDependenciesList.addAll(parentEntities);

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
