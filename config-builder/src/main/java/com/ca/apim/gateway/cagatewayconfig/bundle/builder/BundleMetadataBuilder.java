/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BuilderConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils.generateDependentEnvBundleFromProject;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.L7_TEMPLATE;
import static com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils.PREFIX_ENVIRONMENT;

@Singleton
public class BundleMetadataBuilder {

    private final IdGenerator idGenerator;

    @Inject
    public BundleMetadataBuilder(final IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * Builds bundle metadata for Annotated bundle or Full bundle.
     *
     * @param annotatedBundle   Annotated Bundle for which bundle is being created.
     * @param dependentEntities Dependent Entities of the annotated bundle or all the entities of full bundle
     * @param projectInfo       Gradle Project info containing Gradle project name, groupName and version
     * @return Full bundle or Annotated bundle metadata
     */
    public BundleMetadata build(final AnnotatedBundle annotatedBundle, final Bundle bundle, final List<Entity> dependentEntities,
                                ProjectInfo projectInfo) {
        if (annotatedBundle != null && annotatedBundle.getAnnotatedEntity() != null) {
            AnnotatedEntity<? extends GatewayEntity> annotatedEntity = annotatedBundle.getAnnotatedEntity();
            final String bundleName = annotatedBundle.getBundleName();
            String name = bundleName;
            if (StringUtils.isNotBlank(projectInfo.getVersion())) {
                name =  bundleName.substring(0, bundleName.indexOf(projectInfo.getVersion()) - 1);
            }

            BundleMetadata.Builder builder = new BundleMetadata.Builder(annotatedEntity.getEntityType(), name, projectInfo.getName(), projectInfo.getGroupName(), projectInfo.getVersion());
            builder.description(annotatedEntity.getDescription());
            final Collection<Metadata> referencedEntities = getEnvironmentDependenciesMetadata(dependentEntities);
            builder.referencedEntities(referencedEntities);
            if (!referencedEntities.isEmpty()) {
                annotatedBundle.getDependentBundles().add(generateDependentEnvBundleFromProject(projectInfo));
            }
            builder.dependencies(annotatedBundle.getDependentBundles());
            builder.tags(annotatedEntity.getTags());
            builder.redeployable(annotatedEntity.isRedeployable() || !isBundleContainsReusableEntity(annotatedBundle));
            boolean l7Template = false;
            switch (annotatedEntity.getEntityType()) {
                case EntityTypes.ENCAPSULATED_ASSERTION_TYPE:
                    l7Template = Boolean.valueOf(String.valueOf(((Encass) annotatedEntity.getEntity()).getProperties().get(L7_TEMPLATE)));
                    break;
                case EntityTypes.SERVICE_TYPE:
                    break;
            }
            builder.l7Template(l7Template);
            builder.hasRouting(hasRoutingAssertion(dependentEntities));

            final List<Metadata> definedEntities = new ArrayList<>();
            definedEntities.add(annotatedEntity.getEntity().getMetadata());

            return builder.definedEntities(definedEntities).build();
        } else {
            return buildFullBundleMetadata(dependentEntities, bundle, projectInfo);
        }
    }

    /**
     * Generates metadata for environment bundle
     * @param entities List of environment entities
     * @param projectInfo project information configured in build.gradle
     * @return BundleMetaData
     */
    public BundleMetadata buildEnvironmentMetadata(final List<Entity> entities, ProjectInfo projectInfo) {
        final boolean isConfigNamePresent = StringUtils.isNotBlank(projectInfo.getConfigName());
        String version = projectInfo.getVersion();
        if (StringUtils.isNotBlank(version) && isConfigNamePresent) {
            version = version + "-" + projectInfo.getConfigName();
        }

        final String name = projectInfo.getName() + "-" + PREFIX_ENVIRONMENT;
        BundleMetadata.Builder builder = new BundleMetadata.Builder(EntityBuilder.BundleType.ENVIRONMENT.name(), name,
                projectInfo.getName(), projectInfo.getGroupName(), version);
        builder.description(StringUtils.EMPTY);
        final List<String> tags = new ArrayList<>();
        if (isConfigNamePresent) {
            tags.add(projectInfo.getConfigName());
        }
        builder.tags(tags);
        builder.redeployable(true);
        builder.hasRouting(false);
        builder.definedEntities(getEnvironmentDependenciesMetadata(entities));

        return builder.build();
    }

    /**
     * Builds bundle metadata for full bundle (all entities)
     *
     * @param entities      All the entities of full bundle
     * @param projectInfo   Gradle Project info containing Gradle project name, groupName and version
     * @return Full bundle metadata
     */
    private BundleMetadata buildFullBundleMetadata(final List<Entity> entities, final Bundle bundle, ProjectInfo projectInfo) {
        BundleMetadata.Builder builder = new BundleMetadata.Builder(BUNDLE_TYPE_ALL, projectInfo.getName(), projectInfo.getName(), projectInfo.getGroupName(), projectInfo.getVersion());
        builder.description(StringUtils.EMPTY);
        builder.tags(Collections.emptyList());
        builder.redeployable(true);
        builder.hasRouting(hasRoutingAssertion(entities));
        final Collection<Metadata> referencedEntities = getEnvironmentDependenciesMetadata(entities);
        builder.referencedEntities(referencedEntities);
        if (!referencedEntities.isEmpty()) {
            bundle.getDependentBundles().add(generateDependentEnvBundleFromProject(projectInfo));
        }
        builder.dependencies(bundle.getDependentBundles());
        builder.definedEntities(getDefinedEntitiesMetadata(entities));

        return builder.build();
    }

    private Collection<Metadata> getEnvironmentDependenciesMetadata(final List<Entity> dependentEntities) {
        return dependentEntities.stream().filter(FILTER_ENV_ENTITIES)
                        .map(Entity::getMetadata).collect(Collectors.toList());
    }

    private Collection<Metadata> getDefinedEntitiesMetadata(final List<Entity> definedEntities) {
        return definedEntities.stream().filter(FILTER_NON_ENV_ENTITIES_EXCLUDING_FOLDER)
                .map(e -> ((GatewayEntity)e.getGatewayEntity()).getMetadata()).collect(Collectors.toList());
    }

    private boolean isBundleContainsReusableEntity (final AnnotatedBundle annotatedBundle) {
        return annotatedBundle.getAnnotatedEntity().isReusable();
    }

    private boolean hasRoutingAssertion(final List<Entity> dependentEntities) {
        return dependentEntities.stream().anyMatch(Entity::isHasRouting);
    }
}
