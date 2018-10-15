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
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools.INSTANCE;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.junit.jupiter.api.Assertions.*;

@Extensions(@ExtendWith(TemporaryFolderExtension.class))
class PrivateKeyEntityBuilderTest {

    private File privateKeysDir;
    private KeystoreHelper keystoreHelper = new KeystoreHelper();

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        this.privateKeysDir = new File(temporaryFolder.getRoot(), "privateKeys");
    }

    @Test
    void buildFromEmptyBundle_noKeys() {
        PrivateKeyEntityBuilder builder = new PrivateKeyEntityBuilder(keystoreHelper);
        final List<Entity> entities = builder.build(new Bundle(), ENVIRONMENT, INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void build() throws IOException {
        Bundle bundle = new Bundle();
        bundle.setPrivateKeysDirectory(privateKeysDir.getPath());
        bundle.putAllPrivateKeys(ImmutableMap.of("key1", createPrivateKey()));

        PrivateKeyEntityBuilder builder = new PrivateKeyEntityBuilder(keystoreHelper);
        final List<Entity> entities = builder.build(bundle, ENVIRONMENT, INSTANCE.getDocumentBuilder().newDocument());

        assertNotNull(entities);
        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        assertEquals(EntityTypes.PRIVATE_KEY_TYPE, entity.getType());
        assertEquals(KeyStoreType.GENERIC.generateKeyId("key1"), entity.getId());
        assertEquals("key1", entity.getName());
    }

    @NotNull
    private PrivateKey createPrivateKey() throws IOException {
        PrivateKey privateKey = new PrivateKey();
        privateKey.setAlias("test");
        privateKey.setKeyStoreType(KeyStoreType.GENERIC);
        privateKey.setKeystore(KeyStoreType.GENERIC.getName());
        privateKey.setAlgorithm("RSA");
        privateKey.setKeyPassword("");

        writeByteArrayToFile(new File(this.privateKeysDir, "test.p12"), toByteArray(getClass().getClassLoader().getResource("test.p12")));
        return privateKey;
    }


}