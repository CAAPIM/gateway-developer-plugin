/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderHelper.getEntityWithOnlyMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.PRIVATE_KEY_TYPE;

@Singleton
public class PrivateKeyEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1200;

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        return bundle.getPrivateKeys().entrySet().stream()
                .map(e -> getEntityWithOnlyMapping(PRIVATE_KEY_TYPE, e.getKey(), e.getValue().getKeyStoreType().generateKeyId(e.getKey())))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
