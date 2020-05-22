/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.BuilderConstants.FILTER_ENV_ENTITIES;

@Singleton
public class BundleMetadataBuilder {

    public BundleMetadata build(final AnnotatedBundle annotatedBundle,
                                final AnnotatedEntity<? extends GatewayEntity> annotatedEntity,
                                final List<Entity> dependentEntities, final String projectGroupName,
                                final String projectVersion) {
        final Encass encass = (Encass) annotatedEntity.getEntity();
        final String bundleName = annotatedBundle.getBundleName();
        final String name = bundleName.substring(0, bundleName.indexOf(projectVersion) - 1);

        BundleMetadata.Builder builder = new BundleMetadata.Builder(encass.getType(), encass.getGuid(), name,
                projectGroupName, projectVersion);
        builder.description(annotatedEntity.getDescription());
        builder.environmentEntities(getEnvironmentDependenciesMetadata(dependentEntities));
        builder.tags(annotatedEntity.getTags());
        builder.reusableAndRedeployable(true, annotatedEntity.isRedeployable() || !isBundleContainsReusableEntity(annotatedBundle));
        builder.hasRouting(hasRoutingAssertion(dependentEntities));

        final List<Metadata> desiredEntities = new ArrayList<>();
        desiredEntities.add(annotatedEntity.getEntity().getMetadata());

        return builder.definedEntities(desiredEntities).build();
    }

    private Collection<Metadata> getEnvironmentDependenciesMetadata(final List<Entity> dependentEntities) {
        return dependentEntities.stream().filter(FILTER_ENV_ENTITIES)
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
