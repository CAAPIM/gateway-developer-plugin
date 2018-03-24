/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

@RunWith(MockitoJUnitRunner.class)
public class ServiceLoaderTest {

    @Rule
    public final TemporaryFolder rootProjectDir = new TemporaryFolder();
    @Mock
    private FileUtils fileUtils;

    @Test
    public void loadJSON() throws IOException {
        ServiceLoader serviceLoader = new ServiceLoader(fileUtils, JsonTools.INSTANCE);
        String json = "{\n" +
                "    \"gateway jamer/gateway-jamer.xml\": {\n" +
                "        \"httpMethods\": [\n" +
                "            \"GET\",\n" +
                "            \"POST\",\n" +
                "            \"PUT\",\n" +
                "            \"DELETE\"\n" +
                "        ],\n" +
                "        \"url\": \"/petstore\"\n" +
                "    },\n" +
                "    \"gateway jamer/gateway-jam-example.xml\": {\n" +
                "        \"httpMethods\": [\n" +
                "            \"PUT\",\n" +
                "            \"DELETE\"\n" +
                "        ],\n" +
                "        \"url\": \"/gateway-jam\"\n" +
                "    }\n" +
                "}";
        File configFolder = rootProjectDir.newFolder("config");
        File servicesFile = new File(configFolder, "services.json");
        Files.touch(servicesFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        Bundle bundle = new Bundle();
        serviceLoader.load(bundle, rootProjectDir.getRoot());

        Assert.assertEquals(2, bundle.getServices().size());
        Assert.assertEquals("/petstore", bundle.getServices().get("gateway jamer/gateway-jamer.xml").getUrl());
        Assert.assertEquals("/gateway-jam", bundle.getServices().get("gateway jamer/gateway-jam-example.xml").getUrl());

        Assert.assertEquals(4, bundle.getServices().get("gateway jamer/gateway-jamer.xml").getHttpMethods().size());
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jamer.xml").getHttpMethods().contains("GET"));
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jamer.xml").getHttpMethods().contains("POST"));
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jamer.xml").getHttpMethods().contains("PUT"));
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jamer.xml").getHttpMethods().contains("DELETE"));
        Assert.assertEquals(2, bundle.getServices().get("gateway jamer/gateway-jam-example.xml").getHttpMethods().size());
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jam-example.xml").getHttpMethods().contains("PUT"));
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jam-example.xml").getHttpMethods().contains("DELETE"));
    }


    @Test
    public void loadYAML() throws IOException {
        ServiceLoader serviceLoader = new ServiceLoader(fileUtils, JsonTools.INSTANCE);
        String json = "gateway jamer/gateway-jamer.xml:\n" +
                "  httpMethods:\n" +
                "  - GET\n" +
                "  - POST\n" +
                "  - PUT\n" +
                "  - DELETE\n" +
                "  url: \"/petstore\"\n" +
                "gateway jamer/gateway-jam-example.xml:\n" +
                "  httpMethods:\n" +
                "  - PUT\n" +
                "  - DELETE\n" +
                "  url: \"/gateway-jam\"";
        File configFolder = rootProjectDir.newFolder("config");
        File servicesFile = new File(configFolder, "services.yml");
        Files.touch(servicesFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        Bundle bundle = new Bundle();
        serviceLoader.load(bundle, rootProjectDir.getRoot());

        Assert.assertEquals(2, bundle.getServices().size());
        Assert.assertEquals("/petstore", bundle.getServices().get("gateway jamer/gateway-jamer.xml").getUrl());
        Assert.assertEquals("/gateway-jam", bundle.getServices().get("gateway jamer/gateway-jam-example.xml").getUrl());

        Assert.assertEquals(4, bundle.getServices().get("gateway jamer/gateway-jamer.xml").getHttpMethods().size());
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jamer.xml").getHttpMethods().contains("GET"));
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jamer.xml").getHttpMethods().contains("POST"));
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jamer.xml").getHttpMethods().contains("PUT"));
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jamer.xml").getHttpMethods().contains("DELETE"));
        Assert.assertEquals(2, bundle.getServices().get("gateway jamer/gateway-jam-example.xml").getHttpMethods().size());
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jam-example.xml").getHttpMethods().contains("PUT"));
        Assert.assertTrue(bundle.getServices().get("gateway jamer/gateway-jam-example.xml").getHttpMethods().contains("DELETE"));
    }

    @Test(expected = BundleLoadException.class)
    public void testBothJsonAndYaml() throws IOException {
        ServiceLoader serviceLoader = new ServiceLoader(fileUtils, JsonTools.INSTANCE);
        File configFolder = rootProjectDir.newFolder("config");
        File servicesJsonFile = new File(configFolder, "services.json");
        Files.touch(servicesJsonFile);
        File servicesYamlFile = new File(configFolder, "services.yml");
        Files.touch(servicesYamlFile);

        Bundle bundle = new Bundle();
        serviceLoader.load(bundle, rootProjectDir.getRoot());
    }

    @Test
    public void testNoServices() {
        ServiceLoader serviceLoader = new ServiceLoader(fileUtils, JsonTools.INSTANCE);

        Bundle bundle = new Bundle();
        serviceLoader.load(bundle, rootProjectDir.getRoot());
        Assert.assertTrue(bundle.getServices().isEmpty());
    }

    @Test(expected = BundleLoadException.class)
    public void badJson() throws IOException {
        ServiceLoader serviceLoader = new ServiceLoader(fileUtils, JsonTools.INSTANCE);
        String json = "{\n" +
                "    \"gateway jamer/gateway-jamer.xml\": {\n" +
                "        \"httpMethods\": [\n" +
                "            \"GET\",\n" +
                "            \"POST\",\n" +
                "            \"PUT\",\n" +
                "            \"DELETE\"\n" +
                "        ],\n" +
                "        \"url\": \"/petstore\"\n" +
                "    },\n" +
                "    \"gateway jamer/gateway-jam-example.xml: {\n" +
                "        \"httpMethods\": [\n" +
                "            \"PUT\",\n" +
                "            \"DELETE\"\n" +
                "        ],\n" +
                "        \"url\": \"/gateway-jam\"\n" +
                "    }\n" +
                "}";
        File configFolder = rootProjectDir.newFolder("config");
        File servicesFile = new File(configFolder, "services.json");
        Files.touch(servicesFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        Bundle bundle = new Bundle();
        serviceLoader.load(bundle, rootProjectDir.getRoot());
    }
}