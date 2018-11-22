/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.AuditPolicy;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonToolsException;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class AuditPolicyLoaderTest {

    private static final String NAME = "[Audit]";

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
                "  path: \"gateway-solution/audit-policies/" + NAME + "\"\n" +
                "  tag: \"audit-sink\"";
        load(yaml, "yml", null);
    }

    @Test
    void loadJson() throws IOException {
        String json = "{\n" +
                "  \"" + NAME + "\": {\n" +
                "    \"path\": \"gateway-solution/audit-policies/" + NAME + "\",\n" +
                "    \"tag\": \"audit-sink\"\n" +
                "  }\n" +
                "}";
        load(json, "json", null);
    }

    @Test
    void loadMalformedYaml() throws IOException {
        String yaml = "'" + NAME + "':\n" +
                "  path \"gateway-solution/audit-policies/" + NAME + "\"\n" +
                "  tag \"audit-sink\"";
        load(yaml, "yml", JsonToolsException.class);
    }

    @Test
    void loadMalformedJson() throws IOException {
        String json = "{\n" +
                "  \"" + NAME + "\": {\n" +
                "    \"path\": \"gateway-solution/audit-policies/" + NAME + "\",\n" +
                "    \"tag\": \"audit-sink\"\n" +
                "  \n" +
                "";
        load(json, "json", JsonToolsException.class);
    }

    @Test
    void loadRepeatedTag() throws IOException {
        String yaml = "'[Internal Audit Sink Policy]':\n" +
                "  path: \"gateway-solution/audit-policies/[Internal Audit Sink Policy]\"\n" +
                "  tag: \"audit-sink\"\n" +
                "'[Internal Audit Sink Policy 1]':\n" +
                "  path: \"gateway-solution/audit-policies/[Internal Audit Sink Policy 1]\"\n" +
                "  tag: \"audit-sink\"";
        load(yaml, "yml", ConfigLoadException.class);
    }

    private void load(String content, String fileType, Class<? extends Exception> expectException) throws IOException {
        EntityLoader loader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(AuditPolicy.class));
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "audit-policies." + fileType);
        Files.touch(identityProvidersFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        if (expectException != null) {
            assertThrows(expectException, () -> load(loader, bundle, rootProjectDir));
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
        assertFalse(bundle.getPolicies().isEmpty());
        assertEquals(1, bundle.getPolicies().size());

        String policyPath = PathUtils.unixPath("gateway-solution", "audit-policies", NAME);
        assertNotNull(bundle.getPolicies().get(policyPath));

        Policy policy = bundle.getPolicies().get(policyPath);
        assertEquals(policyPath, policy.getPath());
        assertEquals("audit-sink", policy.getTag());
    }
}