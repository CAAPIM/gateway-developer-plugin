/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType;
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

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BuilderConstants.FILTER_ENV_ENTITIES;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.FOLDER_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.DELETE_BUNDLE_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.INSTALL_BUNDLE_EXTENSION;
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

    public Map<String, BundleArtifacts> build(Bundle bundle, BundleType bundleType,
                                              Document document, ProjectInfo projectInfo) {

        Map<String, BundleArtifacts> artifacts = buildAnnotatedEntities(bundleType, bundle, document, projectInfo);
        if (artifacts.isEmpty()) {
            List<Entity> entities = new ArrayList<>();
            entityBuilders.forEach(builder -> entities.addAll(builder.build(bundle, bundleType, document)));
            final Element fullBundle = bundleDocumentBuilder.build(document, entities);
            final String bundleName = StringUtils.isBlank(projectInfo.getVersion()) ? projectInfo.getName() :
                    projectInfo.getName() + "-" + projectInfo.getVersion();
            final String bundleFileName = generateBundleFileName(bundleType, true, false, bundleName);
            final String deleteBundleFileName = generateBundleFileName(bundleType, true, true, bundleName);
            artifacts.put(bundleName, new BundleArtifacts(fullBundle, null, null, bundleFileName,
                    deleteBundleFileName));
        }
        return artifacts;
    }

    private Map<String, BundleArtifacts> buildAnnotatedEntities(BundleType bundleType, Bundle bundle,
                                                                Document document, ProjectInfo projectInfo) {
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
                                AnnotatedBundle annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity, projectInfo);
                                Map bundleEntities = annotatedBundle.getEntities(annotatedEntity.getEntity().getClass());
                                bundleEntities.put(annotatedEntity.getEntityName(), annotatedEntity.getEntity());
                                loadPolicyDependenciesByPolicyName(annotatedEntity.getPolicyName(), annotatedBundle, bundle, false);
                                entityBuilders.forEach(builder -> entities.addAll(builder.build(annotatedBundle, bundleType, document)));

                                // Create deployment bundle
                                final Element bundleElement = bundleDocumentBuilder.build(document, entities);

                                // Create DELETE bundle - ALWAYS skip environment entities
                                final Element deleteBundleElement = createDeleteBundle(document, entities, bundle,
                                        annotatedEntity, projectInfo);

                                // Create bundle metadata
                                final BundleMetadata bundleMetadata = bundleMetadataBuilder.build(annotatedBundle,
                                        annotatedEntity, entities, projectInfo);

                                final String bundleFileName = generateBundleFileName(bundleType, false, false,
                                        annotatedBundle.getBundleName());
                                final String deleteBundleFileName = generateBundleFileName(bundleType, false, true,
                                        annotatedBundle.getBundleName());
                                annotatedElements.put(annotatedBundle.getBundleName(),
                                        new BundleArtifacts(bundleElement, deleteBundleElement, bundleMetadata,
                                                bundleFileName, deleteBundleFileName));
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
                                       final AnnotatedEntity<GatewayEntity> annotatedEntity, ProjectInfo projectInfo) {
        List<Entity> deleteBundleEntities = copyFilteredEntitiesForDeleteBundle(entities);

        // If @redeployable annotation is added, we can blindly include all the dependencies in the DELETE bundle.
        // Else, we have to include only non-reusable entities
        if (!annotatedEntity.isRedeployable()) {
            // Include only non-reusable entities
            AnnotatedBundle annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity, projectInfo);
            Map bundleEntities = annotatedBundle.getEntities(annotatedEntity.getEntity().getClass());
            bundleEntities.put(annotatedEntity.getEntityName(), annotatedEntity.getEntity());
            loadPolicyDependenciesByPolicyName(annotatedEntity.getPolicyName(), annotatedBundle, bundle, true);

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

        deleteBundleEntities.forEach(e -> e.setMappingAction(MappingActions.DELETE)); // Set Mapping Action to DELETE
        return bundleDocumentBuilder.build(document, deleteBundleEntities);
    }

    /**
     * Copies all the filtered entities in the reverse order to a new {@link List}. The entries in the DELETE bundle
     * must be in the reverse order of deployment bundle.
     *
     * All Environment entities and Folders are skipped from the DELETE bundle list.
     *
     * @param entities Entities in the deployment bundle
     * @return Filtered list of entities in the reverse order
     */
    private List<Entity> copyFilteredEntitiesForDeleteBundle(List<Entity> entities) {
        List<Entity> deleteBundleEntities = new ArrayList<>();
        for (int i = entities.size() - 1; i >= 0; i--) { // Copy in reverse order
            final Entity entity = entities.get(i);
            // SKIP all Environment entities and Folders from the DELETE bundle
            if (!FILTER_ENV_ENTITIES.test(entity) && !FOLDER_TYPE.equals(entity.getType())) {
                deleteBundleEntities.add(entity);
            }
        }
        return deleteBundleEntities;
    }

    /**
     * Loads all the gateway entities used in the policy including the environment or global dependencies.
     *
     * @param policyNameWithPath Name of the policy for which gateway dependencies needs to be found.
     * @param annotatedBundle Annotated Bundle for which bundle is being created.
     * @param rawBundle Bundle containing all the entities of the gateway.
     * @param excludeReusable Exclude loading Reusable entities as the dependencies of the policy
     */
    private void loadPolicyDependenciesByPolicyName(String policyNameWithPath, AnnotatedBundle annotatedBundle,
                                                    Bundle rawBundle, boolean excludeReusable) {
        final Policy policy = findPolicyByNameOrPath(policyNameWithPath, rawBundle);
        loadPolicyDependencies(policy, annotatedBundle, rawBundle, excludeReusable);
    }

    /**
     * Loads the Policy and its dependencies
     *
     * @param policy Policy for which gateway dependencies needs to be loaded.
     * @param annotatedBundle Annotated Bundle for which bundle is being created.
     * @param rawBundle Bundle containing all the entities of the gateway.
     * @param excludeReusable Exclude loading Reusable entities as the dependencies of the policy
     */
    private void loadPolicyDependencies(Policy policy, AnnotatedBundle annotatedBundle, Bundle rawBundle,
                                                    boolean excludeReusable) {
        if (policy == null || excludeGatewayEntity(Policy.class, policy, annotatedBundle, excludeReusable)) {
            return;
        }

        loadFolderDependencies(annotatedBundle, policy);

        Map<String, Policy> annotatedPolicyMap = annotatedBundle.getEntities(Policy.class);
        annotatedPolicyMap.put(policy.getPath(), policy);

        Set<Dependency> dependencies = policy.getUsedEntities();
        if (dependencies != null) {
            for (Dependency dependency : dependencies) {
                switch (dependency.getType()) {
                    case EntityTypes.POLICY_TYPE:
                        Policy dependentPolicy = findPolicyByNameOrPath(dependency.getName(), rawBundle);
                        loadPolicyDependencies(dependentPolicy, annotatedBundle, rawBundle, excludeReusable);
                        break;
                    case EntityTypes.ENCAPSULATED_ASSERTION_TYPE:
                        Encass encass = rawBundle.getEncasses().get(dependency.getName());
                        loadEncassDependencies(encass, annotatedBundle, rawBundle, excludeReusable);
                        break;
                    default:
                        loadGatewayEntity(dependency, annotatedBundle, rawBundle);
                }
            }
        }
    }

    /**
     * Loads the Encass and its dependencies
     *
     * @param encass Encass policy for which gateway dependencies needs to be loaded.
     * @param annotatedBundle Annotated Bundle for which bundle is being created.
     * @param rawBundle Bundle containing all the entities of the gateway.
     * @param excludeReusable Exclude loading Reusable entities as the dependencies of the policy
     */
    private void loadEncassDependencies(Encass encass, AnnotatedBundle annotatedBundle, Bundle rawBundle,
                                        boolean excludeReusable) {
        if (encass != null && !excludeGatewayEntity(Encass.class, encass, annotatedBundle, excludeReusable)) {
            annotatedBundle.getEncasses().put(encass.getName(), encass);
            loadPolicyDependenciesByPolicyName(encass.getPolicy(), annotatedBundle, rawBundle, excludeReusable);
        }
    }

    /**
     * Loads the Folders.
     *
     * @param annotatedBundle Annotated Bundle for which bundle is being created.
     * @param policyEntity Policy for which folder dependencies needs to be loaded.
     */
    private void loadFolderDependencies(AnnotatedBundle annotatedBundle, GatewayEntity policyEntity) {
        if (policyEntity instanceof Folderable) {
            Folder folder = ((Folderable) policyEntity).getParentFolder();
            Map<String, Folder> folderMap = annotatedBundle.getEntities(Folder.class);
            while (folder != null) {
                folderMap.putIfAbsent(folder.getPath(), folder);
                folder = folder.getParentFolder();
            }
        }
    }

    /**
     * Loads the Gateway entities other than Policy and Encass.
     *
     * @param dependency Dependency to be loaded
     * @param annotatedBundle Annotated Bundle for which bundle is being created.
     * @param rawBundle Bundle containing all the entities of the gateway.
     */
    private void loadGatewayEntity(Dependency dependency, AnnotatedBundle annotatedBundle, Bundle rawBundle) {
        Class<? extends GatewayEntity> entityClass = entityTypeRegistry.getEntityClass(dependency.getType());
        Map<String, ? extends GatewayEntity> allEntitiesOfType = rawBundle.getEntities(entityClass);
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
        Map entityMap = annotatedBundle.getEntities(entityClass);
        optionalGatewayEntity.ifPresent(e -> entityMap.put(e.getKey(), e.getValue()));
    }

    /**
     * Return TRUE is the Gateway entity needs to be excluded from being loaded.
     *
     * @param entityType Type of entity class
     * @param gatewayEntity Gateway entity to be checked
     * @param annotatedBundle Annotated Bundle for which bundle is being created.
     * @param excludeReusable Exclude loading Reusable entities as the dependency
     * @return TRUE if the Gateway entity needs to be excluded
     */
    private boolean excludeGatewayEntity(Class<? extends GatewayEntity> entityType, GatewayEntity gatewayEntity,
                                         AnnotatedBundle annotatedBundle, boolean excludeReusable) {
        return annotatedBundle.getEntities(entityType).containsKey(gatewayEntity.getName())
                || excludeReusableOrPolicyEntity(gatewayEntity, annotatedBundle, excludeReusable);
    }

    /**
     * Returns TRUE if the Gateway entity is annotated as @reusable and the reusable entity needs to excluded or the
     * gateway entity is a policy entity and the annotated bundle already contains that policy.
     *
     * @param gatewayEntity Gateway entity to be checked
     * @param annotatedBundle Annotated Bundle for which bundle is being created.
     * @param excludeReusable Exclude loading Reusable entities as the dependency
     * @return TRUE if the Gateway entity is @reusable and needs to be excluded or entity is Policy and annotated
     * bundle already contains the policy
     */
    private boolean excludeReusableOrPolicyEntity(GatewayEntity gatewayEntity, AnnotatedBundle annotatedBundle,
                                                  boolean excludeReusable) {
        if (gatewayEntity instanceof AnnotableEntity && ((AnnotableEntity) gatewayEntity).isReusable() && excludeReusable) {
            return true;
        }
        // Special case for policy because policies are stored by Path in the entities map and
        // GatewayEntity.getName() only gives Policy name.
        return gatewayEntity instanceof Policy && findPolicyByNameOrPath(gatewayEntity.getName(), annotatedBundle) != null;
    }

    /**
     * Finds policy in the Raw bundle by just Policy name or Policy path.
     *
     * @param policyNameOrPath Policy name or path
     * @param rawBundle Bundle containing all the entities of the gateway.
     * @return Found Policy is exists, returns NULL if not found
     */
    private Policy findPolicyByNameOrPath(String policyNameOrPath, Bundle rawBundle) {
        for (String policyKey : rawBundle.getPolicies().keySet()) {
            if (StringUtils.equalsAny(policyNameOrPath, PathUtils.extractName(policyKey), policyKey)) {
                return rawBundle.getPolicies().get(policyKey);
            }
        }
        return null;
    }

    private String generateBundleFileName(BundleType bundleType, boolean isFullBundle,
                                          boolean isDeleteBundle, String bundleName) {
        if (bundleType == BundleType.ENVIRONMENT) {
            return bundleName;
        }
        String filenameSuffix = isDeleteBundle ? DELETE_BUNDLE_EXTENSION : INSTALL_BUNDLE_EXTENSION;
        if (isFullBundle) {
            return bundleName + "-full" + filenameSuffix;
        } else {
            return bundleName + "-policy" + filenameSuffix;
        }
    }

    @VisibleForTesting
    public Set<EntityBuilder> getEntityBuilders() {
        return entityBuilders;
    }
}
