/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.beans.PolicyBackedServiceOperation;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class PolicyBackedServiceLoaderTest {
    
    private static final String NAME = "pbs";

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
                "  interfaceName: \"com.l7tech.objectmodel.polback.BackgroundTask\"\n" +
                "  operations:\n" +
                "  - policy: \"gateway-solution/policy-backed-service/a-policy-backed-service.xml\"\n" +
                "    operationName: \"run\"";
        load(yaml, "yml", false);
    }

    @Test
    void loadJson() throws IOException {
        String json = "{\n" +
                "  \"" + NAME + "\" : {\n" +
                "      \"interfaceName\": \"com.l7tech.objectmodel.polback.BackgroundTask\",\n" +
                "      \"operations\": [\n" +
                "         {\n" +
                "            \"policy\": \"gateway-solution/policy-backed-service/a-policy-backed-service.xml\",\n" +
                "            \"operationName\": \"run\"\n" +
                "         }\n" +
                "      ]\n" +
                "   }\n" +
                "}";
        load(json, "json", false);
    }

    @Test
    void loadMalformedYaml() throws IOException {
        String yaml = NAME + ":\n" +
                "  interfaceName \"com.l7tech.objectmodel.polback.BackgroundTask\"\n" +
                "  operations:\n" +
                "  - policy: \"gateway-solution/policy-backed-service/a-policy-backed-service.xml\"\n" +
                "    operationName: \"run\"";
        load(yaml, "yml", true);
    }

    @Test
    void loadMalformedJson() throws IOException {
        String json = "{\n" +
                "  \"" + NAME + "\" : {\n" +
                "      \"interfaceName\": \"com.l7tech.objectmodel.polback.BackgroundTask\",\n" +
                "      \"operations\": [\n" +
                "         {\n" +
                "            \"policy\": \"gateway-solution/policy-backed-service/a-policy-backed-service.xml\",\n" +
                "            \"operationName\": \"run\"\n" +
                "         }\n" +
                "      ]\n" +
                "   \n" +
                "";
        load(json, "json", true);
    }

    private void load(String content, String fileTyoe, boolean expectException) throws IOException {
        PolicyBackedServiceLoader loader = new PolicyBackedServiceLoader(jsonTools);
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "policy-backed-services." + fileTyoe);
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

    private static void load(PolicyBackedServiceLoader loader, Bundle bundle, TemporaryFolder rootProjectDir) {
        loader.load(bundle, rootProjectDir.getRoot());
    }

    private static void check(Bundle bundle) {
        assertFalse(bundle.getPolicyBackedServices().isEmpty(), "No policy backed services loaded");
        assertEquals(1, bundle.getPolicyBackedServices().size(), () -> "Expected 1 policy backed service, found " + bundle.getPolicyBackedServices().size());
        assertNotNull(bundle.getPolicyBackedServices().get(NAME), "pbs not found");

        PolicyBackedService pbs = bundle.getPolicyBackedServices().get(NAME);
        assertEquals("com.l7tech.objectmodel.polback.BackgroundTask", pbs.getInterfaceName());
        assertNotNull(pbs.getOperations());
        assertFalse(pbs.getOperations().isEmpty());
        assertEquals(1, pbs.getOperations().size());
        PolicyBackedServiceOperation operation = pbs.getOperations().iterator().next();
        assertEquals("run", operation.getOperationName());
        assertEquals("gateway-solution/policy-backed-service/a-policy-backed-service.xml", operation.getPolicy());
    }

}