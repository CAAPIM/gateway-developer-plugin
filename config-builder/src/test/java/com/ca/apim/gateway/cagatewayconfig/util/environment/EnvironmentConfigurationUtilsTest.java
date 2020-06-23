package com.ca.apim.gateway.cagatewayconfig.util.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.IdentityProviderLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoader;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderBase;
import com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderRegistry;
import com.ca.apim.gateway.cagatewayconfig.environment.MissingEnvironmentException;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reflections.Reflections;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.createEntityLoader;
import static com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils.tryInferContentTypeFromValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Extensions({@ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class)})
class EnvironmentConfigurationUtilsTest {

    private TemporaryFolder rootProjectDir;
    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;
    @Mock
    private EntityTypeRegistry entityTypeRegistry;
    @Mock
    private EntityLoaderRegistry entityLoaderRegistry;
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

    @BeforeEach
    void setUp(TemporaryFolder rootProjectDir) {
        jsonTools = new JsonTools(fileUtils);
        this.rootProjectDir = rootProjectDir;
    }

    @Test
    void inferContentYaml() {
        assertEquals(JsonTools.YAML, tryInferContentTypeFromValue("test1:1\ntest1:2"));
    }

    @Test
    void inferContentJson() {
        assertEquals(JsonTools.JSON, tryInferContentTypeFromValue("{ 'test1': 1, 'test2': 2 }"));
    }

    @Test
    void inferContentJsonArray() {
        assertEquals(JsonTools.JSON, tryInferContentTypeFromValue("[ { 'test1': 1, 'test2': 2 }, { 'test3': 3, 'test4': 4 } ]"));
    }

    @Test
    void inferContentXMLUnsupported() {
        assertThrows(MissingEnvironmentException.class, () -> tryInferContentTypeFromValue("<test1>1</test1><test2>2</test2>"));
    }

    @Test
    void testLoadConfigFolder() throws IOException {
        mockEntityLoader();
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "identity-providers.yml");
        Files.touch(identityProvidersFile);
        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(yml.getBytes(Charset.forName("UTF-8"))));
        EntityTypeRegistry entityTypeRegistry = new EntityTypeRegistry(new Reflections());
        EnvironmentConfigurationUtils environmentConfigurationUtils = new EnvironmentConfigurationUtils(JsonTools.INSTANCE, entityLoaderRegistry, JsonFileUtils.INSTANCE, entityTypeRegistry);
        Map<String, String> map = environmentConfigurationUtils.loadConfigFolder(configFolder);
        Assert.assertNotNull(map.get("ENV.IDENTITY_PROVIDER.simple ldap"));
    }

    private void mockEntityLoader() throws IOException {
        EntityUtils.GatewayEntityInfo gatewayEntityInfo = EntityUtils.createEntityInfo(IdentityProvider.class);
        when(entityLoaderRegistry.getLoader("IDENTITY_PROVIDER")).thenReturn(new EntityLoaderBase<GatewayEntity>(jsonTools, new IdGenerator()) {
            @Override
            public String getEntityType() {
                return EntityTypes.ID_PROVIDER_CONFIG_TYPE;
            }

            @Override
            protected Class<GatewayEntity> getBeanClass() {
                return gatewayEntityInfo.getEntityClass();
            }

            @Override
            protected String getFileName() {
                return gatewayEntityInfo.getFileName();
            }

            @Override
            protected void putToBundle(Bundle bundle, @NotNull Map<String, GatewayEntity> entitiesMap) {
                bundle.getEntities(gatewayEntityInfo.getEntityClass()).putAll(entitiesMap);
            }
        });
    }

    @Test
    void testParseEnvironmentValues() throws IOException {
        mockEntityLoader();
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "identity-providers.yml");
        Files.touch(identityProvidersFile);
        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(yml.getBytes(Charset.forName("UTF-8"))));
        EntityTypeRegistry entityTypeRegistry = new EntityTypeRegistry(new Reflections());
        EnvironmentConfigurationUtils environmentConfigurationUtils = new EnvironmentConfigurationUtils(JsonTools.INSTANCE, entityLoaderRegistry, JsonFileUtils.INSTANCE, entityTypeRegistry);
        Map environmentConfig = new HashMap();
        environmentConfig.put("IDENTITY_PROVIDER", identityProvidersFile);

        Map<String, String> map = environmentConfigurationUtils.parseEnvironmentValues(environmentConfig);
        Assert.assertNotNull(map.get("ENV.IDENTITY_PROVIDER.simple ldap"));
    }
}