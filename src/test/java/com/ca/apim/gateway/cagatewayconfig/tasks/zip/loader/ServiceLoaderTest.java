/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.ServiceEntityBuilder;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonToolsException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ServiceLoaderTest {

    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;

    @BeforeEach
    void before() {
        jsonTools = new JsonTools(fileUtils);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void loadJSON(TemporaryFolder temporaryFolder) throws IOException {
        ServiceLoader serviceLoader = new ServiceLoader(jsonTools);
        String json = "{\n" +
                "    \"example project/example.xml\": {\n" +
                "        \"httpMethods\": [\n" +
                "            \"GET\",\n" +
                "            \"POST\",\n" +
                "            \"PUT\",\n" +
                "            \"DELETE\"\n" +
                "        ],\n" +
                "        \"url\": \"/example\"\n" +
                "    },\n" +
                "    \"example project/example-project.xml\": {\n" +
                "        \"httpMethods\": [\n" +
                "            \"PUT\",\n" +
                "            \"DELETE\"\n" +
                "        ],\n" +
                "        \"url\": \"/example-project\",\n" +
                "        \"properties\": {\n" +
                "            \"key\": \"value\",\n" +
                "            \"key.1\": \"value.1\"\n" +
                "        }" +
                "    }\n" +
                "}";
        File configFolder = temporaryFolder.createDirectory("config");
        File servicesFile = new File(configFolder, "services.json");
        Files.touch(servicesFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        Bundle bundle = new Bundle();
        serviceLoader.load(bundle, temporaryFolder.getRoot());

        Assert.assertEquals(2, bundle.getServices().size());
        Assert.assertEquals("/example", bundle.getServices().get("example project/example.xml").getUrl());
        Assert.assertEquals("/example-project", bundle.getServices().get("example project/example-project.xml").getUrl());

        Assert.assertEquals(4, bundle.getServices().get("example project/example.xml").getHttpMethods().size());
        Assert.assertTrue(bundle.getServices().get("example project/example.xml").getHttpMethods().contains("GET"));
        Assert.assertTrue(bundle.getServices().get("example project/example.xml").getHttpMethods().contains("POST"));
        Assert.assertTrue(bundle.getServices().get("example project/example.xml").getHttpMethods().contains("PUT"));
        Assert.assertTrue(bundle.getServices().get("example project/example.xml").getHttpMethods().contains("DELETE"));
        Assert.assertEquals(2, bundle.getServices().get("example project/example-project.xml").getHttpMethods().size());
        Assert.assertTrue(bundle.getServices().get("example project/example-project.xml").getHttpMethods().contains("PUT"));
        Assert.assertTrue(bundle.getServices().get("example project/example-project.xml").getHttpMethods().contains("DELETE"));
        Assert.assertEquals(2, bundle.getServices().get("example project/example-project.xml").getProperties().keySet().size());
        Assert.assertEquals("value", bundle.getServices().get("example project/example-project.xml").getProperties().get("key"));
        Assert.assertEquals("value.1", bundle.getServices().get("example project/example-project.xml").getProperties().get("key.1"));
    }


    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void loadYAML(TemporaryFolder temporaryFolder) throws IOException {
        ServiceLoader serviceLoader = new ServiceLoader(jsonTools);
        String json = "example project/example.xml:\n" +
                "  httpMethods:\n" +
                "  - GET\n" +
                "  - POST\n" +
                "  - PUT\n" +
                "  - DELETE\n" +
                "  url: \"/example\"\n" +
                "example project/example-project.xml:\n" +
                "  httpMethods:\n" +
                "  - PUT\n" +
                "  - DELETE\n" +
                "  url: \"/example-project\"\n" +
                "  properties:\n" +
                "    key: \"value\"\n" +
                "    key.1: \"value.1\"";
        File configFolder = temporaryFolder.createDirectory("config");
        File servicesFile = new File(configFolder, "services.yml");
        Files.touch(servicesFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        Bundle bundle = new Bundle();
        serviceLoader.load(bundle, temporaryFolder.getRoot());

        Assert.assertEquals(2, bundle.getServices().size());
        Assert.assertEquals("/example", bundle.getServices().get("example project/example.xml").getUrl());
        Assert.assertEquals("/example-project", bundle.getServices().get("example project/example-project.xml").getUrl());

        Assert.assertEquals(4, bundle.getServices().get("example project/example.xml").getHttpMethods().size());
        Assert.assertTrue(bundle.getServices().get("example project/example.xml").getHttpMethods().contains("GET"));
        Assert.assertTrue(bundle.getServices().get("example project/example.xml").getHttpMethods().contains("POST"));
        Assert.assertTrue(bundle.getServices().get("example project/example.xml").getHttpMethods().contains("PUT"));
        Assert.assertTrue(bundle.getServices().get("example project/example.xml").getHttpMethods().contains("DELETE"));
        Assert.assertEquals(2, bundle.getServices().get("example project/example-project.xml").getHttpMethods().size());
        Assert.assertTrue(bundle.getServices().get("example project/example-project.xml").getHttpMethods().contains("PUT"));
        Assert.assertTrue(bundle.getServices().get("example project/example-project.xml").getHttpMethods().contains("DELETE"));
        Assert.assertEquals(2, bundle.getServices().get("example project/example-project.xml").getProperties().keySet().size());
        Assert.assertEquals("value", bundle.getServices().get("example project/example-project.xml").getProperties().get("key"));
        Assert.assertEquals("value.1", bundle.getServices().get("example project/example-project.xml").getProperties().get("key.1"));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testBothJsonAndYaml(TemporaryFolder temporaryFolder) throws IOException {
        ServiceLoader serviceLoader = new ServiceLoader(jsonTools);
        File configFolder = temporaryFolder.createDirectory("config");
        File servicesJsonFile = new File(configFolder, "services.json");
        Files.touch(servicesJsonFile);
        File servicesYamlFile = new File(configFolder, "services.yml");
        Files.touch(servicesYamlFile);

        Bundle bundle = new Bundle();
        assertThrows(JsonToolsException.class, () -> serviceLoader.load(bundle, temporaryFolder.getRoot()));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testNoServices(TemporaryFolder temporaryFolder) {
        ServiceLoader serviceLoader = new ServiceLoader(jsonTools);

        Bundle bundle = new Bundle();
        serviceLoader.load(bundle, temporaryFolder.getRoot());
        Assert.assertTrue(bundle.getServices().isEmpty());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void badJson(TemporaryFolder temporaryFolder) throws IOException {
        ServiceLoader serviceLoader = new ServiceLoader(jsonTools);
        String json = "{\n" +
                "    \"example project/example.xml\": {\n" +
                "        \"httpMethods\": [\n" +
                "            \"GET\",\n" +
                "            \"POST\",\n" +
                "            \"PUT\",\n" +
                "            \"DELETE\"\n" +
                "        ],\n" +
                "        \"url\": \"/example\"\n" +
                "    },\n" +
                "    \"example project/example-project.xml: {\n" +
                "        \"httpMethods\": [\n" +
                "            \"PUT\",\n" +
                "            \"DELETE\"\n" +
                "        ],\n" +
                "        \"url\": \"/example-project\"\n" +
                "    }\n" +
                "}";
        File configFolder = temporaryFolder.createDirectory("config");
        File servicesFile = new File(configFolder, "services.json");
        Files.touch(servicesFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        Bundle bundle = new Bundle();
        assertThrows(JsonToolsException.class, () -> serviceLoader.load(bundle, temporaryFolder.getRoot()));
    }
}