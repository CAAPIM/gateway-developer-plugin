/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BuilderConstants.*;

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
    public BundleMetadata build(final AnnotatedBundle annotatedBundle, final List<Entity> dependentEntities,
                                ProjectInfo projectInfo) {
        if (annotatedBundle != null && annotatedBundle.getAnnotatedEntity() != null) {
            AnnotatedEntity annotatedEntity = annotatedBundle.getAnnotatedEntity();
            final Encass encass = (Encass) annotatedEntity.getEntity();
            final String bundleName = annotatedBundle.getBundleName();
            final String name = bundleName.substring(0, bundleName.indexOf(projectInfo.getVersion()) - 1);
            final String metadataId = StringUtils.isBlank(annotatedEntity.getMetadataId()) ? idGenerator.generate() :
                    annotatedEntity.getMetadataId();

            BundleMetadata.Builder builder = new BundleMetadata.Builder(encass.getType(), metadataId, name,
                    projectInfo.getGroupName(), projectInfo.getVersion());
            builder.description(annotatedEntity.getDescription());
            builder.environmentEntities(getEnvironmentDependenciesMetadata(dependentEntities));
            builder.tags(annotatedEntity.getTags());
            builder.reusableAndRedeployable(true, annotatedEntity.isRedeployable() || !isBundleContainsReusableEntity(annotatedBundle));
            builder.hasRouting(hasRoutingAssertion(dependentEntities));

            final List<Metadata> definedEntities = new ArrayList<>();
            definedEntities.add(annotatedEntity.getEntity().getMetadata());

            return builder.definedEntities(definedEntities).build();
        } else {
            return buildFullBundleMetadata(dependentEntities, projectInfo);
        }
    }

    /**
     * Builds bundle metadata for full bundle (all entities)
     *
     * @param entities      All the entities of full bundle
     * @param projectInfo   Gradle Project info containing Gradle project name, groupName and version
     * @return Full bundle metadata
     */
    private BundleMetadata buildFullBundleMetadata(final List<Entity> entities, ProjectInfo projectInfo) {
        BundleMetadata.Builder builder = new BundleMetadata.Builder(BUNDLE_TYPE_ALL, idGenerator.generate(),
                projectInfo.getName(), projectInfo.getGroupName(), projectInfo.getVersion());
        builder.description(StringUtils.EMPTY);
        builder.tags(Collections.emptyList());
        builder.reusableAndRedeployable(true, true);
        builder.hasRouting(hasRoutingAssertion(entities));
        builder.environmentEntities(getEnvironmentDependenciesMetadata(entities));
        builder.definedEntities(getDefinedEntitiesMetadata(entities));

        return builder.build();
    }

    private Collection<Metadata> getEnvironmentDependenciesMetadata(final List<Entity> dependentEntities) {
        return dependentEntities.stream().filter(FILTER_ENV_ENTITIES)
                        .map(Entity::getMetadata).collect(Collectors.toList());
    }

    private Collection<Metadata> getDefinedEntitiesMetadata(final List<Entity> definedEntities) {
        return definedEntities.stream().filter(FILTER_NON_ENV_ENTITIES_EXCLUDING_FOLDER)
                .map(Entity::getMetadata).collect(Collectors.toList());
    }

    private boolean isBundleContainsReusableEntity (final AnnotatedBundle annotatedBundle) {
        return annotatedBundle.getEntities(Policy.class).entrySet().stream().anyMatch(entity -> entity.getValue().isReusable()) ||
                annotatedBundle.getEntities(Encass.class).entrySet().stream().anyMatch(entity -> entity.getValue().isReusable());
    }

    private boolean hasRoutingAssertion(final List<Entity> dependentEntities) {
        return dependentEntities.stream().anyMatch(Entity::isHasRouting);
    }
}
