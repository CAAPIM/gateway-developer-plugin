/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.KeyStoreType;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools.INSTANCE;
import static org.junit.jupiter.api.Assertions.*;

class PrivateKeyEntityBuilderTest {

    @Test
    void buildFromEmptyBundle_noConnections() {
        PrivateKeyEntityBuilder builder = new PrivateKeyEntityBuilder();
        final List<Entity> entities = builder.build(new Bundle(), ENVIRONMENT, INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void build() {
        Bundle bundle = new Bundle();
        bundle.putAllPrivateKeys(ImmutableMap.of("key1", createPrivateKey()));

        PrivateKeyEntityBuilder builder = new PrivateKeyEntityBuilder();
        final List<Entity> entities = builder.build(bundle, ENVIRONMENT, INSTANCE.getDocumentBuilder().newDocument());

        assertNotNull(entities);
        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        assertEquals(EntityTypes.PRIVATE_KEY_TYPE, entity.getType());
        assertEquals(KeyStoreType.PKCS12_SOFTWARE.generateKeyId("key1"), entity.getId());
        assertEquals("key1", entity.getName());
    }

    @NotNull
    private PrivateKey createPrivateKey() {
        PrivateKey privateKey = new PrivateKey();
        privateKey.setAlias("key1");
        privateKey.setKeyStoreType(KeyStoreType.PKCS12_SOFTWARE);
        privateKey.setKeystore(KeyStoreType.PKCS12_SOFTWARE.getName());
        privateKey.setAlgorithm("RSA");
        privateKey.setKeyPassword("");
        return privateKey;
    }


}