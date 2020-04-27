/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.AnnotatedEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class BundleMetadataBuilder {

    public BundleMetadata build(final String bundleVersion, final GatewayEntity entity,
                                final AnnotatedEntity annotatedEntity) {
        final Encass encass = (Encass) entity;
        final String name = annotatedEntity.getBundleName().substring(0,
                annotatedEntity.getBundleName().indexOf(bundleVersion) - 1);

        BundleMetadata.Builder builder = new BundleMetadata.Builder("encass", encass.getGuid(), name,
                bundleVersion);
        builder.description(annotatedEntity.getDescription());
        builder.tags(annotatedEntity.getTags());
        builder.reusableAndRedeployable(annotatedEntity.isReusableTypeEnabled(),
                annotatedEntity.isRedeployableTypeEnabled());
        final List<Metadata> desiredEntities = new ArrayList<>();
        desiredEntities.add(entity.getMetadata());

        return builder.definedEntities(desiredEntities).build();
    }
}
