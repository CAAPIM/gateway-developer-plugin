/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.BindOnlyLdapIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.FederatedIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.IdentityProvider;
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
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

/**
 * Created by chaoy01 on 2018-08-17.
 */
@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class IdentityProviderLoaderTest {

    private TemporaryFolder rootProjectDir;
    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;

    @BeforeEach
    void setUp(TemporaryFolder rootProjectDir) {
        jsonTools = new JsonTools(fileUtils);
        this.rootProjectDir = rootProjectDir;
    }

    @Test
    void loadBindOnlyLdapJSON() throws IOException {
        final IdentityProviderLoader identityProviderLoader = new IdentityProviderLoader(jsonTools);
        final String json = "{\n" +
                "  \"simple ldap\": {\n" +
                "    \"type\" : \"BIND_ONLY_LDAP\",\n" +
                "    \"properties\": {\n" +
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
                "}\n";
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "identity-providers.json");
        Files.touch(identityProvidersFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        final Bundle bundle = new Bundle();
        identityProviderLoader.load(bundle, rootProjectDir.getRoot());
        verifySimpleLdap(bundle);
    }

    @Test
    void loadBindOnlyLdapYml() throws IOException {
        final IdentityProviderLoader identityProviderLoader = new IdentityProviderLoader(jsonTools);
        final String yml = "  simple ldap:\n" +
                "    type: BIND_ONLY_LDAP\n" +
                "    properties:\n" +
                "      key1: \"value1\"\n" +
                "      key2: \"value2\"\n" +
                "    identityProviderDetail:\n" +
                "      serverUrls:\n" +
                "        - ldap://host:port\n" +
                "        - ldap://host:port2\n" +
                "      useSslClientAuthentication: false\n" +
                "      bindPatternPrefix: somePrefix\n" +
                "      bindPatternSuffix: someSuffix";
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "identity-providers.yml");
        Files.touch(identityProvidersFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(yml.getBytes(Charset.forName("UTF-8"))));

        final Bundle bundle = new Bundle();
        identityProviderLoader.load(bundle, rootProjectDir.getRoot());
        verifySimpleLdap(bundle);
    }

    @Test
    void loadIncorrectTypeForBoolean() throws IOException {
        final IdentityProviderLoader identityProviderLoader = new IdentityProviderLoader(jsonTools);
        final String json = "{\n" +
                "  \"simple ldap\": {\n" +
                "    \"type\" : \"BIND_ONLY_LDAP\",\n" +
                "    \"properties\": {\n" +
                "      \"key1\":\"value1\",\n" +
                "      \"key2\":\"value2\"\n" +
                "    },\n" +
                "    \"identityProviderDetail\" : {\n" +
                "      \"serverUrls\": [\n" +
                "        \"ldap://host:port\",\n" +
                "        \"ldap://host:port2\"\n" +
                "      ],\n" +
                "      \"useSslClientAuthentication\":NOT_A_BOOLEAN,\n" +
                "      \"bindPatternPrefix\": \"somePrefix\",\n" +
                "      \"bindPatternSuffix\": \"someSuffix\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "identity-providers.json");
        Files.touch(identityProvidersFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        final Bundle bundle = new Bundle();
        Assertions.assertThrows(JsonToolsException.class, () -> identityProviderLoader.load(bundle, rootProjectDir.getRoot()));
    }

    @Test
    void loadFedIdWithMultipleCertRefs() throws IOException {
        final IdentityProviderLoader identityProviderLoader = new IdentityProviderLoader(jsonTools);
        final String yml = "fed ID:\n" +
                "  type: FEDERATED\n" +
                "  properties:\n" +
                "    certificateValidation: Validate\n" +
                "    enableCredentialType.saml: true\n" +
                "    enableCredentialType.x509: true\n" +
                "  identityProviderDetail:\n" +
                "    certificateReferences:\n" +
                "    - 355cd79a5fc6a2a508c45e6e875ce5ab\n" +
                "    - 355cd79a5fc6a2a508c45e6e875ce628";
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "identity-providers.yml");
        Files.touch(identityProvidersFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(yml.getBytes(Charset.forName("UTF-8"))));

        final Bundle bundle = new Bundle();
        identityProviderLoader.load(bundle, rootProjectDir.getRoot());

        assertEquals(1, bundle.getIdentityProviders().size());
        final IdentityProvider identityProvider = bundle.getIdentityProviders().get("fed ID");
        assertEquals(3, identityProvider.getProperties().size());
        assertEquals(IdentityProvider.Type.FEDERATED, identityProvider.getType());
        assertTrue(identityProvider.getIdentityProviderDetail() instanceof FederatedIdentityProviderDetail);

        final FederatedIdentityProviderDetail identityProviderDetail = (FederatedIdentityProviderDetail) identityProvider.getIdentityProviderDetail();
        assertEquals(2, identityProviderDetail.getCertificateReferences().size());
    }

    @Test
    void loadFedIdWithNoCertRefs() throws IOException {
        final IdentityProviderLoader identityProviderLoader = new IdentityProviderLoader(jsonTools);
        final String yml = "fed_no_cert:\n" +
                "  type: \"FEDERATED\"\n" +
                "  properties:\n" +
                "    enableCredentialType.saml: false\n" +
                "    enableCredentialType.x509: true";
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "identity-providers.yml");
        Files.touch(identityProvidersFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(yml.getBytes(Charset.forName("UTF-8"))));

        final Bundle bundle = new Bundle();
        identityProviderLoader.load(bundle, rootProjectDir.getRoot());

        assertEquals(1, bundle.getIdentityProviders().size());
        final IdentityProvider identityProvider = bundle.getIdentityProviders().get("fed_no_cert");
        assertEquals(2, identityProvider.getProperties().size());
        assertEquals(IdentityProvider.Type.FEDERATED, identityProvider.getType());
        assertNull(identityProvider.getIdentityProviderDetail());
    }

    private void verifySimpleLdap(Bundle bundle) {
        assertEquals(1, bundle.getIdentityProviders().size());

        final IdentityProvider identityProvider = bundle.getIdentityProviders().get("simple ldap");
        assertEquals("Two items in properties", 2, identityProvider.getProperties().size());
        assertEquals("Type is BIND_ONLY_LDAP", IdentityProvider.Type.BIND_ONLY_LDAP, identityProvider.getType());
        assertTrue("IdentityProviderDetail deserialized to BindOnlyLdapIdentityProviderDetail", identityProvider.getIdentityProviderDetail() instanceof BindOnlyLdapIdentityProviderDetail);

        final BindOnlyLdapIdentityProviderDetail identityProviderDetail = (BindOnlyLdapIdentityProviderDetail) identityProvider.getIdentityProviderDetail();
        assertEquals("Two serverUrls", 2, identityProviderDetail.getServerUrls().size());
        assertFalse("Not using ssl client authentication", identityProviderDetail.isUseSslClientAuthentication());
        assertEquals("Check bindPatternPrefix", "somePrefix", identityProviderDetail.getBindPatternPrefix());
        assertEquals("Check bindPatternSuffix", "someSuffix", identityProviderDetail.getBindPatternSuffix());
    }

}