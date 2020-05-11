/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static java.util.Collections.unmodifiableSet;

@Singleton
public class BundleEntityBuilder {

    private final Set<EntityBuilder> entityBuilders;
    private final BundleDocumentBuilder bundleDocumentBuilder;
    private final BundleMetadataBuilder bundleMetadataBuilder;
    private final EntityTypeRegistry entityTypeRegistry;

    @Inject
    BundleEntityBuilder(final Set<EntityBuilder> entityBuilders, final BundleDocumentBuilder bundleDocumentBuilder,
                        final BundleMetadataBuilder bundleMetadataBuilder, final EntityTypeRegistry entityTypeRegistry) {
        // treeset is needed here to sort the builders in the proper order to get a correct bundle build
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
        entityTypeMap.values().stream().filter(EntityUtils.GatewayEntityInfo::isBundleGenerationSupported).forEach(entityInfo ->
                bundle.getEntities(entityInfo.getEntityClass()).values().stream()
                        .filter(entity -> entity instanceof AnnotableEntity)
                        .map(entity -> ((AnnotableEntity) entity).getAnnotatedEntity())
                        .forEach(annotatedEntity -> {
                            if (annotatedEntity != null && annotatedEntity.isBundle()) {
                                List<Entity> entities = new ArrayList<>();
                                AnnotatedBundle annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity);
                                annotatedBundle.setProjectName(projectName);
                                annotatedBundle.setProjectVersion(projectVersion);
                                Map bundleEntities = annotatedBundle.getEntities(annotatedEntity.getEntity().getClass());
                                bundleEntities.put(annotatedEntity.getEntityName(), annotatedEntity.getEntity());
                                loadEntityDependencies(annotatedEntity.getPolicyName(), annotatedBundle, bundle);
                                entityBuilders.forEach(builder -> entities.addAll(builder.build(annotatedBundle, bundleType, document)));
                                // Create bundle
                                final Element annotatedElement = bundleDocumentBuilder.build(document, entities);
                                final BundleMetadata bundleMetadata = bundleMetadataBuilder.build(annotatedBundle, annotatedBundle.getAnnotatedEntity(),
                                        entities, projectGroupName, projectVersion);
                                annotatedElements.put(annotatedBundle.getBundleName(), ImmutablePair.of(annotatedElement,
                                        bundleMetadata));
                            }
                        })
        );

        return annotatedElements;
    }

    private void loadEntityDependencies(String policyNameWithPath, AnnotatedBundle annotatedBundle, Bundle bundle) {
        final Map<String, Policy> policyMap = bundle.getPolicies();
        final Policy policyEntity = policyMap.get(policyNameWithPath);
        if (policyEntity != null) {
            populateDependentFolders(annotatedBundle, policyEntity);
            Map<String, Policy> annotatedPolicyMap = annotatedBundle.getEntities(Policy.class);
            annotatedPolicyMap.put(policyNameWithPath, policyEntity);
            Set<Dependency> dependencies = policyEntity.getUsedEntities();
            if (dependencies != null) {
                for (Dependency dependency : dependencies) {
                    Class<? extends GatewayEntity> entityClass = entityTypeRegistry.getEntityClass(dependency.getType());
                    Map<String, ? extends GatewayEntity> entities = bundle.getEntities(entityClass);
                    Optional<? extends Map.Entry<String, ? extends GatewayEntity>> optionalGatewayEntity = entities.entrySet().stream()
                            .filter(e -> {
                                GatewayEntity gatewayEntity = e.getValue();
                                if (gatewayEntity.getName() != null) {
                                    return dependency.getName().equals(gatewayEntity.getName());
                                } else {
                                    return dependency.getName().equals(PathUtils.extractName(e.getKey()));
                                }
                            }).findFirst();
                    Map entityMap = annotatedBundle.getEntities(entityClass);
                    optionalGatewayEntity.ifPresent(entityEntry -> entityMap.put(entityEntry.getKey(), entityEntry.getValue()));
                }
            }
        }
    }

    private void populateDependentFolders(AnnotatedBundle annotatedBundle, GatewayEntity policyEntity) {
        if (policyEntity instanceof Folderable) {
            Folder folder = ((Folderable) policyEntity).getParentFolder();
            Map<String, Folder> folderMap = annotatedBundle.getEntities(Folder.class);
            while (folder != null) {
                folderMap.put(folder.getPath(), folder);
                folder = folder.getParentFolder();
            }
        }
    }

    @VisibleForTesting
    public Set<EntityBuilder> getEntityBuilders() {
        return entityBuilders;
    }
}
