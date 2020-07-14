/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadingOperation;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.EntityBundleLoader;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import com.google.common.collect.ImmutableMap;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import static com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.DEFAULT_HTTPS_8443;
import static com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.DEFAULT_HTTP_8080;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.junit.jupiter.api.Assertions.*;

class EnvironmentCreatorApplicationTest {

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testNoPropertiesAndNoBundles(TemporaryFolder temporaryFolder) throws URISyntaxException, IOException {
        File testTemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "no-bundles");
        File testDetemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "detemplatized-bundles");
        File keyStoreFolder = new File(temporaryFolder.getRoot(), "keystore");
        File envFolder = new File(temporaryFolder.getRoot(), "config");
        File privateKeyFolder = new File(envFolder, "privateKeys");

        assertTrue(testDetemplatizedBundlesFolder.mkdirs());

        copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("no-bundles")).toURI()), testTemplatizedBundlesFolder);

        ImmutableMap<String, String> environmentProperties = ImmutableMap.of();

        new EnvironmentCreatorApplication(environmentProperties, testTemplatizedBundlesFolder.getPath(), testDetemplatizedBundlesFolder.getPath(), keyStoreFolder.getPath(), privateKeyFolder.getPath(), envFolder.getPath()).run();

        File environmentBundle = new File(testDetemplatizedBundlesFolder, "_0_env.req.install.bundle");

        assertTrue(environmentBundle.exists());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testEnvironmentPropertiesNotFoundInBundle(TemporaryFolder temporaryFolder) throws URISyntaxException, IOException {
        File testTemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "templatized-bundles");
        File testDetemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "detemplatized-bundles");
        File keyStoreFolder = new File(temporaryFolder.getRoot(), "keystore");
        File envFolder = new File(temporaryFolder.getRoot(), "config");
        File privateKeyFolder = new File(envFolder, "privateKeys");

        assertTrue(testDetemplatizedBundlesFolder.mkdirs());

        copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("templatized-bundles")).toURI()), testTemplatizedBundlesFolder);

        ImmutableMap<String, String> environmentProperties = ImmutableMap.<String, String>builder()
                .put("ENV.SERVICE_PROPERTY.my-gateway-api.myEnvironmentVariable", "my-service-property-value")
                .put("ENV.CONTEXT_VARIABLE_PROPERTY.anotherEnvVar", "context-variable-value")
                .put("ENV.SERVICE_PROPERTY.my-gateway-api.environmentVariableNotInBundle", "my-service-property-value")
                .put("ENV.CONTEXT_VARIABLE_PROPERTY.environmentVariableNotInBundle", "context-variable-value")
                .build();

        new EnvironmentCreatorApplication(environmentProperties, testTemplatizedBundlesFolder.getPath(), testDetemplatizedBundlesFolder.getPath(), keyStoreFolder.getPath(), privateKeyFolder.getPath(), envFolder.getPath()).run();
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testEnvironmentProperties(TemporaryFolder temporaryFolder) throws URISyntaxException, IOException {
        File testTemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "templatized-bundles");
        File testDetemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "detemplatized-bundles");
        File keyStoreFolder = new File(temporaryFolder.getRoot(), "keystore");
        File envFolder = new File(temporaryFolder.getRoot(), "config");
        File privateKeyFolder = new File(envFolder, "privateKeys");

        assertTrue(testDetemplatizedBundlesFolder.mkdirs());

        copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("templatized-bundles")).toURI()), testTemplatizedBundlesFolder);

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
                .put("ENV.SERVICE_PROPERTY.my-gateway-api.myEnvironmentVariable", "my-service-property-value")
                .put("ENV.CONTEXT_VARIABLE_PROPERTY.anotherEnvVar", "context-variable-value")
                .build();

        new EnvironmentCreatorApplication(
                environmentProperties,
                testTemplatizedBundlesFolder.getPath(),
                testDetemplatizedBundlesFolder.getPath(),
                keyStoreFolder.getPath(),
                privateKeyFolder.getPath(),
                envFolder.getPath()).run();

        assertEnvironment(testDetemplatizedBundlesFolder);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testEnvironmentFromConfigFolder(TemporaryFolder temporaryFolder) throws URISyntaxException, IOException {
        File testTemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "templatized-bundles");
        File testDetemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "detemplatized-bundles");
        File keyStoreFolder = new File(temporaryFolder.getRoot(), "keystore");
        File envFolder = new File(temporaryFolder.getRoot(), "config");
        File privateKeyFolder = new File(envFolder, "privateKeys");

        assertTrue(testDetemplatizedBundlesFolder.mkdirs());

        copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("templatized-bundles")).toURI()), testTemplatizedBundlesFolder);
        writeStringToFile(
                new File(envFolder, "identity-providers.json"),
                "{\n" +
                        "  \"simple ldap\": {\n" +
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
                        "  }\n" +
                        "}",
                defaultCharset()
        );
        writeStringToFile(
                new File(envFolder, "listen-ports.json"),
                "{\n" +
                        "  \"Custom HTTPS Port\": {\n" +
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
                        "    } \n" +
                        "}",
                defaultCharset()
        );
        writeStringToFile(
                new File(envFolder, "jdbc-connections.json"),
                "{\n" +
                        "  \"my-jdbc\": {\n" +
                        "    \"driverClass\" : \"com.mysql.jdbc.Driver\",\n" +
                        "    \"jdbcUrl\" : \"jdbc:mysql://localhost:3306/ssg\",\n" +
                        "    \"user\" : \"gateway\",\n" +
                        "    \"passwordRef\" : \"gateway\",\n" +
                        "    \"minimumPoolSize\" : 3,\n" +
                        "    \"maximumPoolSize\" : 15,\n" +
                        "    \"properties\" : {\n" +
                        "      \"EnableCancelTimeout\" : \"true\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                defaultCharset()
        );
        writeStringToFile(
                new File(envFolder, "trusted-certs.json"),
                "{\n" +
                        "  \"my-cert\": {\n" +
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
                        "  }\n" +
                        "}",
                defaultCharset()
        );
        writeStringToFile(
                new File(envFolder, "stored-passwords.properties"),
                "my_password=my_secret_password",
                defaultCharset()
        );
        writeStringToFile(
                new File(envFolder, "service-env.properties"),
                "my-gateway-api.myEnvironmentVariable=my-service-property-value",
                defaultCharset()
        );
        writeStringToFile(new File( envFolder, "context-env.properties"), "anotherEnvVar=context-variable-value", defaultCharset());

        new EnvironmentCreatorApplication(
                System.getenv(),
                testTemplatizedBundlesFolder.getPath(),
                testDetemplatizedBundlesFolder.getPath(),
                keyStoreFolder.getPath(),
                privateKeyFolder.getPath(),
                temporaryFolder.getRoot().getPath()
        ).run();

        assertEnvironment(testDetemplatizedBundlesFolder);
    }

    private static void assertEnvironment(File testDetemplatizedBundlesFolder) {
        File environmentBundleFile = new File(testDetemplatizedBundlesFolder, "_0_env.req.install.bundle");
        assertTrue(environmentBundleFile.exists());

        EntityBundleLoader bundleLoader = InjectionRegistry.getInjector().getInstance(EntityBundleLoader.class);
        Bundle environmentBundle = bundleLoader.load(environmentBundleFile, BundleLoadingOperation.EXPORT);

        assertEquals(1, environmentBundle.getIdentityProviders().size());
        assertNotNull(environmentBundle.getIdentityProviders().get("::::simple ldap"));

        assertEquals(3, environmentBundle.getListenPorts().size());
        assertNotNull(environmentBundle.getListenPorts().get("Custom HTTPS Port"));
        assertNotNull(environmentBundle.getListenPorts().get(DEFAULT_HTTP_8080));
        assertNotNull(environmentBundle.getListenPorts().get(DEFAULT_HTTPS_8443));

        assertEquals(1, environmentBundle.getJdbcConnections().size());
        assertNotNull(environmentBundle.getJdbcConnections().get("::::my-jdbc"));

        assertEquals(1, environmentBundle.getTrustedCerts().size());
        assertNotNull(environmentBundle.getTrustedCerts().get("my-cert"));

        assertEquals(1, environmentBundle.getStoredPasswords().size());
        assertNotNull(environmentBundle.getStoredPasswords().get("my_password"));

        File deploymentBundleFile = new File(testDetemplatizedBundlesFolder, "my-bundle.req.bundle");
        assertTrue(deploymentBundleFile.exists());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testEnvironmentFromConfigFolderMissingValues(TemporaryFolder temporaryFolder) throws URISyntaxException, IOException {
        File testTemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "templatized-bundles");
        File testDetemplatizedBundlesFolder = new File(temporaryFolder.getRoot(), "detemplatized-bundles");
        File keyStoreFolder = new File(temporaryFolder.getRoot(), "keystore");
        File envFolder = new File(temporaryFolder.getRoot(), "config");
        File privateKeyFolder = new File(envFolder, "privateKeys");

        assertTrue(testDetemplatizedBundlesFolder.mkdirs());

        copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("templatized-bundles")).toURI()), testTemplatizedBundlesFolder);

        writeStringToFile(
                new File(envFolder, "service-env.properties"),
                "my-gateway-api.myEnvironmentVariable=my-service-property-value",
                defaultCharset()
        );
        writeStringToFile(new File( envFolder, "context-env.properties"), "anotherEnvVar=context-variable-value", defaultCharset());

        new EnvironmentCreatorApplication(
                System.getenv(),
                testTemplatizedBundlesFolder.getPath(),
                testDetemplatizedBundlesFolder.getPath(),
                keyStoreFolder.getPath(),
                privateKeyFolder.getPath(),
                temporaryFolder.getRoot().getPath()
        ).run();
    }
}