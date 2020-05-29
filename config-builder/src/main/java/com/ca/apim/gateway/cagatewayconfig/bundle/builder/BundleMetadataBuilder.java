/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

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
    BundleMetadataBuilder (IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * Builds bundle metadata for Annotated bundle or Full bundle.
     *
     * @param annotatedBundle   Annotated Bundle for which bundle is being created.
     * @param dependentEntities Dependent Entities of the annotated bundle or all the entities of full bundle
     * @param projectName       Gradle Project name
     * @param projectGroupName  Gradle Project Group name
     * @param projectVersion    Gradle Project version
     * @return Full bundle or Annotated bundle metadata
     */
    public BundleMetadata build(final AnnotatedBundle annotatedBundle,
                                final List<Entity> dependentEntities, final String projectName,
                                final String projectGroupName, final String projectVersion) {
        if (annotatedBundle != null && annotatedBundle.getAnnotatedEntity() != null) {
            final Encass encass = (Encass) annotatedBundle.getAnnotatedEntity().getEntity();
            final String bundleName = annotatedBundle.getBundleName();
            final String name = bundleName.substring(0, bundleName.indexOf(projectVersion) - 1);

            BundleMetadata.Builder builder = new BundleMetadata.Builder(encass.getType(), encass.getGuid(), name,
                    projectGroupName, projectVersion);
            builder.description(annotatedBundle.getAnnotatedEntity().getDescription());
            builder.environmentEntities(getEnvironmentDependenciesMetadata(dependentEntities));
            builder.tags(annotatedBundle.getAnnotatedEntity().getTags());
            builder.reusableAndRedeployable(true, annotatedBundle.getAnnotatedEntity().isRedeployable() || !isBundleContainsReusableEntity(annotatedBundle));
            builder.hasRouting(hasRoutingAssertion(dependentEntities));

            final List<Metadata> desiredEntities = new ArrayList<>();
            desiredEntities.add(annotatedBundle.getAnnotatedEntity().getEntity().getMetadata());

            return builder.definedEntities(desiredEntities).build();
        } else {
            return buildFullBundleMetadata(dependentEntities, projectName, projectGroupName, projectVersion);
        }
    }

    /**
     * Builds bundle metadata for full bundle (all entities)
     *
     * @param entities          All the entities of full bundle
     * @param projectName       Gradle Project name
     * @param projectGroupName  Gradle Project Group name
     * @param projectVersion    Gradle Project version
     * @return Full bundle metadata
     */
    private BundleMetadata buildFullBundleMetadata(final List<Entity> entities, final String projectName,
                                                   final String projectGroupName, final String projectVersion) {
        BundleMetadata.Builder builder = new BundleMetadata.Builder(BUNDLE_TYPE_ALL, idGenerator.generate(), projectName,
                projectGroupName, projectVersion);
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
