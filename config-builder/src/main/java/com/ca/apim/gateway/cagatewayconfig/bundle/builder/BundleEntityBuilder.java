/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Collections.unmodifiableSet;

@Singleton
public class BundleEntityBuilder {
    private static final Logger LOGGER = Logger.getLogger(BundleEntityBuilder.class.getName());
    private final Set<EntityBuilder> entityBuilders;
    private final BundleDocumentBuilder bundleDocumentBuilder;
    private final BundleMetadataBuilder bundleMetadataBuilder;
    private final EntityTypeRegistry entityTypeRegistry;

    @Inject
    BundleEntityBuilder(final Set<EntityBuilder> entityBuilders, final BundleDocumentBuilder bundleDocumentBuilder,
                        final BundleMetadataBuilder bundleMetadataBuilder, final EntityTypeRegistry entityTypeRegistry) {
        // treeset is needed here to sort the builders in the proper order to get a correct bundle builded
        // Ordering is necessary for the bundle, for the gateway to load it properly.
        this.entityBuilders = unmodifiableSet(new TreeSet<>(entityBuilders));
        this.bundleDocumentBuilder = bundleDocumentBuilder;
        this.bundleMetadataBuilder = bundleMetadataBuilder;
        this.entityTypeRegistry = entityTypeRegistry;
    }

    public Map<String, Pair<Element, BundleMetadata>> build(Bundle bundle, EntityBuilder.BundleType bundleType,
                                                            Document document, String projectName,
                                                            String projectGroupName, String projectVersion) {

        Map<String, Pair<Element, BundleMetadata>> artifacts = buildAnnotatedEntities(bundleType, bundle, document, projectName,
                projectGroupName, projectVersion);
        if (artifacts.isEmpty()) {
            List<Entity> entities = new ArrayList<>();
            entityBuilders.forEach(builder -> entities.addAll(builder.build(bundle, bundleType, document)));
            artifacts.put(StringUtils.isBlank(projectVersion) ? projectName : projectName + "-" + projectVersion,
                    ImmutablePair.of(bundleDocumentBuilder.build(document, entities), null));
        }
        return artifacts;
    }

    private Map<String, Pair<Element, BundleMetadata>> buildAnnotatedEntities(EntityBuilder.BundleType bundleType, Bundle bundle,
                                                                              Document document, String projectName,
                                                                              String projectGroupName,
                                                                              String projectVersion) {
        final Map<String, Pair<Element, BundleMetadata>> annotatedElements = new LinkedHashMap<>();
        Map<String, EntityUtils.GatewayEntityInfo> entityTypeMap = entityTypeRegistry.getEntityTypeMap();
        // Filter the bundle to export only annotated entities
        entityTypeMap.values().stream().filter(gatewayEntityInfo -> gatewayEntityInfo.isBundleGenerationSupported()).forEach(entityInfo ->
                bundle.getEntities(entityInfo.getEntityClass()).values().stream()
                        .filter(GatewayEntity::hasBundleAnnotation)
                        .map(entity -> entity.getAnnotatedEntity(projectName, projectVersion))
                        .forEach(annotatedEntity -> {
                            List<Entity> entities = new ArrayList<>();
                            Map<String, GatewayEntity> entityMap = getEntityDependencies(annotatedEntity.getPolicyName(), bundle);
                            entityMap.put(getKeyPrefix(annotatedEntity.getEntity()) + annotatedEntity.getEntityName(), annotatedEntity.getEntity());
                            entityBuilders.forEach(builder -> entities.addAll(builder.build(entityMap, annotatedEntity, bundle, bundleType, document)));
                            // Create bundle
                            final Element annotatedBundle = bundleDocumentBuilder.build(document, entities);
                            final BundleMetadata bundleMetadata = bundleMetadataBuilder.build(annotatedEntity, entities, projectGroupName, projectVersion);
                            annotatedElements.put(annotatedEntity.getBundleName(), ImmutablePair.of(annotatedBundle,
                                    bundleMetadata));
                        })
        );

        return annotatedElements;
    }

    private Map<String, GatewayEntity> getEntityDependencies(String policyNameWithPath, Bundle bundle) {
        Map<String, GatewayEntity> entityDependenciesMap = new HashMap<>();
        final Map<String, Policy> entityMap = bundle.getPolicies();
        final Policy policyEntity = entityMap.get(policyNameWithPath);
        if (policyEntity != null) {
            populateDependentFolders(entityDependenciesMap, policyEntity);
            entityDependenciesMap.put(getKeyPrefix(policyEntity) + policyNameWithPath, policyEntity);
            Set<Dependency> dependencies = policyEntity.getUsedEntities();
            for (Dependency dependency : dependencies) {
                Map<String, ? extends GatewayEntity> entities = bundle.getEntities(entityTypeRegistry.getEntityClass(dependency.getType()));
                GatewayEntity dependentEntity = entities.get(dependency.getName());
                entityDependenciesMap.put(dependency.getType() + ":" + dependency.getName(), dependentEntity);
            }
        }
        return entityDependenciesMap;
    }

    private void populateDependentFolders(Map<String, GatewayEntity> gatewayEntities, GatewayEntity policyEntity) {
        if (policyEntity instanceof Folderable) {
            Folder folder = ((Folderable) policyEntity).getParentFolder();
            while (folder != null) {
                gatewayEntities.put(getKeyPrefix(folder) + folder.getPath(), folder);
                folder = folder.getParentFolder();
            }
        }
    }

    private String getKeyPrefix(GatewayEntity entity) {
        return EntityUtils.getEntityType(entity.getClass()) + ":";
    }

    @VisibleForTesting
    public Set<EntityBuilder> getEntityBuilders() {
        return entityBuilders;
    }
}
