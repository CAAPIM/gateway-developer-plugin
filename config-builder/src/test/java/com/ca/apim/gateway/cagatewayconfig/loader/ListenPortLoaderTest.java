/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.ClientAuthentication;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.Feature;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.ListenPortTlsSettings;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonToolsException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class ListenPortLoaderTest {

    private static final String CUSTOM_HTTPS_PORT = "Custom HTTPS Port";

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
    void loadListenPortsYaml() throws IOException {
        String yaml = "Custom HTTPS Port:\n" +
                "    protocol: \"HTTPS\"\n" +
                "    port: 12345\n" +
                "    enabledFeatures:\n" +
                "    - \"Published service message input\"\n" +
                "    tlsSettings:\n" +
                "      clientAuthentication: \"REQUIRED\"\n" +
                "      enabledVersions:\n" +
                "      - \"TLSv1.2\"\n" +
                "      enabledCipherSuites:\n" +
                "      - \"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "      - \"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384\"\n" +
                "      - \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\"\n" +
                "      properties:\n" +
                "        usesTLS: true\n" +
                "    properties:\n" +
                "      threadPoolSize: \"20\"";
        loadListenPort(yaml, "yml", false);
    }

    @Test
    void loadListenPortsJson() throws IOException {
        String json = "{\n" +
                "  \"Custom HTTPS Port\" : {\n" +
                "      \"protocol\" : \"HTTPS\",\n" +
                "      \"port\" : 12345,\n" +
                "      \"enabledFeatures\" : [ \"Published service message input\" ],\n" +
                "      \"tlsSettings\" : {\n" +
                "        \"clientAuthentication\" : \"REQUIRED\",\n" +
                "        \"enabledVersions\" : [ \"TLSv1.2\" ],\n" +
                "        \"enabledCipherSuites\" : [ \"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384\", \"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384\", \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\" ],\n" +
                "        \"properties\" : {\n" +
                "          \"usesTLS\" : true\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\" : { \n" +
                "         \"threadPoolSize\" : \"20\"\n" +
                "      }\n" +
                "    }\n" +
                "}";
        loadListenPort(json, "json", false);
    }

    @Test
    void loadListenPortsInvalidPortYaml() throws IOException {
        String yaml = "Custom HTTPS Port:\n" +
                "    protocol: \"HTTPS\"\n" +
                "    port: \"AAAA1\"\n" +
                "    enabledFeatures:\n" +
                "    - \"Published service message input\"\n" +
                "    tlsSettings:\n" +
                "      clientAuthentication: \"REQUIRED\"\n" +
                "      enabledVersions:\n" +
                "      - \"TLSv1.2\"\n" +
                "      enabledCipherSuites:\n" +
                "      - \"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "      - \"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384\"\n" +
                "      - \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\"\n" +
                "      properties:\n" +
                "        usesTLS: true\n" +
                "    properties:\n" +
                "      threadPoolSize: \"20\"";
        loadListenPort(yaml, "yml", true);
    }

    @Test
    void loadListenPortsInvalidPortJson() throws IOException {
        String json = "{\n" +
                "  \"Custom HTTPS Port\" : {\n" +
                "      \"protocol\" : \"HTTPS\",\n" +
                "      \"port\" : \"BBBB1\",\n" +
                "      \"enabledFeatures\" : [ \"Published service message input\" ],\n" +
                "      \"tlsSettings\" : {\n" +
                "        \"clientAuthentication\" : \"REQUIRED\",\n" +
                "        \"enabledVersions\" : [ \"TLSv1.2\" ],\n" +
                "        \"enabledCipherSuites\" : [ \"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384\", \"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384\", \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\" ],\n" +
                "        \"properties\" : {\n" +
                "          \"usesTLS\" : true\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\" : { \n" +
                "         \"threadPoolSize\" : \"20\"\n" +
                "      }\n" +
                "    }\n" +
                "}";
        loadListenPort(json, "json", true);
    }

    @Test
    void loadListenPortsMalformedYaml() throws IOException {
        String yaml = "Custom HTTPS Port:\n" +
                "    protocol: \"HTTPS\"\n" +
                "    port: \"12345\"\n" +
                "    enabledFeatures:\n" +
                "    - \"Published service message input\"\n" +
                "    tlsSettings:\n" +
                "      clientAuthentication REQUIRED\n" +
                "      enabledVersions:\n" +
                "      - \"TLSv1.2\"\n" +
                "      enabledCipherSuites:\n" +
                "      - \"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "      - \"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384\"\n" +
                "      - \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\"\n" +
                "      properties:\n" +
                "        usesTLS: true\n" +
                "    properties:\n" +
                "      threadPoolSize: \"20\"";
        loadListenPort(yaml, "yml", true);
    }

    @Test
    void loadListenPortsMalformedJson() throws IOException {
        String json = "{\n" +
                "  \"Custom HTTPS Port\" : {\n" +
                "      \"protocol\" : \"HTTPS\",\n" +
                "      \"port\" : \"12345\",\n" +
                "      \"enabledFeatures\" : [ \"Published service message input\" ],\n" +
                "      \"tlsSettings\" : {\n" +
                "        \"clientAuthentication REQUIRED,\n" +
                "        \"enabledVersions\" : [ \"TLSv1.2\" ],\n" +
                "        \"enabledCipherSuites\" : [ \"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384\", \"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384\", \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\" ],\n" +
                "        \"properties\" : {\n" +
                "          \"usesTLS\" : true\n" +
                "        }\n" +
                "      },\n" +
                "      \"properties\" : { \n" +
                "         \"threadPoolSize\" : \"20\"\n" +
                "      }\n" +
                "    }\n" +
                "}";
        loadListenPort(json, "json", true);
    }

    private void loadListenPort(String content, String fileTyoe, boolean expectException) throws IOException {
        ListenPortLoader loader = new ListenPortLoader(jsonTools);
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "listen-ports." + fileTyoe);
        Files.touch(identityProvidersFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        if (expectException) {
            assertThrows(JsonToolsException.class, () -> loadListenPorts(loader, bundle, rootProjectDir));
            return;
        } else {
            loadListenPorts(loader, bundle, rootProjectDir);
        }
        checkListenPort(bundle);
    }

    private static void loadListenPorts(ListenPortLoader loader, Bundle bundle, TemporaryFolder rootProjectDir) {
        loader.load(bundle, rootProjectDir.getRoot());
    }

    private static void checkListenPort(Bundle bundle) {
        assertFalse(bundle.getListenPorts().isEmpty(), "No ports loaded");
        assertEquals(1, bundle.getListenPorts().size(), () -> "Expected 1 port, found " + bundle.getListenPorts().size());
        assertNotNull(bundle.getListenPorts().get(CUSTOM_HTTPS_PORT), "Custom HTTPS Port not found");

        ListenPort listenPort = bundle.getListenPorts().get(CUSTOM_HTTPS_PORT);
        Assertions.assertEquals(ListenPort.PROTOCOL_HTTPS, listenPort.getProtocol(), "Protocol is not HTTPS");
        assertEquals(12345, listenPort.getPort(), "Port is not 12345");
        assertFalse(listenPort.getEnabledFeatures().isEmpty(), "Feature list is empty");
        assertEquals(1, listenPort.getEnabledFeatures().size(), "Feature list contains more than 1 feature");
        Assertions.assertTrue(listenPort.getEnabledFeatures().contains(Feature.MESSAGE_INPUT.getDescription()), "Feature list does not contain " + Feature.MESSAGE_INPUT.getDescription());
        assertNotNull(listenPort.getTlsSettings(), "TLS Settings not loaded");
        Assertions.assertEquals(listenPort.getTlsSettings().getClientAuthentication(), ClientAuthentication.REQUIRED, "Client authentication is not REQUIRED");
        assertFalse(listenPort.getTlsSettings().getEnabledVersions().isEmpty(), "TLS Enabled Versions not loaded");
        assertEquals(listenPort.getTlsSettings().getEnabledVersions().size(), 1, "More than 1 tls version enabled");
        assertTrue(listenPort.getTlsSettings().getEnabledVersions().contains(ListenPortTlsSettings.TLSV12), "TLS Version enabled is not 1.2");
        assertFalse(listenPort.getTlsSettings().getEnabledCipherSuites().isEmpty(), "Enabled Cipher Suites not loaded");
        assertEquals(listenPort.getTlsSettings().getEnabledCipherSuites().size(), 3, "More than 3 cipher suites enabled");
        assertTrue(listenPort.getTlsSettings().getEnabledCipherSuites().containsAll(asList("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384")), "Unexpected Cipher Suites enabled");
        assertPropertiesContent(ImmutableMap.of("usesTLS", true), listenPort.getTlsSettings().getProperties());
        assertPropertiesContent(ImmutableMap.of("threadPoolSize", "20"), listenPort.getProperties());
    }

}
