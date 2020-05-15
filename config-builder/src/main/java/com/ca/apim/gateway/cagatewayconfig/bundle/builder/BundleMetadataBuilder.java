/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class BundleMetadataBuilder {

    private static final Set<String> NON_ENV_ENTITY_TYPES;

    static {
        NON_ENV_ENTITY_TYPES = new HashSet<>();
        NON_ENV_ENTITY_TYPES.add(EntityTypes.FOLDER_TYPE);
        NON_ENV_ENTITY_TYPES.add(EntityTypes.POLICY_TYPE);
        NON_ENV_ENTITY_TYPES.add(EntityTypes.SERVICE_TYPE);
        NON_ENV_ENTITY_TYPES.add(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
    }

    public BundleMetadata build(final AnnotatedBundle annotatedBundle, final AnnotatedEntity<? extends GatewayEntity> annotatedEntity,
                                final List<Entity> dependentEntities, final String projectGroupName,
                                final String projectVersion) {
        final Encass encass = (Encass) annotatedEntity.getEntity();
        final String bundleName = annotatedBundle.getBundleName();
        final String name = bundleName.substring(0, bundleName.indexOf(projectVersion) - 1);
        final boolean isPolicyOrEncassReusable = annotatedBundle.getEntities(Policy.class).entrySet().stream().anyMatch(entity -> entity.getValue().isReusable()) ||
                annotatedBundle.getEntities(Encass.class).entrySet().stream().anyMatch(entity -> entity.getValue().isReusable());

        BundleMetadata.Builder builder = new BundleMetadata.Builder(encass.getType(), encass.getGuid(), name,
                projectGroupName, projectVersion);
        builder.description(annotatedEntity.getDescription());
        builder.environmentEntities(getEnvironmentDependenciesMetadata(dependentEntities));
        builder.tags(annotatedEntity.getTags());
        builder.reusableAndRedeployable(true, !isPolicyOrEncassReusable || annotatedEntity.isRedeployable());

        final List<Metadata> desiredEntities = new ArrayList<>();
        desiredEntities.add(annotatedEntity.getEntity().getMetadata());

        return builder.definedEntities(desiredEntities).build();
    }

    private Collection<Metadata> getEnvironmentDependenciesMetadata(final List<Entity> dependentEntities) {
        return dependentEntities.stream().filter(e -> !NON_ENV_ENTITY_TYPES.contains(e.getType()))
                        .map(Entity::getMetadata).collect(Collectors.toList());
    }
}
