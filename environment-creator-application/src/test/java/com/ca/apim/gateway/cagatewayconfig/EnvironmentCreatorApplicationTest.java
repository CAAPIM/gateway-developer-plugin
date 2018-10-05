/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.DependencyBundleLoader;
import com.ca.apim.gateway.cagatewayconfig.util.injection.ConfigBuilderModule;
import com.google.common.collect.ImmutableMap;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentCreatorApplicationTest {

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testNoPropertiesAndNoBundles(TemporaryFolder temporaryFolder) throws URISyntaxException, IOException {
        File testTemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "no-bundles");
        File testDetemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "detemplatized-bundles");
        assertTrue(testDetemplatizedBundlesFolder.mkdirs());

        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("no-bundles")).toURI()), testTemplatizedBundlesFolder);

        ImmutableMap<String, String> environmentProperties = ImmutableMap.of();

        new EnvironmentCreatorApplication(environmentProperties, testTemplatizedBundlesFolder.getPath(), testDetemplatizedBundlesFolder.getPath()).run();

        File environmentBundle = new File(testDetemplatizedBundlesFolder, "_env.req.bundle");

        assertTrue(environmentBundle.exists());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testEnvironmentProperties(TemporaryFolder temporaryFolder) throws URISyntaxException, IOException {
        File testTemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "templatized-bundles");
        File testDetemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "detemplatized-bundles");
        assertTrue(testDetemplatizedBundlesFolder.mkdirs());

        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("templatized-bundles")).toURI()), testTemplatizedBundlesFolder);

        ImmutableMap<String, String> environmentProperties = ImmutableMap.<String, String>builder()
                .put(
                        "ENV.IDENTITY_PROVIDER.simple ldap", "{\n" +
                                "    \"type\" : \"BIND_ONLY_LDAP\",\n" +
                                "    \"environmentVariables\": {\n" +
                                "      \"key1\":\"value1\",\n" +
                                "      \"key2\":\"value2\"\n" +
                                "    },\n" +
                                "    \"identityProviderDetail\" : {\n" +
                                "      \"serverUrls\": [\n" +
                                "        \"ldap://host:port\",\n" +
                                "        \"ldap://host:port2\"\n" +
                                "      ],\n" +
                                "      \"useSslClientAuthentication\":false,\n" +
                                "      \"bindPatternPrefix\": \"somePrefix\",\n" +
                                "      \"bindPatternSuffix\": \"someSuffix\"\n" +
                                "    }\n" +
                                "  }")
                .put("ENV.LISTEN_PORT.Custom HTTPS Port", "{\n" +
                        "      \"protocol\" : \"HTTPS\",\n" +
                        "      \"port\" : 12345,\n" +
                        "      \"enabledFeatures\" : [ \"Published service message input\" ],\n" +
                        "      \"tlsSettings\" : {\n" +
                        "        \"clientAuthentication\" : \"REQUIRED\",\n" +
                        "        \"enabledVersions\" : [ \"TLSv1.2\" ],\n" +
                        "        \"enabledCipherSuites\" : [ \"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384\", \"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384\", \"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384\" ],\n" +
                        "        \"environmentVariables\" : {\n" +
                        "          \"usesTLS\" : true\n" +
                        "        }\n" +
                        "      },\n" +
                        "      \"environmentVariables\" : { \n" +
                        "         \"threadPoolSize\" : \"20\"\n" +
                        "      }\n" +
                        "    }")
                .put("ENV.JDBC_CONNECTION.my-jdbc", "{\n" +
                        "    \"driverClass\" : \"com.mysql.jdbc.Driver\",\n" +
                        "    \"jdbcUrl\" : \"jdbc:mysql://localhost:3306/ssg\",\n" +
                        "    \"user\" : \"gateway\",\n" +
                        "    \"passwordRef\" : \"gateway\",\n" +
                        "    \"minimumPoolSize\" : 3,\n" +
                        "    \"maximumPoolSize\" : 15,\n" +
                        "    \"properties\" : {\n" +
                        "      \"EnableCancelTimeout\" : \"true\"\n" +
                        "    }\n" +
                        "  }")
                .put("ENV.CERTIFICATE.my-cert", "{\n" +
                        "      \"verifyHostname\" : false,\n" +
                        "      \"trustedForSsl\" : true,\n" +
                        "      \"trustedAsSamlAttestingEntity\" : false,\n" +
                        "      \"trustAnchor\" : true,\n" +
                        "      \"revocationCheckingEnabled\" : true,\n" +
                        "      \"trustedForSigningClientCerts\" : true,\n" +
                        "      \"trustedForSigningServerCerts\" : true,\n" +
                        "      \"trustedAsSamlIssuer\" : false,\n" +
                        "      \"certificateData\" : {\n" +
                        "           \"issuerName\" : \"my-cert\",\n" +
                        "           \"serialNumber\" : \"123\",\n" +
                        "           \"subjectName\" : \"my-cert\",\n" +
                        "           \"encodedData\" : \"my-cert-data\"\n" +
                        "      }\n" +
                        "  }")
                .put("ENV.PASSWORD.my_password", "my_secret_password")
                .put("ENV.PROPERTY.myEnvironmentVariable", "my-service-property-value")
                .put("ENV.PROPERTY.anotherEnvVar", "context-variable-value")
                .build();

        new EnvironmentCreatorApplication(environmentProperties, testTemplatizedBundlesFolder.getPath(), testDetemplatizedBundlesFolder.getPath()).run();

        File environmentBundleFile = new File(testDetemplatizedBundlesFolder, "_env.req.bundle");
        assertTrue(environmentBundleFile.exists());
        System.out.println(new String(Files.readAllBytes(environmentBundleFile.toPath())));

        DependencyBundleLoader bundleLoader = ConfigBuilderModule.getInjector().getInstance(DependencyBundleLoader.class);
        Bundle environmentBundle = bundleLoader.load(environmentBundleFile);

        assertEquals(1, environmentBundle.getIdentityProviders().size());
        assertNotNull(environmentBundle.getIdentityProviders().get("simple ldap"));

        assertEquals(1, environmentBundle.getListenPorts().size());
        assertNotNull(environmentBundle.getListenPorts().get("Custom HTTPS Port"));

        assertEquals(1, environmentBundle.getJdbcConnections().size());
        assertNotNull(environmentBundle.getJdbcConnections().get("my-jdbc"));

        assertEquals(1, environmentBundle.getTrustedCerts().size());
        assertNotNull(environmentBundle.getTrustedCerts().get("my-cert"));

        assertEquals(1, environmentBundle.getStoredPasswords().size());
        assertNotNull(environmentBundle.getStoredPasswords().get("my_password"));

        File deploymentBundleFile = new File(testDetemplatizedBundlesFolder, "my-bundle.req.bundle");
        assertTrue(deploymentBundleFile.exists());
        System.out.println(new String(Files.readAllBytes(deploymentBundleFile.toPath())));
    }
}