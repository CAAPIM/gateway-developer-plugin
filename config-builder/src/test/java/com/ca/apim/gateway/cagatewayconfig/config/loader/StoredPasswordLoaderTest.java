/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static com.ca.apim.gateway.cagatewayconfig.beans.StoredPassword.fillDefaultProperties;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class StoredPasswordLoaderTest {

    private static final String PASSWORD_1 = "Password1";
    private static final String PASSWORD_2 = "Password2";
    private static final String GATEWAY = "gateway";

    private TemporaryFolder rootProjectDir;
    @Mock
    private FileUtils fileUtils;

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        rootProjectDir = temporaryFolder;
    }

    @Test
    void loadEmptyPasswordsFile() throws IOException {
        final Bundle bundle = loadPasswords("");

        assertTrue(bundle.getStoredPasswords().isEmpty());
    }

    @Test
    void loadFileWithBlankPasswords() throws IOException {
        loadPasswords("TestPassword=");
    }

    @Test
    void loadFromEnvironment() {
        StoredPasswordsLoader loader = new StoredPasswordsLoader(fileUtils);

        final Bundle bundle = new Bundle();
        loader.load(bundle, PASSWORD_1, "pwd1");

        assertFalse(bundle.getStoredPasswords().isEmpty());
        assertEquals(1, bundle.getStoredPasswords().size());

        // pwd1
        final StoredPassword password1 = bundle.getStoredPasswords().get(PASSWORD_1);
        assertNotNull(password1);
        assertEquals(PASSWORD_1, password1.getName());
        assertEquals("pwd1", password1.getPassword());
        assertFalse(password1.getProperties().isEmpty());
        assertPropertiesContent(fillDefaultProperties(PASSWORD_1, new HashMap<>()), password1.getProperties());
    }

    @Test
    void tryLoadNonexistentFile() {
        StoredPasswordsLoader loader = new StoredPasswordsLoader(fileUtils);

        final Bundle bundle = new Bundle();
        loader.load(bundle, rootProjectDir.getRoot());
        assertTrue(bundle.getStoredPasswords().isEmpty());
    }

    @Test
    void loadFileWithSpecifiedPasswords() throws IOException {
        final Bundle bundle = loadPasswords("Password1=pwd1\nPassword2=pwd2\ngateway=7layer");

        assertFalse(bundle.getStoredPasswords().isEmpty());
        assertEquals(3, bundle.getStoredPasswords().size());

        // pwd1
        final StoredPassword password1 = bundle.getStoredPasswords().get(PASSWORD_1);
        assertNotNull(password1);
        assertEquals(PASSWORD_1, password1.getName());
        assertEquals("pwd1", password1.getPassword());
        assertFalse(password1.getProperties().isEmpty());
        assertPropertiesContent(fillDefaultProperties(PASSWORD_1, new HashMap<>()), password1.getProperties());

        // pwd2
        final StoredPassword password2 = bundle.getStoredPasswords().get(PASSWORD_2);
        assertNotNull(password2);
        assertEquals(PASSWORD_2, password2.getName());
        assertEquals("pwd2", password2.getPassword());
        assertFalse(password2.getProperties().isEmpty());
        assertPropertiesContent(fillDefaultProperties(PASSWORD_2, new HashMap<>()), password2.getProperties());

        // gateway
        final StoredPassword gateway = bundle.getStoredPasswords().get(GATEWAY);
        assertNotNull(gateway);
        assertEquals(GATEWAY, gateway.getName());
        assertEquals("7layer", gateway.getPassword());
        assertFalse(gateway.getProperties().isEmpty());
        assertPropertiesContent(fillDefaultProperties(GATEWAY, new HashMap<>()), gateway.getProperties());
    }

    @Test
    void tryLoadPasswordsFromNonexistentFile() throws IOException {
        StoredPasswordsLoader loader = new StoredPasswordsLoader(fileUtils);
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "stored-passwords.properties");
        Files.touch(identityProvidersFile);

        InputStream stream = mock(InputStream.class);
        when(stream.read(any(byte[].class))).thenThrow(IOException.class);
        when(fileUtils.getInputStream(any(File.class))).thenReturn(stream);

        final Bundle bundle = new Bundle();
        assertThrows(ConfigLoadException.class, () -> loader.load(bundle, rootProjectDir.getRoot()));
    }

    private Bundle loadPasswords(String content) throws IOException {
        StoredPasswordsLoader loader = new StoredPasswordsLoader(fileUtils);
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "stored-passwords.properties");
        Files.touch(identityProvidersFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        loader.load(bundle, rootProjectDir.getRoot());
        return bundle;
    }
}
