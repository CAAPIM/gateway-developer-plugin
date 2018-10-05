/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.PRIVATE_KEY_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributes;
import static java.util.stream.Collectors.toList;

@Singleton
public class PrivateKeyEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1200;

    @Override
    public List<Entity> build(Bundle bundle, Document document) {
        return bundle.getPrivateKeys().entrySet().stream().map(e -> buildPrivateKeyEntity(e.getKey(), e.getValue(), document)).collect(toList());
    }

    private Entity buildPrivateKeyEntity(String alias, PrivateKey privateKey, Document document) {
        final String id = privateKey.getKeyStoreType().generateKeyId(alias);
        final Element privateKeyElem = createElementWithAttributes(
                document,
                PRIVATE_KEY,
                ImmutableMap.of(
                        ATTRIBUTE_ID, id,
                        ATTRIBUTE_KEYSTORE_ID, privateKey.getKeyStoreType().getId(),
                        ATTRIBUTE_ALIAS, alias)
        );
        buildAndAppendPropertiesElement(ImmutableMap.of("keyAlgorithm", privateKey.getAlgorithm()), document, privateKeyElem);

        return new Entity(PRIVATE_KEY_TYPE, alias, id, privateKeyElem);
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
