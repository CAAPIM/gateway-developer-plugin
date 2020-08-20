/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.KeyStoreType;
import com.ca.apim.gateway.cagatewayconfig.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonToolsException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.createEntityLoader;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Extensions({@ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class)})
class PrivateKeyLoaderTest {

    private TemporaryFolder rootProjectDir;
    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;
    private File config;
    private byte[] testPrivateKeyContent;

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) throws IOException {
        rootProjectDir = temporaryFolder;
        jsonTools = new JsonTools(fileUtils);

        config = temporaryFolder.createDirectory("config");
        testPrivateKeyContent = IOUtils.toByteArray(new FileInputStream(new File(
                        Objects.requireNonNull(getClass().getClassLoader().getResource("test.p12")).getFile())));
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
        createPrivateKeys(new Object[]{"key1"}, new Object[]{"test"}, new Object[]{"key2"});
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
        createPrivateKeys(new Object[]{"key1"}, new Object[]{"test"}, new Object[]{"key2"});
        loadPrivateKeys(json, "json", false);
    }

    @Test
    void loadSingle() throws IOException {
        PrivateKeyLoader loader = new PrivateKeyLoader();
        createPrivateKeys(new Object[]{"key1", testPrivateKeyContent});
        Object certContent = loader.loadSingle("key1", new File(new File(config, "privateKeys"), "key1.p12"));

        assertNotNull(certContent);
        assertTrue(certContent instanceof String);
        assertTrue(Base64.isBase64(certContent.toString()));
    }

    @Test
    void loadSingleInvalidExtension() throws IOException {
        PrivateKeyLoader loader = new PrivateKeyLoader();
        createPrivateKeys(new Object[]{"key1", testPrivateKeyContent});
        assertThrows(ConfigLoadException.class, () -> loader.loadSingle("key1", new File(new File(config,
                "privateKeys"), "key1.pfx")));
    }

    @Test
    void loadNoCerts() {
        CertificatesLoader loader = new CertificatesLoader(FileUtils.INSTANCE);
        Bundle bundle = new Bundle();
        loader.load(bundle, rootProjectDir.getRoot());

        assertTrue(bundle.getPrivateKeyFiles().isEmpty());
    }

    private void createPrivateKeys(Object[]... keys) throws IOException {
        File keysDir = new File(config, "privateKeys");
        assertTrue(keysDir.mkdir());
        for (Object[] k : keys) {
            Files.touch(new File(keysDir, k[0] + ".p12"));
            if (k.length > 1 && k[1] != null) {
                Files.write((byte[]) k[1], new File(keysDir, k[0] + ".p12"));
            }
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
        final Bundle bundle = new Bundle();
        PrivateKeyLoader privateKeyFilesLoader = new PrivateKeyLoader();
        privateKeyFilesLoader.load(bundle, rootProjectDir.getRoot());

        EntityLoader loader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(PrivateKey.class));
        final File privateKeys = new File(config, "private-keys." + fileTyoe);
        Files.touch(privateKeys);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

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

    @Test
    void loadNonexistingDirectory() throws IOException {
        final File identityProvidersFile = new File(config, "private-keys.json");
        Files.touch(identityProvidersFile);

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
        loadPrivateKeys(json, "json", false);
    }

    @Test
    void loadMissingKeyFiles() throws IOException {
        final File identityProvidersFile = new File(config, "private-keys.json");
        Files.touch(identityProvidersFile);

        File keysDir = new File(config, "privateKeys");
        assertTrue(keysDir.mkdir());

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
        loadPrivateKeys(json, "json", false);
    }

    @Test
    void loadMissingKeyFilesFailIfMissing() {
        File keysDir = new File(config, "privateKeys");
        assertTrue(keysDir.mkdir());

        Collection<PrivateKey> privateKeys = new HashSet<>();
        PrivateKey privateKey = new PrivateKey();
        privateKey.setAlias("my-key");
        privateKeys.add(privateKey);

        assertThrows(ConfigLoadException.class, () -> PrivateKey.loadFromDirectory(privateKeys, keysDir, true));
    }

    @Test
    void loadNonexistingDirectoryFailIfMissing() {
        File keysDir = new File(config, "someNonExistingDirectory");

        Collection<PrivateKey> privateKeys = new HashSet<>();
        PrivateKey privateKey = new PrivateKey();
        privateKey.setAlias("my-key");
        privateKeys.add(privateKey);

        assertThrows(ConfigLoadException.class, () -> PrivateKey.loadFromDirectory(privateKeys, keysDir, true));
    }
}