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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

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

    public Map<String, BundleArtifacts> build(Bundle bundle, EntityBuilder.BundleType bundleType,
                                               Document document, String projectName,
                                               String projectGroupName, String projectVersion) {

        Map<String, BundleArtifacts> artifacts = buildAnnotatedEntities(bundleType, bundle, document, projectName,
                projectGroupName, projectVersion);
        if (artifacts.isEmpty()) {
            List<Entity> entities = new ArrayList<>();
            entityBuilders.forEach(builder -> entities.addAll(builder.build(bundle, bundleType, document)));
            final Element fullBundle = bundleDocumentBuilder.build(document, entities);
            artifacts.put(StringUtils.isBlank(projectVersion) ? projectName : projectName + "-" + projectVersion,
                    new BundleArtifacts(fullBundle, null, null));
        }
        return artifacts;
    }

    private Map<String, BundleArtifacts> buildAnnotatedEntities(EntityBuilder.BundleType bundleType, Bundle bundle,
                                                                              Document document, String projectName,
                                                                              String projectGroupName,
                                                                              String projectVersion) {
        final Map<String, BundleArtifacts> annotatedElements = new LinkedHashMap<>();
        Map<String, EntityUtils.GatewayEntityInfo> entityTypeMap = entityTypeRegistry.getEntityTypeMap();
        // Filter the bundle to export only annotated entities
        entityTypeMap.values().stream().filter(EntityUtils.GatewayEntityInfo::isBundleGenerationSupported).forEach(entityInfo ->
                bundle.getEntities(entityInfo.getEntityClass()).values().stream()
                        .filter(entity -> entity instanceof AnnotatableEntity)
                        .map(entity -> ((AnnotatableEntity)entity).getAnnotatedEntity(projectName, projectVersion))
                        .forEach(annotatedEntity -> {
                            if (annotatedEntity != null && annotatedEntity.isBundle()){
                                List<Entity> entities = new ArrayList<>();
                                Map<Class, Map<String, GatewayEntity>> entityMap =
                                        getEntityDependencies(annotatedEntity.getPolicyName(), bundle, false);

                                // Insert the annotated GatewayEntity into the dependent Entities Map.
                                putGatewayEntityByType(entityMap, annotatedEntity.getEntity());

                                entityBuilders.forEach(builder -> entities.addAll(builder.build(entityMap,
                                        annotatedEntity, bundle, bundleType, document)));

                                // Create deployment bundle
                                final Element annotatedBundle = bundleDocumentBuilder.build(document, entities);

                                // Create DELETE bundle - ALWAYS skip environment entities
                                final List<Entity> deleteBundleEntities = new ArrayList<>();
                                if (annotatedEntity.isRedeployable()) {
                                    // If the bundle is marked as @redeployable, blindly include all dependencies in
                                    // delete bundle
                                    deleteBundleEntities.addAll(entities);
                                } else {
                                    // Else, include only non-reusable entities
                                    deleteBundleEntities.addAll(getNonReusableEntities(entities));
                                }
                                final Element annotatedDeleteBundle = bundleDocumentBuilder.buildDeleteBundle(document,
                                        deleteBundleEntities);

                                final BundleMetadata bundleMetadata = bundleMetadataBuilder.build(annotatedEntity,
                                        entities, projectGroupName, projectVersion);
                                annotatedElements.put(annotatedEntity.getBundleName(),
                                        new BundleArtifacts(annotatedBundle, annotatedDeleteBundle, bundleMetadata));
                            }
                        })
        );

        return annotatedElements;
    }

    /**
     * Returns all the gateway entities used in the policy including the environment or global dependencies.
     *
     * @param policyNameWithPath Name of the policy for which gateway dependencies needs to be found.
     * @param bundle Bundle containing all the entities of the gateway.
     * @return Map of Gateway Entity type ({@link Class}) as Key and the Entity as Value.
     */
    private Map<Class, Map<String, GatewayEntity>> getEntityDependencies(String policyNameWithPath, Bundle bundle,
                                                                         boolean excludeReusable) {
        Map<Class, Map<String, GatewayEntity>> entityDependenciesMap = new HashMap<>();
        final Map<String, Policy> entityMap = bundle.getPolicies();
        final Policy policyEntity = entityMap.get(policyNameWithPath);
        if (policyEntity != null) {
            // Add folder tree for the policy.
            populateDependentFolders(entityDependenciesMap, policyEntity);

            // Add entities used in Policy.
            Map<String, GatewayEntity> policyMap = new HashMap<>();
            policyMap.put(policyNameWithPath, policyEntity);
            entityDependenciesMap.put(Policy.class, policyMap);
            Set<Dependency> dependencies = policyEntity.getUsedEntities();
            if (dependencies != null) {
                for (Dependency dependency : dependencies) {
                    Map<String, ? extends GatewayEntity> allEntitiesOfDependencyType = bundle.getEntities(entityClass);
                    // Find the dependency
                    Optional<? extends Map.Entry<String, ? extends GatewayEntity>> optionalGatewayEntity = entities.entrySet().stream()
                            .filter(e-> dependency.getName().equals(PathUtils.extractName(e.getKey()))).findFirst();
                    // Put the found entity
                    optionalGatewayEntity.ifPresent(e -> putGatewayEntityByType(entityDependenciesMap, e));
                }
            }
        }
        return entityDependenciesMap;
    }

    /**
     * Inserts the Gateway entity into the Entity Map based on the Gateway entity type. If the Gateway entity type
     * doesn't exist in the Entity Map, initializes the Entity Map with the type before inserting the Gateway entity.
     *
     * @param entityMapToUpdate Entity Map where GatewayEntity needs to be inserted
     * @param gatewayEntity Gateway entity to be inserted
     */
    public void putGatewayEntityByType(Map<Class, Map<String, GatewayEntity>> entityMapToUpdate,
                                       GatewayEntity gatewayEntity) {
        entityMapToUpdate.computeIfAbsent(gatewayEntity.getClass(), klass -> new HashMap<>())
                .put(gatewayEntity.getName(), gatewayEntity);
    }

    private void populateDependentFolders(Map<Class, Map<String, GatewayEntity>> entityDependenciesMap, GatewayEntity policyEntity) {
        if (policyEntity instanceof Folderable) {
            Folder folder = ((Folderable) policyEntity).getParentFolder();
            Map<String, GatewayEntity> folderMap = new HashMap<>();
            while (folder != null) {
                folderMap.put(folder.getPath(), folder);
                folder = folder.getParentFolder();
            }
            entityDependenciesMap.put(Folder.class, folderMap);
        }
    }

    private List<Entity> getNonReusableEntities(final List<Entity> entities) {
        return entities.stream().collect(Collectors.toList());
    }

    @VisibleForTesting
    public Set<EntityBuilder> getEntityBuilders() {
        return entityBuilders;
    }
}
