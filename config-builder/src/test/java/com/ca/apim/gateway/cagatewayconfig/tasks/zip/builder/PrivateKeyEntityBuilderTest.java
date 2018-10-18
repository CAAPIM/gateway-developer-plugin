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
import com.ca.apim.gateway.cagatewayconfig.util.keystore.KeystoreHelper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools.INSTANCE;
import static org.junit.jupiter.api.Assertions.*;

class PrivateKeyEntityBuilderTest {

    private KeystoreHelper keystoreHelper = new KeystoreHelper();

    @Test
    void buildFromEmptyBundle_noKeys() {
        PrivateKeyEntityBuilder builder = new PrivateKeyEntityBuilder(keystoreHelper);
        final List<Entity> entities = builder.build(new Bundle(), ENVIRONMENT, INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void build() {
        Bundle bundle = new Bundle();
        bundle.putAllPrivateKeys(ImmutableMap.of("test", createPrivateKey()));

        PrivateKeyEntityBuilder builder = new PrivateKeyEntityBuilder(keystoreHelper);
        final List<Entity> entities = builder.build(bundle, ENVIRONMENT, INSTANCE.getDocumentBuilder().newDocument());

        assertNotNull(entities);
        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        assertEquals(EntityTypes.PRIVATE_KEY_TYPE, entity.getType());
        assertEquals(KeyStoreType.GENERIC.generateKeyId("test"), entity.getId());
        assertEquals("test", entity.getName());
    }

    @NotNull
    private PrivateKey createPrivateKey() {
        PrivateKey privateKey = new PrivateKey();
        privateKey.setAlias("test");
        privateKey.setKeyStoreType(KeyStoreType.GENERIC);
        privateKey.setKeystore(KeyStoreType.GENERIC.getName());
        privateKey.setAlgorithm("RSA");
        privateKey.setKeyPassword("");
        privateKey.setPrivateKeyFile(() -> getClass().getClassLoader().getResource("test.p12").openStream());
        return privateKey;
    }


}