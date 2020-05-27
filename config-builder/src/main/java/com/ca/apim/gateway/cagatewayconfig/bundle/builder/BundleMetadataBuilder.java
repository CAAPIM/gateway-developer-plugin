/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
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
    BundleMetadataBuilder (IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * Builds bundle metadata for annotated entities.
     *
     * @param annotatedBundle
     * @param annotatedEntity
     * @param dependentEntities
     * @param projectName
     * @param projectGroupName
     * @param projectVersion
     * @return bundle metadata
     */
    public BundleMetadata build(final AnnotatedBundle annotatedBundle,
                                final AnnotatedEntity<? extends GatewayEntity> annotatedEntity,
                                final List<Entity> dependentEntities, final String projectName,
                                final String projectGroupName, final String projectVersion) {
        if (annotatedBundle != null && annotatedEntity != null) {
            final Encass encass = (Encass) annotatedEntity.getEntity();
            final String bundleName = annotatedBundle.getBundleName();
            final String name = bundleName.substring(0, bundleName.indexOf(projectVersion) - 1);

            BundleMetadata.Builder builder = new BundleMetadata.Builder(encass.getType(), encass.getGuid(), name,
                    projectGroupName, projectVersion);
            builder.description(annotatedEntity.getDescription());
            builder.environmentEntities(getEnvironmentDependenciesMetadata(dependentEntities));
            builder.tags(annotatedEntity.getTags());
            builder.reusableAndRedeployable(true, annotatedEntity.isRedeployable() || !isBundleContainsReusableEntity(annotatedBundle));

            final List<Metadata> desiredEntities = new ArrayList<>();
            desiredEntities.add(annotatedEntity.getEntity().getMetadata());

            return builder.definedEntities(desiredEntities).build();
        } else {
            return buildFullBundleMetadata(dependentEntities, projectName, projectGroupName, projectVersion);
        }
    }

    /**
     * Builds bundle metadata for full bundle (all entities)
     *
     * @param entities
     * @param projectName
     * @param projectGroupName
     * @param projectVersion
     * @return bundle metadata
     */
    private BundleMetadata buildFullBundleMetadata (final List<Entity> entities, final String projectName,
                                                    final String projectGroupName, final String projectVersion) {
        BundleMetadata.Builder builder = new BundleMetadata.Builder(BUNDLE_TYPE_ALL, idGenerator.generate(), projectName,
                projectGroupName, projectVersion);
        builder.description(StringUtils.EMPTY);
        builder.tags(Collections.EMPTY_LIST);
        builder.reusableAndRedeployable(true, true);
        builder.environmentIncluded(true);
        builder.environmentEntities(getEnvironmentDependenciesMetadata(entities));
        builder.definedEntities(getDefinedEntitiesMetadata(entities));

        return builder.build();
    }

    private Collection<Metadata> getEnvironmentDependenciesMetadata(final List<Entity> dependentEntities) {
        return dependentEntities.stream().filter(FILTER_ENV_ENTITIES)
                        .map(Entity::getMetadata).collect(Collectors.toList());
    }

    private Collection<Metadata> getDefinedEntitiesMetadata(final List<Entity> definedEntities) {
        return definedEntities.stream().filter(FILTER_METADATA_NON_ENV_ENTITIES)
                .map(Entity::getMetadata).collect(Collectors.toList());
    }

    private boolean isBundleContainsReusableEntity (final AnnotatedBundle annotatedBundle) {
        return annotatedBundle.getEntities(Policy.class).entrySet().stream().anyMatch(entity -> entity.getValue().isReusable()) ||
                annotatedBundle.getEntities(Encass.class).entrySet().stream().anyMatch(entity -> entity.getValue().isReusable());
    }
}
