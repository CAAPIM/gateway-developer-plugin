/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ClusterProperty;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class StaticPropertiesLoaderTest {

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

        assertTrue(bundle.getStaticProperties().isEmpty());
    }

    @Test
    void loadFileWithPropertiesNoValues() throws IOException {
        loadProperties("Property=");
    }

    @Test
    void loadFromEnvironment() {
        PropertiesLoaderBase loader = EntityLoaderUtils.createPropertiesLoader(fileUtils, new IdGenerator(), createEntityInfo(ClusterProperty.class));

        final Bundle bundle = new Bundle();
        loader.load(bundle, PROP_1, "value1");

        assertFalse(bundle.getStaticProperties().isEmpty());
        assertEquals(1, bundle.getStaticProperties().size());

        // Prop1
        final ClusterProperty prop1 = bundle.getStaticProperties().get(PROP_1);
        assertNotNull(prop1);
        assertEquals("value1", prop1.getValue());
    }

    @Test
    void tryLoadNonexistentFile() {
        PropertiesLoaderBase loader = EntityLoaderUtils.createPropertiesLoader(fileUtils, new IdGenerator(), createEntityInfo(ClusterProperty.class));

        final Bundle bundle = new Bundle();
        loader.load(bundle, rootProjectDir.getRoot());
        assertTrue(bundle.getStaticProperties().isEmpty());
    }

    @Test
    void loadFileWithSpecifiedProperties() throws IOException {
        final Bundle bundle = loadProperties("Prop1=value1\nProp2=value2\nProp3=Gateway");

        assertFalse(bundle.getStaticProperties().isEmpty());
        assertEquals(3, bundle.getStaticProperties().size());

        // Prop1
        final ClusterProperty prop1 = bundle.getStaticProperties().get(PROP_1);
        assertNotNull(prop1);
        assertEquals("value1", prop1.getValue());

        // Prop2
        final ClusterProperty prop2 = bundle.getStaticProperties().get(PROP_2);
        assertNotNull(prop2);
        assertEquals("value2", prop2.getValue());

        // Prop3
        final ClusterProperty prop3 = bundle.getStaticProperties().get(PROP_3);
        assertNotNull(prop3);
        assertEquals("Gateway", prop3.getValue());
    }

    @Test
    void tryLoadPropertiesFromNonexistentFile() throws IOException {
        PropertiesLoaderBase loader = EntityLoaderUtils.createPropertiesLoader(fileUtils, new IdGenerator(), createEntityInfo(ClusterProperty.class));
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "static.properties");
        Files.touch(identityProvidersFile);

        InputStream stream = mock(InputStream.class);
        when(stream.read(any(byte[].class))).thenThrow(IOException.class);
        when(fileUtils.getInputStream(any(File.class))).thenReturn(stream);

        final Bundle bundle = new Bundle();
        assertThrows(ConfigLoadException.class, () -> loader.load(bundle, rootProjectDir.getRoot()));
    }

    private Bundle loadProperties(String content) throws IOException {
        PropertiesLoaderBase loader = EntityLoaderUtils.createPropertiesLoader(fileUtils, new IdGenerator(), createEntityInfo(ClusterProperty.class));
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "static.properties");
        Files.touch(identityProvidersFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        loader.load(bundle, rootProjectDir.getRoot());
        return bundle;
    }
}
