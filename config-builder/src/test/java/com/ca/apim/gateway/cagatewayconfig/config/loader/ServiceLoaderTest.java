/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonToolsException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
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

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.createEntityLoader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ServiceLoaderTest {

    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;

    private final String SERVICE_NAME_1 = "example";
    private final String SERVICE_NAME_2 = "projectName/v1/subfolder/example-project";
    private final String SERVICE_NAME_3 = "project/v1/SOAP/SoapService";   //SOAP

    @BeforeEach
    void before() {
        jsonTools = new JsonTools(fileUtils);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void loadJSON(TemporaryFolder temporaryFolder) throws IOException {
        EntityLoader serviceLoader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(Service.class));
        String json = "{\n" +
                "    \"" + SERVICE_NAME_1 + "\": {\n" +
                "        \"policy\": \"projectName/test\", \n" +
                "        \"httpMethods\": [\n" +
                "            \"GET\",\n" +
                "            \"POST\",\n" +
                "            \"PUT\",\n" +
                "            \"DELETE\"\n" +
                "        ],\n" +
                "        \"url\": \"/example\"\n" +
                "    },\n" +
                "    \"" + SERVICE_NAME_2 + "\": {\n" +
                "        \"policy\": \"projectName/test\", \n" +
                "        \"httpMethods\": [\n" +
                "            \"PUT\",\n" +
                "            \"DELETE\"\n" +
                "        ],\n" +
                "        \"url\": \"/example-project\",\n" +
                "        \"properties\": {\n" +
                "            \"key\": \"value\",\n" +
                "            \"key.1\": \"value.1\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"" + SERVICE_NAME_3 + "\": {\n" +
                "        \"url\": \"/soaptest\",\n" +
                "        \"policy\": \"project/SOAP/SoapService\",\n" +
                "        \"httpMethods\": [\n" +
                "            \"POST\"\n" +
                "        ],\n" +
                "        \"properties\": {},\n" +
                "        \"wsdl\": {\n" +
                "            \"rootUrl\": \"/path/to/wsdl/SoapService.wsdl\",\n" +
                "            \"soapVersion\": \"1.1\",\n" +
                "            \"wssProcessingEnabled\": true,\n" +
                "            \"wsdlXml\": \"wsdl xml content\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
        File configFolder = temporaryFolder.createDirectory("config");
        File servicesFile = new File(configFolder, "services.json");
        Files.touch(servicesFile);

        temporaryFolder.createDirectory("policy");

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        Bundle bundle = new Bundle();
        //start with no folders
        assertTrue(bundle.getFolders().isEmpty());

        serviceLoader.load(bundle, temporaryFolder.getRoot());

        verifyConfig(bundle);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void loadYAML(TemporaryFolder temporaryFolder) throws IOException {
        EntityLoader serviceLoader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(Service.class));
        String json = SERVICE_NAME_1 + ":\n" +
                "  policy: \"projectName/test\"\n" +
                "  httpMethods:\n" +
                "  - GET\n" +
                "  - POST\n" +
                "  - PUT\n" +
                "  - DELETE\n" +
                "  url: \"/example\"\n" +
                SERVICE_NAME_2 + ":\n" +
                "  policy: \"projectName/test\"\n" +
                "  httpMethods:\n" +
                "  - PUT\n" +
                "  - DELETE\n" +
                "  url: \"/example-project\"\n" +
                "  properties:\n" +
                "    key: \"value\"\n" +
                "    key.1: \"value.1\"\n" +
                SERVICE_NAME_3 + ":\n" +
                "  url: \"/soaptest\"\n" +
                "  policy: project/SOAP/SoapService\n" +
                "  httpMethods:\n" +
                "  - POST\n" +
                "  properties: {}\n" +
                "  wsdl:\n" +
                "    rootUrl: \"/path/to/wsdl/SoapService.wsdl\"\n" +
                "    soapVersion: \"1.1\"\n" +
                "    wssProcessingEnabled: true\n" +
                "    wsdlXml: wsdl xml content";
        File configFolder = temporaryFolder.createDirectory("config");
        File servicesFile = new File(configFolder, "services.yml");
        Files.touch(servicesFile);

        temporaryFolder.createDirectory("policy");

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        Bundle bundle = new Bundle();
        serviceLoader.load(bundle, temporaryFolder.getRoot());

        verifyConfig(bundle);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testBothJsonAndYaml(TemporaryFolder temporaryFolder) throws IOException {
        EntityLoader serviceLoader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(Service.class));
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
        EntityLoader serviceLoader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(Service.class));

        Bundle bundle = new Bundle();
        serviceLoader.load(bundle, temporaryFolder.getRoot());
        assertTrue(bundle.getServices().isEmpty());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void badJson(TemporaryFolder temporaryFolder) throws IOException {
        EntityLoader serviceLoader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(Service.class));
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

    private void verifyConfig(Bundle bundle) {
        Service service1 = bundle.getServices().get(SERVICE_NAME_1);
        Service service2 = bundle.getServices().get(SERVICE_NAME_2);

        assertEquals(3, bundle.getServices().size());
        assertEquals("/example", service1.getUrl());
        assertEquals("/example-project", service2.getUrl());

        assertEquals(4, service1.getHttpMethods().size());
        assertTrue(service1.getHttpMethods().contains("GET"));
        assertTrue(service1.getHttpMethods().contains("POST"));
        assertTrue(service1.getHttpMethods().contains("PUT"));
        assertTrue(service1.getHttpMethods().contains("DELETE"));
        assertEquals(2, service2.getHttpMethods().size());
        assertTrue(service2.getHttpMethods().contains("PUT"));
        assertTrue(service2.getHttpMethods().contains("DELETE"));
        assertEquals(2, service2.getProperties().keySet().size());
        assertEquals("value", service2.getProperties().get("key"));
        assertEquals("value.1", service2.getProperties().get("key.1"));
    }
}