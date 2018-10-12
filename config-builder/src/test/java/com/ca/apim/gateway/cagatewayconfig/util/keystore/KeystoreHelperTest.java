/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.keystore;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PrivateKey;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import static java.security.KeyStore.getInstance;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TemporaryFolderExtension.class)
class KeystoreHelperTest {

    private TemporaryFolder rootProjectDir;
    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        rootProjectDir = temporaryFolder;
    }

    @Test
    void createKeyStoreWithKeys() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeystoreHelper keystoreHelper = new KeystoreHelper();
        final byte[] keyStore = keystoreHelper.createKeyStore(Arrays.asList(createPrivateKey("test"), createPrivateKey("test2")));

        assertNotNull(keyStore);
        assertTrue(keyStore.length > 0);

        KeyStore ks = getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(keyStore), keystoreHelper.getKeystorePassword());

        assertEquals(2, ks.size());
        assertTrue(ks.containsAlias("test"));
        assertTrue(ks.containsAlias("test2"));
        assertNotNull(ks.getKey("test", EMPTY.toCharArray()));
        assertNotNull(ks.getKey("test2", EMPTY.toCharArray()));
    }

    @Test
    void createEmptyKeyStore() {
        KeystoreHelper keystoreHelper = new KeystoreHelper();
        final byte[] keyStore = keystoreHelper.createKeyStore(emptyList());

        assertNotNull(keyStore);
        assertEquals(0, keyStore.length);
    }

    @Test
    void tryCreateKeyStoreMissingKeyFile() {
        KeystoreHelper keystoreHelper = new KeystoreHelper();
        assertThrows(KeyStoreCreationException.class, () -> keystoreHelper.createKeyStore(singletonList(createPrivateKey("test", EMPTY, false))));
    }

    @Test
    void checkKeyStoreIsPKCS12() {
        KeystoreHelper keystoreHelper = new KeystoreHelper();
        KeyStore keyStore = keystoreHelper.createKeyStoreInstance(emptyList());

        assertNotNull(keyStore);
        assertEquals("PKCS12", keyStore.getType());
    }

    @Test
    void tryCreateKeyStoreWrongPasswordForKey() {
        KeystoreHelper keystoreHelper = new KeystoreHelper();
        assertThrows(KeyStoreCreationException.class, () -> keystoreHelper.createKeyStore(singletonList(createPrivateKey("test", "test", true))));
    }

    @Test
    void tryCreateKeyStoreIncorrectKeyAlias() {
        KeystoreHelper keystoreHelper = new KeystoreHelper();
        assertThrows(KeyStoreCreationException.class, () -> keystoreHelper.createKeyStore(singletonList(createPrivateKey("testXXXX"))));
    }

    @Test
    void tryCreateKeyStoreMalformedP12File() {
        KeystoreHelper keystoreHelper = new KeystoreHelper();
        assertThrows(KeyStoreCreationException.class, () -> keystoreHelper.createKeyStore(singletonList(createPrivateKey("testInvalid"))));
    }

    @Test
    void tryCreateKeyStoreInvalidKeyStoreType() {
        KeystoreHelper keystoreHelper = new KeystoreHelper() {
            @Override
            @NotNull
            public String getKeyStoreType() {
                return "GW";
            }
        };
        assertThrows(KeyStoreCreationException.class, () -> keystoreHelper.createKeyStore(singletonList(createPrivateKey("testInvalid"))));
    }

    @Test
    void tryGetCertificateForUninitializedKeyStore() throws IOException {
        KeystoreHelper keystoreHelper = new KeystoreHelper();
        final PrivateKey key = createPrivateKey("test");
        assertThrows(KeyStoreCreationException.class, () -> keystoreHelper.loadCertificatesForPrivateKey(key, KeyStore.getInstance(keystoreHelper.getKeyStoreType())));
    }

    @Test
    void tryCreateKeyStoreUninitializedForKey() {
        KeystoreHelper keystoreHelper = new KeystoreHelper() {
            @Override
            public KeyStore loadKeyStore(PrivateKey pk) {
                try {
                    return KeyStore.getInstance(this.getKeyStoreType());
                } catch (KeyStoreException e) {
                    throw new KeyStoreCreationException(e.getMessage(), e);
                }
            }
        };
        assertThrows(KeyStoreCreationException.class, () -> keystoreHelper.createKeyStore(singletonList(createPrivateKey("test"))));
    }

    @Test
    void tryCreateKeyStoreUninitialized() {
        KeystoreHelper keystoreHelper = new KeystoreHelper() {
            @Override
            KeyStore loadKeyStore(InputStream stream, char[] password) throws KeyStoreException {
                return KeyStore.getInstance(getKeyStoreType());
            }
        };
        assertThrows(KeyStoreCreationException.class, () -> keystoreHelper.createKeyStore(singletonList(createPrivateKey("test"))));
    }

    @Test
    void tryGetBytesKeyStoreUninitialized() {
        KeystoreHelper keystoreHelper = new KeystoreHelper();
        assertThrows(KeyStoreCreationException.class, () -> keystoreHelper.toBytes(KeyStore.getInstance(keystoreHelper.getKeyStoreType())));
    }

    @Test
    void tryStoreKeyStoreUninitialized() {
        KeystoreHelper keystoreHelper = new KeystoreHelper();
        assertThrows(KeyStoreCreationException.class, () -> keystoreHelper.storeKey(KeyStore.getInstance(keystoreHelper.getKeyStoreType()), createPrivateKey("test")));
    }

    private PrivateKey createPrivateKey(String alias) throws IOException {
        return createPrivateKey(alias, EMPTY, true);
    }

    private PrivateKey createPrivateKey(String alias, String keyPassword, boolean writeKey) throws IOException {
        File privateKeysDir = new File(rootProjectDir.getRoot(), "config/privateKeys");
        privateKeysDir.mkdirs();
        File privateKeyFile = new File(privateKeysDir, alias + ".p12");
        privateKeyFile.createNewFile();

        PrivateKey key = new PrivateKey();
        key.setAlias(alias);
        key.setPrivateKeyFile(() -> Files.newInputStream(privateKeyFile.toPath()));
        key.setKeyPassword(keyPassword);
        key.setAlgorithm("RSA");

        if (writeKey) {
            Files.write(privateKeyFile.toPath(), toByteArray(getClass().getClassLoader().getResource(alias + ".p12")));
        }
        return key;
    }

}