/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BuilderConstants.FILTER_ENV_ENTITIES;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.FOLDER_TYPE;
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
                                loadEntityDependencies(annotatedEntity.getPolicyName(), annotatedBundle, bundle, false);
                                entityBuilders.forEach(builder -> entities.addAll(builder.build(annotatedBundle, bundleType, document)));

                                // Create deployment bundle
                                final Element bundleElement = bundleDocumentBuilder.build(document, entities);

                                // Create DELETE bundle - ALWAYS skip environment entities
                                final Element deleteBundleElement = createDeleteBundle(document, entities, bundle,
                                        annotatedEntity);

                                // Create bundle metadata
                                final BundleMetadata bundleMetadata = bundleMetadataBuilder.build(annotatedBundle,
                                        annotatedEntity, entities, projectGroupName, projectVersion);

                                annotatedElements.put(annotatedBundle.getBundleName(),
                                        new BundleArtifacts(bundleElement, deleteBundleElement, bundleMetadata));
                            }
                        })
        );

        return annotatedElements;
    }

    /**
     * Creates the DELETE bundle element.
     *
     * @param document Document
     * @param entities Entities packaged in the deployment bundle
     * @param bundle Bundle containing all the Gateway entities
     * @param annotatedEntity Annotated Bundle for which bundle is being created.
     * @return Delete bundle Element for the Annotated Bundle
     */
    private Element createDeleteBundle(final Document document, List<Entity> entities, final Bundle bundle,
                                       final AnnotatedEntity<GatewayEntity> annotatedEntity) {
        List<Entity> deleteBundleEntities = new ArrayList<>(entities);

        // If @redeployable annotation is added, we can blindly include all the dependencies in the DELETE bundle.
        // Else, we have to include only non-reusable entities
        if (!annotatedEntity.isRedeployable()) {
            // Include only non-reusable entities
            AnnotatedBundle annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity);
            Map bundleEntities = annotatedBundle.getEntities(annotatedEntity.getEntity().getClass());
            bundleEntities.put(annotatedEntity.getEntityName(), annotatedEntity.getEntity());
            loadEntityDependencies(annotatedEntity.getPolicyName(), annotatedBundle, bundle, true);

            Iterator<Entity> it = deleteBundleEntities.iterator();
            while (it.hasNext()) {
                final Entity entity = it.next();
                final Class<? extends GatewayEntity> entityClass = entityTypeRegistry.getEntityClass(entity.getType());
                Map entityMap = annotatedBundle.getEntities(entityClass);
                if (!entityMap.containsKey(entity.getOriginalName())) {
                    it.remove();
                }
            }
        }

        removeEntitiesForDeleteBundle(deleteBundleEntities); // Filter entities for delete bundle based on type
        deleteBundleEntities.forEach(e -> e.setMappingAction(MappingActions.DELETE)); // Set Mapping Action to DELETE
        return bundleDocumentBuilder.build(document, deleteBundleEntities);
    }

    /**
     * Remove environment entities and folders from the entity list. Environment entities and Folders should not be
     * part of DELETE bundle.
     *
     * @param entities list of entities to be included in the delete bundle
     */
    private void removeEntitiesForDeleteBundle(final List<Entity> entities) {
        // SKIP all environment entities from DELETE bundle
        entities.removeAll(entities.stream().filter(FILTER_ENV_ENTITIES).collect(Collectors.toList()));

        // SKIP all Folders from DELETE bundle
        entities.removeAll(entities.stream().filter(e -> FOLDER_TYPE.equals(e.getType())).collect(Collectors.toList()));
    }

    /**
     * Loads all the gateway entities used in the policy including the environment or global dependencies.
     *
     * @param policyNameWithPath Name of the policy for which gateway dependencies needs to be found.
     * @param annotatedBundle Annotated Bundle for which bundle is being created.
     * @param bundle Bundle containing all the entities of the gateway.
     * @param excludeReusable Exclude loading Reusable entities as the dependencies of the policy
     */
    private void loadEntityDependencies(String policyNameWithPath, AnnotatedBundle annotatedBundle, Bundle bundle,
                                        boolean excludeReusable) {
        final Map<String, Policy> policyMap = bundle.getPolicies();
        final Policy policyEntity = policyMap.get(policyNameWithPath);
        if (policyEntity != null) {
            populateDependentFolders(annotatedBundle, policyEntity);
            Map<String, Policy> annotatedPolicyMap = annotatedBundle.getEntities(Policy.class);
            annotatedPolicyMap.put(policyNameWithPath, policyEntity);
            Set<Dependency> dependencies = policyEntity.getUsedEntities();
            List<GatewayEntity> excludedReusableEntities = new ArrayList<>();
            if (dependencies != null) {
                for (Dependency dependency : dependencies) {
                    Class<? extends GatewayEntity> entityClass = entityTypeRegistry.getEntityClass(dependency.getType());
                    Map<String, ? extends GatewayEntity> allEntitiesOfType = bundle.getEntities(entityClass);
                    Optional<? extends Map.Entry<String, ? extends GatewayEntity>> optionalGatewayEntity =
                            allEntitiesOfType.entrySet().stream()
                            .filter(e -> {
                                GatewayEntity gatewayEntity = e.getValue();
                                if (gatewayEntity.getName() != null) {
                                    return dependency.getName().equals(gatewayEntity.getName());
                                } else {
                                    return dependency.getName().equals(PathUtils.extractName(e.getKey()));
                                }
                            }).findFirst();
                    Map<String, ? extends GatewayEntity> entityMap = annotatedBundle.getEntities(entityClass);
                    optionalGatewayEntity.ifPresent(entry -> insertGatewayEntity(entityMap, entry.getKey(),
                            entry.getValue(), excludeReusable, excludedReusableEntities));
                }
            }
            // Remove policies of excluded Encasses
            excludedReusableEntities.forEach(ex -> {
                Map<String, ? extends GatewayEntity> policyEntities =
                        annotatedBundle.getEntities(entityTypeRegistry.getEntityClass(EntityTypes.POLICY_TYPE));
                if (ex instanceof Encass) {
                    removeExcludedReusablePolicy((Policy) policyEntities.get(((Encass) ex).getPolicy()), annotatedBundle);
                } else if (ex instanceof Policy) {
                    removeExcludedReusablePolicy((Policy) ex, annotatedBundle);
                }
            });
        }
    }

    /**
     * Inserts the Gateway entity into the Entity Map. It skip adding the entity if excludeReusable is TRUE and the
     * Gateway Entity is Reusable
     *
     * @param entityMapToUpdate Entity Map where GatewayEntity needs to be inserted
     * @param key Gateway entity key to store against
     * @param gatewayEntity Gateway entity to be inserted
     * @param excludeReusable Exclude inserting reusable entity
     */
    private void insertGatewayEntity(Map entityMapToUpdate, String key, GatewayEntity gatewayEntity,
                                     boolean excludeReusable, List<GatewayEntity> excludedReusableEntities) {
        if (excludeReusable && gatewayEntity instanceof AnnotableEntity && ((AnnotableEntity) gatewayEntity).isReusable()) {
            excludedReusableEntities.add(gatewayEntity);
            return; // Return without inserting as its reusable entity
        }
        entityMapToUpdate.put(key, gatewayEntity);
    }

    private void removeExcludedReusablePolicy(Policy policy, AnnotatedBundle annotatedBundle) {
        if (policy != null) {
            final Set<Dependency> dependencies = policy.getUsedEntities();
            for (Dependency dependency : dependencies) {
                Class<? extends GatewayEntity> entityClass = entityTypeRegistry.getEntityClass(dependency.getType());
                Map<String, ? extends GatewayEntity> allEntitiesOfType = annotatedBundle.getEntities(entityClass);
                allEntitiesOfType.entrySet().stream()
                        .filter(e -> {
                            GatewayEntity gatewayEntity = e.getValue();
                            if (gatewayEntity.getName() != null) {
                                return dependency.getName().equals(gatewayEntity.getName());
                            } else {
                                return dependency.getName().equals(PathUtils.extractName(e.getKey()));
                            }
                        }).findFirst();
                annotatedBundle.getEntities(entityClass).remove(dependency.getName());
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
