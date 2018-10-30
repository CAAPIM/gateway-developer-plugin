/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
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
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class GlobalPolicyLoaderTest {

    private static final String NAME = "[Global]";

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
        String yaml = "'" + NAME + "':\n" +
                "  path: \"gateway-solution/global-policies/" + NAME + ".xml\"\n" +
                "  tag: \"message-completed\"";
        load(yaml, "yml", false);
    }

    @Test
    void loadJson() throws IOException {
        String json = "{\n" +
                "  \"" + NAME + "\": {\n" +
                "    \"path\": \"gateway-solution/global-policies/" + NAME + ".xml\",\n" +
                "    \"tag\": \"message-completed\"\n" +
                "  }\n" +
                "}";
        load(json, "json", false);
    }

    @Test
    void loadMalformedYaml() throws IOException {
        String yaml = "'" + NAME + "':\n" +
                "  path \"gateway-solution/global-policies/" + NAME + ".xml\"\n" +
                "  tag \"message-completed\"";
        load(yaml, "yml", true);
    }

    @Test
    void loadMalformedJson() throws IOException {
        String json = "{\n" +
                "  \"" + NAME + "\": {\n" +
                "    \"path\": \"gateway-solution/global-policies/" + NAME + ".xml\",\n" +
                "    \"tag\": \"message-completed\"\n" +
                "  \n" +
                "";
        load(json, "json", true);
    }

    private void load(String content, String fileTyoe, boolean expectException) throws IOException {
        GlobalPolicyLoader loader = new GlobalPolicyLoader(jsonTools);
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "global-policies." + fileTyoe);
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

    private static void load(GlobalPolicyLoader loader, Bundle bundle, TemporaryFolder rootProjectDir) {
        loader.load(bundle, rootProjectDir.getRoot());
    }

    private static void check(Bundle bundle) {
        assertFalse(bundle.getPolicies().isEmpty());
        assertEquals(1, bundle.getPolicies().size());
        
        String policyPath = Paths.get("gateway-solution", "global-policies", NAME + ".xml").toString();
        assertNotNull(bundle.getPolicies().get(policyPath));

        Policy policy = bundle.getPolicies().get(policyPath);
        assertEquals(policyPath, policy.getPath());
        assertEquals("message-completed", policy.getTag());
    }
    

}