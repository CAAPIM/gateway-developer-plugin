/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonToolsException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class JdbcConnectionLoaderTest {

    private static final String CONNECTION_NAME = "MySQL";

    private TemporaryFolder rootProjectDir;
    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        jsonTools = new JsonTools(fileUtils);
        rootProjectDir = temporaryFolder;
    }

    @Test
    void loadJdbcConnectionYaml() throws IOException {
        String yaml = CONNECTION_NAME + ":\n" +
                "  driverClass: \"com.mysql.jdbc.Driver\"\n" +
                "  jdbcUrl: \"jdbc:mysql://localhost:3306/ssg\"\n" +
                "  user: \"gateway\"\n" +
                "  passwordRef: \"gateway\"\n" +
                "  minimumPoolSize: 3\n" +
                "  maximumPoolSize: 15\n" +
                "  properties:\n" +
                "    EnableCancelTimeout: \"true\"\n";
        loadJdbcConnection(yaml, "yml", false);
    }

    @Test
    void loadJdbcConnectionJson() throws IOException {
        String json = "{\n" +
                "  \"" + CONNECTION_NAME + "\" : {\n" +
                "    \"driverClass\" : \"com.mysql.jdbc.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:mysql://localhost:3306/ssg\",\n" +
                "    \"user\" : \"gateway\",\n" +
                "    \"passwordRef\" : \"gateway\",\n" +
                "    \"minimumPoolSize\" : 3,\n" +
                "    \"maximumPoolSize\" : 15,\n" +
                "    \"properties\" : {\n" +
                "      \"EnableCancelTimeout\" : \"true\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        loadJdbcConnection(json, "json", false);
    }

    @Test
    void loadJdbcConnectionMalformedYaml() throws IOException {
        String yaml = CONNECTION_NAME + ":\n" +
                "  driverClass com.mysql.jdbc.Driver\n" +
                "  jdbcUrl: \"jdbc:mysql://localhost:3306/ssg\"\n" +
                "  user: \"gateway\"\n" +
                "  passwordRef: \"gateway\"\n" +
                "  minimumPoolSize: 3\n" +
                "  maximumPoolSize: 15\n" +
                "  properties:\n" +
                "    EnableCancelTimeout: \"true\"\n";
        loadJdbcConnection(yaml, "yml", true);
    }

    @Test
    void loadJdbcConnectionMalformedJson() throws IOException {
        String json = "{\n" +
                "  \"" + CONNECTION_NAME + "\" : {\n" +
                "    \"driverClass\" : \"com.mysql.jdbc.Driver\",\n" +
                "    \"jdbcUrl\" : \"jdbc:mysql://localhost:3306/ssg\",\n" +
                "    \"user\" : \"gateway\",\n" +
                "    \"passwordRef\" : \"gateway\",\n" +
                "    \"minimumPoolSize\" : 3,\n" +
                "    \"maximumPoolSize\" : 15,\n" +
                "    \"properties\" : {\n" +
                "      \"EnableCancelTimeout\" : \"true\"\n" +
                "    }\n" +
                "  }\n";
        loadJdbcConnection(json, "json", true);
    }

    private void loadJdbcConnection(String content, String fileTyoe, boolean expectException) throws IOException {
        JdbcConnectionLoader loader = new JdbcConnectionLoader(jsonTools);
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "jdbc-connections." + fileTyoe);
        Files.touch(identityProvidersFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        if (expectException) {
            assertThrows(JsonToolsException.class, () -> loadJdbcConnections(loader, bundle, rootProjectDir));
            return;
        } else {
            loadJdbcConnections(loader, bundle, rootProjectDir);
        }
        checkJdbcConnection(bundle);
    }

    private static void loadJdbcConnections(JdbcConnectionLoader loader, Bundle bundle, TemporaryFolder rootProjectDir) {
        loader.load(bundle, rootProjectDir.getRoot());
    }

    private static void checkJdbcConnection(Bundle bundle) {
        assertFalse(bundle.getJdbcConnections().isEmpty(), "No connections loaded");
        assertEquals(1, bundle.getJdbcConnections().size(), () -> "Expected 1 connection, found " + bundle.getJdbcConnections().size());
        assertNotNull(bundle.getJdbcConnections().get(CONNECTION_NAME), "MySQL not found");

        JdbcConnection jdbcConnection = bundle.getJdbcConnections().get(CONNECTION_NAME);
        assertEquals("com.mysql.jdbc.Driver", jdbcConnection.getDriverClass());
        assertEquals("jdbc:mysql://localhost:3306/ssg", jdbcConnection.getJdbcUrl());
        assertEquals(15, jdbcConnection.getMaximumPoolSize().intValue());
        assertEquals(3, jdbcConnection.getMinimumPoolSize().intValue());
        assertEquals("gateway", jdbcConnection.getUser());
        assertEquals("gateway", jdbcConnection.getPasswordRef());
        assertPropertiesContent(ImmutableMap.of("EnableCancelTimeout", Boolean.TRUE.toString()), jdbcConnection.getProperties());
    }
}
