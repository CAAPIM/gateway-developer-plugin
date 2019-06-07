/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GlobalEnvironmentProperty;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
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

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.createPropertiesLoader;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class EnvironmentPropertiesLoaderTest {

    private static final String PROP_1 = "Prop1";
    private static final String PROP_2 = "Prop2";
    private static final String PROP_3 = "Prop3";

    private TemporaryFolder rootProjectDir;
    @Mock
    private FileUtils fileUtils;

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        rootProjectDir = temporaryFolder;
    }

    @Test
    void loadEmptyFile() throws IOException {
        final Bundle bundle = loadProperties("");

        assertTrue(bundle.getGlobalEnvironmentProperties().isEmpty());
    }

    @Test
    void loadFileWithPropertiesNoValues() throws IOException {
        loadProperties("Property=");
    }

    @Test
    void loadSinglePropertyFromFile() throws IOException {
        PropertiesLoaderBase loader = createPropertiesLoader(fileUtils, new IdGenerator(), createEntityInfo(GlobalEnvironmentProperty.class));
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "global-env.properties");
        Files.touch(identityProvidersFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream("Prop1=value1\nProp2=value2\nProp3=Gateway".getBytes()));
        Object prop1 = loader.loadSingle("Prop1", identityProvidersFile);

        assertNotNull(prop1);
        assertTrue(prop1 instanceof String);
        assertEquals("value1", prop1);
        assertNull(loader.loadSingle("Prop111", identityProvidersFile));
    }

    @Test
    void loadFromEnvironment() {
        PropertiesLoaderBase loader = createPropertiesLoader(fileUtils, new IdGenerator(), createEntityInfo(GlobalEnvironmentProperty.class));

        final Bundle bundle = new Bundle();
        loader.load(bundle, PROP_1, "value1");

        assertFalse(bundle.getGlobalEnvironmentProperties().isEmpty());
        assertEquals(1, bundle.getGlobalEnvironmentProperties().size());

        // Prop1
        final GlobalEnvironmentProperty prop1 = bundle.getGlobalEnvironmentProperties().get(PROP_1);
        assertNotNull(prop1);
        assertEquals("value1", prop1.getValue());
    }

    @Test
    void tryLoadNonexistentFile() {
        PropertiesLoaderBase loader = createPropertiesLoader(fileUtils, new IdGenerator(), createEntityInfo(GlobalEnvironmentProperty.class));

        final Bundle bundle = new Bundle();
        loader.load(bundle, rootProjectDir.getRoot());
        assertTrue(bundle.getGlobalEnvironmentProperties().isEmpty());
    }

    @Test
    void loadFileWithSpecifiedProperties() throws IOException {
        final Bundle bundle = loadProperties("Prop1=value1\nProp2=value2\nProp3=Gateway");

        assertFalse(bundle.getGlobalEnvironmentProperties().isEmpty());
        assertEquals(3, bundle.getGlobalEnvironmentProperties().size());

        // Prop1
        final GlobalEnvironmentProperty prop1 = bundle.getGlobalEnvironmentProperties().get(PROP_1);
        assertNotNull(prop1);
        assertEquals("value1", prop1.getValue());

        // Prop2
        final GlobalEnvironmentProperty prop2 = bundle.getGlobalEnvironmentProperties().get(PROP_2);
        assertNotNull(prop2);
        assertEquals("value2", prop2.getValue());

        // Prop3
        final GlobalEnvironmentProperty prop3 = bundle.getGlobalEnvironmentProperties().get(PROP_3);
        assertNotNull(prop3);
        assertEquals("Gateway", prop3.getValue());
    }

    @Test
    void tryLoadPropertiesFromNonexistentFile() throws IOException {
        PropertiesLoaderBase loader = createPropertiesLoader(fileUtils, new IdGenerator(), createEntityInfo(GlobalEnvironmentProperty.class));

        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "global-env.properties");
        Files.touch(identityProvidersFile);

        InputStream stream = mock(InputStream.class);
        when(stream.read(any(byte[].class))).thenThrow(IOException.class);
        when(fileUtils.getInputStream(any(File.class))).thenReturn(stream);

        final Bundle bundle = new Bundle();
        assertThrows(ConfigLoadException.class, () -> loader.load(bundle, rootProjectDir.getRoot()));
    }

    private Bundle loadProperties(String content) throws IOException {
        PropertiesLoaderBase loader = createPropertiesLoader(fileUtils, new IdGenerator(), createEntityInfo(GlobalEnvironmentProperty.class));

        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "global-env.properties");
        Files.touch(identityProvidersFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        loader.load(bundle, rootProjectDir.getRoot());
        return bundle;
    }

}