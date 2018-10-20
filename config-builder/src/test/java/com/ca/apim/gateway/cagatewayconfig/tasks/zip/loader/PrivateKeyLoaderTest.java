/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.KeyStoreType;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonToolsException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Extensions({@ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class)})
class PrivateKeyLoaderTest {

    private TemporaryFolder rootProjectDir;
    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;
    private File keysDir;

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        rootProjectDir = temporaryFolder;
        jsonTools = new JsonTools(fileUtils);

        keysDir = new File(temporaryFolder.getRoot(), "config/privateKeys");
        keysDir.mkdirs();
    }

    @Test
    void loadPrivateKeysYaml() throws IOException {
        String yaml = "key1:\n" +
                "  keystore: \"Software DB\"\n" +
                "  algorithm: \"RSA\"\n" +
                "  keyPassword: \"\"\n" +
                "test:\n" +
                "  keystore: \"Software DB\"\n" +
                "  algorithm: \"EC\"\n" +
                "  keyPassword: \"\"\n" +
                "key2:\n" +
                "  keystore: \"Software DB\"\n" +
                "  algorithm: \"RSA\"\n" +
                "  keyPassword: \"\"\n" +
                "\n";
        createPrivateKeys("key1", "test", "key2");
        loadPrivateKeys(yaml, "yml", false);
    }

    @Test
    void loadPrivateKeysJson() throws IOException {
        String json = "{\n" +
                "  \"key1\": {\n" +
                "    \"keystore\": \"Software DB\",\n" +
                "    \"algorithm\": \"RSA\",\n" +
                "    \"keyPassword\": \"\"\n" +
                "  },\n" +
                "  \"test\": {\n" +
                "    \"keystore\": \"Software DB\",\n" +
                "    \"algorithm\": \"EC\",\n" +
                "    \"keyPassword\": \"\"\n" +
                "  },\n" +
                "  \"key2\": {\n" +
                "    \"keystore\": \"Software DB\",\n" +
                "    \"algorithm\": \"RSA\",\n" +
                "    \"keyPassword\": \"\"\n" +
                "  }\n" +
                "}";
        createPrivateKeys("key1", "test", "key2");
        loadPrivateKeys(json, "json", false);
    }

    private void createPrivateKeys(String... keys) throws IOException {
        for (String k : keys) {
            Files.touch(new File(keysDir, k + ".p12"));
        }
    }

    @Test
    void loadPrivateKeysMalformedYaml() throws IOException {
        String yaml = "key1:\n" +
                "  keystore: Software DB\n" +
                "  algorithm: \"RSA\"\n" +
                "  keyPassword: \"\"\n" +
                "test:\n" +
                "  keystore: \"Software DB\"\n" +
                "  algorithm: \"EC\"\n" +
                "  keyPassword: \"\"\n" +
                "key2:\n" +
                "  keystore: \"Software DB\"\n" +
                "  algorithm \"RSA\"\n" +
                "  keyPassword: \"\"\n" +
                "\n";
        loadPrivateKeys(yaml, "yml", true);
    }

    @Test
    void loadPrivateKeysMalformedJson() throws IOException {
        String json = "{\n" +
                "  \"key1\": {\n" +
                "    \"keystore\": Software DB,\n" +
                "    \"algorithm\": \"RSA\",\n" +
                "    \"keyPassword\": \"\"\n" +
                "  },\n" +
                "  \"test\": {\n" +
                "    \"keystore\": \"Software DB\",\n" +
                "    \"algorithm\": \"EC\",\n" +
                "    \"keyPassword\": \"\"\n" +
                "  },\n" +
                "  \"key2\": {\n" +
                "    \"keystore\": \"Software DB\",\n" +
                "    \"algorithm\": \"RSA\",\n" +
                "    \"keyPassword\": \"\"\n" +
                "  }\n" +
                "}";
        loadPrivateKeys(json, "json", true);
    }

    private void loadPrivateKeys(String content, String fileTyoe, boolean expectException) throws IOException {
        PrivateKeyLoader loader = new PrivateKeyLoader(jsonTools);
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "private-keys." + fileTyoe);
        Files.touch(identityProvidersFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        if (expectException) {
            assertThrows(JsonToolsException.class, () -> loader.load(bundle, rootProjectDir.getRoot()));
            return;
        }
        loader.load(bundle, rootProjectDir.getRoot());

        assertNotNull(bundle.getPrivateKeys());
        assertFalse(bundle.getPrivateKeys().isEmpty());
        assertEquals(3, bundle.getPrivateKeys().size());

        PrivateKey key1 = bundle.getPrivateKeys().get("key1");
        assertNotNull(key1);
        assertEquals("key1", key1.getAlias());
        assertEquals("RSA", key1.getAlgorithm());
        assertEquals("", key1.getKeyPassword());
        assertEquals(KeyStoreType.PKCS12_SOFTWARE.getName(), key1.getKeystore());
        assertNotNull(key1.getKeyStoreType());
        assertEquals(KeyStoreType.PKCS12_SOFTWARE, key1.getKeyStoreType());
    }
}