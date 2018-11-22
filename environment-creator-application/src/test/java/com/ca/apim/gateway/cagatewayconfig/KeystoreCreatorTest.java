/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.beans.KeyStoreType;
import com.ca.apim.gateway.cagatewayconfig.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.keystore.KeyStoreCreationException;
import com.ca.apim.gateway.cagatewayconfig.util.keystore.KeystoreHelper;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;

import static com.ca.apim.gateway.cagatewayconfig.KeystoreCreator.KEYSTORE_FILE_NAME;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class KeystoreCreatorTest {

    private TemporaryFolder temporaryFolder;
    private File keystoreFile;
    private File systemProperties;

    @Mock
    private FileUtils fileUtils;

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) throws IOException {
        this.temporaryFolder = temporaryFolder;

        keystoreFile = new File(temporaryFolder.getRoot(), KEYSTORE_FILE_NAME);
        systemProperties = new File(temporaryFolder.getRoot(), "system.properties");
        if (!systemProperties.createNewFile()) {
            throw new RuntimeException("Could not create system.properties file");
        }
    }

    @Test
    void createKeystore_noKeys() {
        KeystoreCreator.createKeyStoreIfNecessary(temporaryFolder.getRoot().getPath(), getClass().getClassLoader().getResource("privateKeys").getPath(), emptyList(), fileUtils, systemProperties.getPath());
        assertFalse(new File(temporaryFolder.getRoot(), KEYSTORE_FILE_NAME).exists());
    }

    @Test
    void createKeystore_checkKeystoreFile() {
        when(fileUtils.getOutputStream(any(File.class))).thenCallRealMethod();

        KeystoreCreator.createKeyStoreIfNecessary(temporaryFolder.getRoot().getPath(), getClass().getClassLoader().getResource("privateKeys").getPath(), singletonList(createPrivateKey()), fileUtils, systemProperties.getPath());
        // intention here is not to test the keystore contents, only if is created
        assertTrue(new File(temporaryFolder.getRoot(), KEYSTORE_FILE_NAME).exists());
    }

    @Test
    void createKeystore_errorWriting() {
        when(fileUtils.getOutputStream(any(File.class))).thenReturn(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException();
            }
        });

        assertThrows(KeyStoreCreationException.class,
                () -> KeystoreCreator.createKeyStoreIfNecessary(temporaryFolder.getRoot().getPath(), getClass().getClassLoader().getResource("privateKeys").getPath(), singletonList(createPrivateKey()), fileUtils, systemProperties.getPath()));
    }

    @Test
    void createKeystore_checkPropertiesFile() throws IOException {
        final KeystoreHelper keystoreHelper = new KeystoreHelper();

        KeystoreCreator.updateSystemPropertiesFile(keystoreHelper, keystoreFile, systemProperties.getPath());

        assertTrue(systemProperties.exists());
        try (InputStream stream = Files.newInputStream(systemProperties.toPath())){
            Properties properties = new Properties();
            properties.load(stream);

            assertFalse(properties.isEmpty());
            assertEquals(6, properties.size());

            assertNotNull(properties.getProperty("com.l7tech.common.security.jceProviderEngineName"));
            assertEquals("generic", properties.getProperty("com.l7tech.common.security.jceProviderEngineName"));

            assertNotNull(properties.getProperty("com.l7tech.keystore.type"));
            assertEquals(keystoreHelper.getKeyStoreType(), properties.getProperty("com.l7tech.keystore.type"));

            assertNotNull(properties.getProperty("com.l7tech.keystore.path"));
            assertEquals(FilenameUtils.separatorsToUnix(keystoreFile.getPath()), properties.getProperty("com.l7tech.keystore.path"));

            assertNotNull(properties.getProperty("com.l7tech.keystore.savePath"));
            assertEquals("EMPTY", properties.getProperty("com.l7tech.keystore.savePath"));

            assertNotNull(properties.getProperty("com.l7tech.keystore.password"));
            assertEquals(new String(keystoreHelper.getKeystorePassword()), properties.getProperty("com.l7tech.keystore.password"));

            assertNotNull(properties.getProperty("com.l7tech.common.security.jceProviderEngine"));
            assertEquals("com.l7tech.security.prov.generic.GenericJceProviderEngine", properties.getProperty("com.l7tech.common.security.jceProviderEngine"));
        }

        Files.delete(systemProperties.toPath());
    }

    @Test
    void createKeystore_errorPropertiesFile() throws IOException {
        final KeystoreHelper keystoreHelper = new KeystoreHelper();

        if (FileUtils.POSIX_ENABLED) {
            Files.setPosixFilePermissions(systemProperties.toPath(), PosixFilePermissions.fromString("---------"));
        } else {
            systemProperties.setWritable(false);
        }
        assertThrows(KeyStoreCreationException.class, () -> KeystoreCreator.updateSystemPropertiesFile(keystoreHelper, keystoreFile, systemProperties.getPath()));
        if (FileUtils.POSIX_ENABLED) {
            Files.setPosixFilePermissions(systemProperties.toPath(), PosixFilePermissions.fromString("rwxrwxrwx"));
        } else {
            systemProperties.setWritable(true);
        }
        Files.delete(systemProperties.toPath());
    }

    @NotNull
    private PrivateKey createPrivateKey() {
        PrivateKey privateKey = new PrivateKey();
        privateKey.setAlias("test");
        privateKey.setKeyStoreType(KeyStoreType.GENERIC);
        privateKey.setKeystore(KeyStoreType.GENERIC.getName());
        privateKey.setAlgorithm("RSA");
        privateKey.setKeyPassword("");
        return privateKey;
    }
}