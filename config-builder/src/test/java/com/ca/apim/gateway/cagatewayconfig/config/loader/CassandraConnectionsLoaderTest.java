/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.CassandraConnection;
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
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.createEntityLoader;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class CassandraConnectionsLoaderTest {

    private static final String CONNECTION_NAME = "Test";

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
    void loadCassandraConnectionYaml() throws IOException {
        String yaml = CONNECTION_NAME + ":\n" +
                "  keyspace: \"Test\"\n" +
                "  contactPoint: \"Test\"\n" +
                "  port: 9042\n" +
                "  username: \"test\"\n" +
                "  storedPasswordName: \"gateway\"\n" +
                "  compression: \"LZ4\"\n" +
                "  ssl: true\n" +
                "  tlsCiphers:\n" +
                "  - \"TLS_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_256_CBC_SHA256\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA256\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  properties:\n" +
                "    keepAlive: \"true\"";
        loadCassandraConnection(yaml, "yml", false, false);
    }

    @Test
    void loadCassandraConnectionJson() throws IOException {
        String json = "{\n" +
                "  \"" + CONNECTION_NAME + "\" : {\n" +
                "                                      \"keyspace\": \"Test\",\n" +
                "                                      \"contactPoint\": \"Test\",\n" +
                "                                      \"port\": 9042,\n" +
                "                                      \"username\": \"test\",\n" +
                "                                      \"storedPasswordName\": \"gateway\",\n" +
                "                                      \"compression\": \"LZ4\",\n" +
                "                                      \"ssl\": true,\n" +
                "                                      \"tlsCiphers\": [\n" +
                "                                        \"TLS_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_RSA_WITH_AES_256_GCM_SHA384\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_256_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384\"\n" +
                "                                      ],\n" +
                "                                      \"properties\": {\n" +
                "                                        \"keepAlive\": \"true\"\n" +
                "                                      }\n" +
                "                                    }\n" +
                "}";
        loadCassandraConnection(json, "json", false, false);
    }

    @Test
    void loadSingleCassandraConnectionYaml() throws IOException {
        String yaml = CONNECTION_NAME + ":\n" +
                "  keyspace: \"Test\"\n" +
                "  contactPoint: \"Test\"\n" +
                "  port: 9042\n" +
                "  username: \"test\"\n" +
                "  storedPasswordName: \"gateway\"\n" +
                "  compression: \"LZ4\"\n" +
                "  ssl: true\n" +
                "  tlsCiphers:\n" +
                "  - \"TLS_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_256_CBC_SHA256\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA256\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  properties:\n" +
                "    keepAlive: \"true\"";
        loadCassandraConnection(yaml, "yml", false, true);
    }

    @Test
    void loadSingleCassandraConnectionJson() throws IOException {
        String json = "{\n" +
                "  \"" + CONNECTION_NAME + "\" : {\n" +
                "                                      \"keyspace\": \"Test\",\n" +
                "                                      \"contactPoint\": \"Test\",\n" +
                "                                      \"port\": 9042,\n" +
                "                                      \"username\": \"test\",\n" +
                "                                      \"storedPasswordName\": \"gateway\",\n" +
                "                                      \"compression\": \"LZ4\",\n" +
                "                                      \"ssl\": true,\n" +
                "                                      \"tlsCiphers\": [\n" +
                "                                        \"TLS_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_RSA_WITH_AES_256_GCM_SHA384\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_256_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384\"\n" +
                "                                      ],\n" +
                "                                      \"properties\": {\n" +
                "                                        \"keepAlive\": \"true\"\n" +
                "                                      }\n" +
                "                                    }\n" +
                "}";
        loadCassandraConnection(json, "json", false, true);
    }

    @Test
    void loadCassandraConnectionMalformedYaml() throws IOException {
        String yaml = CONNECTION_NAME + ":\n" +
                "  keyspace: \"Test\"\n" +
                "  contactPoint: \"Test\"\n" +
                "  port: 9042\n" +
                "  username: \"test\"\n" +
                "  storedPasswordName: \"gateway\"\n" +
                "  compression: \"LZ4\"\n" +
                "  ssl: true\n" +
                "  tlsCiphers:\n" +
                "   \"TLS_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_RSA_WITH_AES_256_CBC_SHA256\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA256\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA\"\n" +
                "  - \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256\"\n" +
                "  - \"TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384\"\n" +
                "  properties:\n" +
                "    keepAlive: \"true\"";
        loadCassandraConnection(yaml, "yml", true, false);
    }

    @Test
    void loadCassandraConnectionMalformedJson() throws IOException {
        String json = "{\n" +
                "  \"" + CONNECTION_NAME + "\" : {\n" +
                "                                      \"keyspace\": \"Test\",\n" +
                "                                      \"contactPoint\": \"Test\",\n" +
                "                                      \"port\": 9042,\n" +
                "                                      \"username\": \"test\",\n" +
                "                                      \"storedPasswordName\": \"gateway\",\n" +
                "                                      \"compression\": \"LZ4\",\n" +
                "                                      \"ssl\": true,\n" +
                "                                      \"tlsCiphers\": [\n" +
                "                                        \"TLS_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_RSA_WITH_AES_256_GCM_SHA384\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_RSA_WITH_AES_256_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA256\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_DHE_RSA_WITH_AES_128_CBC_SHA\",\n" +
                "                                        \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256\",\n" +
                "                                        \"TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384\"\n" +
                "                                      ],\n" +
                "                                      \"properties\": {\n" +
                "                                        \"keepAlive\": \"true\"\n" +
                "                                      }\n" +
                "                                    \n" +
                "  }\n";
        loadCassandraConnection(json, "json", true, false);
    }

    private void loadCassandraConnection(String content, String fileTyoe, boolean expectException, boolean single) throws IOException {
        EntityLoader loader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(CassandraConnection.class));
        final File configFolder = rootProjectDir.createDirectory("config");
        final File cassandraConnectionsFile = new File(configFolder, "cassandra-connections." + fileTyoe);
        Files.touch(cassandraConnectionsFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        if (expectException) {
            assertThrows(JsonToolsException.class, () -> loadCassandraConnections(loader, bundle, rootProjectDir));
        } else if (single) {
            assertNotNull(loader.loadSingle(CONNECTION_NAME, cassandraConnectionsFile));
        } else {
            loadCassandraConnections(loader, bundle, rootProjectDir);
            checkCassandraConnection(bundle);
        }
    }

    private static void loadCassandraConnections(EntityLoader loader, Bundle bundle, TemporaryFolder rootProjectDir) {
        loader.load(bundle, rootProjectDir.getRoot());
    }

    private static void checkCassandraConnection(Bundle bundle) {
        assertFalse(bundle.getCassandraConnections().isEmpty(), "No connections loaded");
        assertEquals(1, bundle.getCassandraConnections().size(), () -> "Expected 1 connection, found " + bundle.getCassandraConnections().size());
        assertNotNull(bundle.getCassandraConnections().get(CONNECTION_NAME), "Test not found");

        CassandraConnection cassandraConnection = bundle.getCassandraConnections().get(CONNECTION_NAME);
        assertEquals("Test", cassandraConnection.getKeyspace());
        assertEquals("Test", cassandraConnection.getContactPoint());
        assertEquals(9042, cassandraConnection.getPort().intValue());
        assertEquals("test", cassandraConnection.getUsername());
        assertEquals("gateway", cassandraConnection.getStoredPasswordName());
        assertEquals("LZ4", cassandraConnection.getCompression());
        assertEquals(true, cassandraConnection.getSsl());
        assertNotNull(cassandraConnection.getTlsCiphers());
        assertTrue(cassandraConnection.getTlsCiphers().containsAll(Stream.of("TLS_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
                "TLS_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_RSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
                "TLS_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384",
                "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                "TLS_RSA_WITH_AES_256_CBC_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
                "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
                "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
                "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384").collect(Collectors.toList())));
        assertPropertiesContent(ImmutableMap.of("keepAlive", Boolean.TRUE.toString()), cassandraConnection.getProperties());
    }
}