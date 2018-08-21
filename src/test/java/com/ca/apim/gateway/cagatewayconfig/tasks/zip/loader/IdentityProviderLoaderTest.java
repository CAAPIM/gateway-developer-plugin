package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.BindOnlyLdapIdentityProviderDetail;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.identityprovider.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonToolsException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

/**
 * Created by chaoy01 on 2018-08-17.
 */

@RunWith(MockitoJUnitRunner.class)
public class IdentityProviderLoaderTest {

    @Rule
    public final TemporaryFolder rootProjectDir = new TemporaryFolder();
    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;

    @Before
    public void setUp() throws Exception {
        jsonTools = new JsonTools(fileUtils);
    }

    @Test
    public void loadBindOnlyLdapJSON() throws IOException {
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
        final File configFolder = rootProjectDir.newFolder("config");
        final File identityProvidersFile = new File(configFolder, "identity-providers.json");
        Files.touch(identityProvidersFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        final Bundle bundle = new Bundle();
        identityProviderLoader.load(bundle, rootProjectDir.getRoot());
        verifySimpleLdap(bundle);
    }

    @Test
    public void loadBindOnlyLdapYml() throws IOException {
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
        final File configFolder = rootProjectDir.newFolder("config");
        final File identityProvidersFile = new File(configFolder, "identity-providers.yml");
        Files.touch(identityProvidersFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(yml.getBytes(Charset.forName("UTF-8"))));

        final Bundle bundle = new Bundle();
        identityProviderLoader.load(bundle, rootProjectDir.getRoot());
        verifySimpleLdap(bundle);
    }

    @Test (expected = JsonToolsException.class)
    public void loadIncorrectTypeForBoolean() throws IOException {
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
        final File configFolder = rootProjectDir.newFolder("config");
        final File identityProvidersFile = new File(configFolder, "identity-providers.json");
        Files.touch(identityProvidersFile);

        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(json.getBytes(Charset.forName("UTF-8"))));

        final Bundle bundle = new Bundle();
        identityProviderLoader.load(bundle, rootProjectDir.getRoot());
    }

    private void verifySimpleLdap(Bundle bundle) {
        assertEquals(1, bundle.getIdentityProviders().size());

        final IdentityProvider identityProvider = bundle.getIdentityProviders().get("simple ldap");
        assertTrue("Two items in properties", identityProvider.getProperties().size() == 2);
        assertTrue("Type is BIND_ONLY_LDAP", identityProvider.getType() == IdentityProvider.IdentityProviderType.BIND_ONLY_LDAP);
        assertTrue("IdentityProviderDetail deserialized to BindOnlyLdapIdentityProviderDetail", identityProvider.getIdentityProviderDetail() instanceof BindOnlyLdapIdentityProviderDetail);

        final BindOnlyLdapIdentityProviderDetail identityProviderDetail = (BindOnlyLdapIdentityProviderDetail) identityProvider.getIdentityProviderDetail();
        assertEquals("Two serverUrls", 2, identityProviderDetail.getServerUrls().size());
        assertFalse("Not using ssl client authentication", identityProviderDetail.isUseSslClientAuthentication());
        assertEquals("Check bindPatternPrefix", "somePrefix", identityProviderDetail.getBindPatternPrefix());
        assertEquals("Check bindPatternSuffix", "someSuffix", identityProviderDetail.getBindPatternSuffix());
    }

}