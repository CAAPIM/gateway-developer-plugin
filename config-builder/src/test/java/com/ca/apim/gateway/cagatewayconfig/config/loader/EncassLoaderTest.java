/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
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
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.createEntityLoader;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class EncassLoaderTest {

    private static final String NAME = "encass policy";
    private static final String POLICY_PATH = "gateway-solution/encass-policy.xml";

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
    void loadYaml() throws IOException {
        String yaml = NAME + ":\n" +
                "  policy: " + POLICY_PATH + "\n" +
                "  arguments:\n" +
                "  - name: \"hello\"\n" +
                "    type: \"string\"\n" +
                "  - name: \"hello-again\"\n" +
                "    type: \"message\"\n" +
                "  results:\n" +
                "  - name: \"goodbye\"\n" +
                "    type: \"string\"\n" +
                "  - name: \"goodbye-again\"\n" +
                "    type: \"message\"\n" +
                "  properties:\n" +
                "    " + PALETTE_FOLDER + ": \"policyLogic\"\n" +
                "    " + PALETTE_ICON_RESOURCE_NAME + ": \"OversizedElement16.gif\"\n" +
                "    " + ALLOW_TRACING + ": \"false\"\n" +
                "    " + DESCRIPTION + ": \"some description\"\n" +
                "    " + PASS_METRICS_TO_PARENT + ": \"false\"\n";
        load(yaml, "yml", false);
    }

    @Test
    void loadJson() throws IOException {
        String json = "{\n" +
                "  \"" + NAME + "\" : {\n" +
                "      \"policy\": \"" + POLICY_PATH + "\",\n" +
                "      \"arguments\": [\n" +
                "         {\n" +
                "            \"name\": \"hello\",\n" +
                "            \"type\": \"string\"\n" +
                "         },\n" +
                "         {\n" +
                "            \"name\": \"hello-again\",\n" +
                "            \"type\": \"message\"\n" +
                "         }\n" +
                "      ],\n" +
                "      \"results\": [\n" +
                "         {\n" +
                "            \"name\": \"goodbye\",\n" +
                "            \"type\": \"string\"\n" +
                "         },\n" +
                "         {\n" +
                "            \"name\": \"goodbye-again\",\n" +
                "            \"type\": \"message\"\n" +
                "         }\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "         \"" + PALETTE_FOLDER + "\": \"policyLogic\",\n" +
                "         \"" + PALETTE_ICON_RESOURCE_NAME + "\": \"OversizedElement16.gif\",\n" +
                "         \"" + ALLOW_TRACING + "\": \"false\",\n" +
                "         \"" + DESCRIPTION + "\": \"some description\",\n" +
                "         \"" + PASS_METRICS_TO_PARENT + "\": \"false\"\n" +
                "      }\n" +
                "   }\n" +
                "}";
        load(json, "json", false);
    }

    @Test
    void loadMalformedYaml() throws IOException {
        String yaml = NAME + ":\n" +
                "  arguments:\n" +
                "  - name: \"hello\"\n" +
                "    type: \"string\"\n" +
                "  - name: \"hello-again\"\n" +
                "    type: \"message\"\n" +
                "  results:\n" +
                "   name: \"goodbye\"\n" +
                "   type: \"string\"\n" +
                "   name: \"goodbye-again\"\n" +
                "   type: \"message\"\n";
        load(yaml, "yml", true);
    }

    @Test
    void loadMalformedJson() throws IOException {
        String json = "{\n" +
                "      \"arguments\": [\n" +
                "         {\n" +
                "            \"name\": hello\",\n" +
                "            \"type\": string\"\n" +
                "         },\n" +
                "         {\n" +
                "            \"name\": \"hello-again\",\n" +
                "            \"type\": \"message\"\n" +
                "         }\n" +
                "      ],\n" +
                "      \"results\": [\n" +
                "         {\n" +
                "            \"name\": \"goodbye\",\n" +
                "            \"type\": \"string\"\n" +
                "         },\n" +
                "         {\n" +
                "            \"name\": \"goodbye-again\",\n" +
                "            \"type\": \"message\"\n" +
                "         }\n" +
                "      ]\n" +
                "   }";
        load(json, "json", true);
    }

    private void load(String content, String fileTyoe, boolean expectException) throws IOException {
        EntityLoader loader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(Encass.class));
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "encass." + fileTyoe);
        Files.touch(identityProvidersFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        if (expectException) {
            assertThrows(JsonToolsException.class, () -> load(loader, bundle, rootProjectDir));
            return;
        } else {
            load(loader, bundle, rootProjectDir);
        }
        check(bundle);
    }

    private static void load(EntityLoader loader, Bundle bundle, TemporaryFolder rootProjectDir) {
        loader.load(bundle, rootProjectDir.getRoot());
    }

    private static void check(Bundle bundle) {
        assertFalse(bundle.getEncasses().isEmpty(), "No encapsulated assertions loaded");
        assertEquals(1, bundle.getEncasses().size(), () -> "Expected 1 encapsulated assertion, found " + bundle.getEncasses().size());
        assertNotNull(bundle.getEncasses().get(NAME), NAME + " not found");

        Encass encass = bundle.getEncasses().get(NAME);
        assertNotNull(encass.getGuid());
        assertNotNull(encass.getArguments());
        assertEquals(encass.getPolicy(), POLICY_PATH);
        assertFalse(encass.getArguments().isEmpty());
        assertEquals(2, encass.getArguments().size());
        encass.getArguments().forEach(e -> assertTrue((e.getName().equals("hello") && e.getType().equals("string")) ||
                (e.getName().equals("hello-again") && e.getType().equals("message"))));
        assertNotNull(encass.getResults());
        assertFalse(encass.getResults().isEmpty());
        assertEquals(2, encass.getResults().size());
        encass.getResults().forEach(e -> assertTrue((e.getName().equals("goodbye") && e.getType().equals("string")) ||
                (e.getName().equals("goodbye-again") && e.getType().equals("message"))));
        assertEquals(5, encass.getProperties().size());
        assertEquals("policyLogic", encass.getProperties().get(PALETTE_FOLDER));
        assertEquals("OversizedElement16.gif", encass.getProperties().get(PALETTE_ICON_RESOURCE_NAME));
        assertEquals("false", encass.getProperties().get(ALLOW_TRACING));
        assertEquals("some description", encass.getProperties().get(DESCRIPTION));
        assertEquals("false", encass.getProperties().get(PASS_METRICS_TO_PARENT));
    }

}