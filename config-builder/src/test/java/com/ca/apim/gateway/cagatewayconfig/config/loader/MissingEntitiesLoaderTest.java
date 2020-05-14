/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JdbcConnection;
import com.ca.apim.gateway.cagatewayconfig.beans.MissingGatewayEntity;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.createEntityLoader;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class MissingEntitiesLoaderTest {

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
    void loadMissingEntitiesFromYmlConfiguration() throws IOException {
        String yaml = "Missing Encass:\n" +
                "  type: \"ENCAPSULATED_ASSERTION\"\n" +
                "  guid: \"e65db2e2-26f4-452e-922d-9fe4e8533889\"\n" +
                "Policy#1234:\n" +
                "  type: \"POLICY\"\n" +
                "  guid: \"e65db2e1-16f4-452e-922e-afe4e8533998\"\n";
        Bundle bundle = loadConfiguration(yaml);

        assertEquals(2, bundle.getMissingEntities().size());

        MissingGatewayEntity missingEntity = bundle.getMissingEntities().get("Missing Encass");
        assertNotNull(missingEntity);
        assertEquals("ENCAPSULATED_ASSERTION", missingEntity.getType());
        assertEquals("e65db2e2-26f4-452e-922d-9fe4e8533889", missingEntity.getGuid());

        missingEntity = bundle.getMissingEntities().get("Policy#1234");
        assertNotNull(missingEntity);
        assertEquals("POLICY", missingEntity.getType());
        //assertEquals("Policy#1234", missingEntity.getName());
        assertEquals("e65db2e1-16f4-452e-922e-afe4e8533998", missingEntity.getGuid());
    }

    private Bundle loadConfiguration(String content) throws IOException {
        EntityLoader loader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(MissingGatewayEntity.class));
        final File configFolder = rootProjectDir.createDirectory("config");
        final File identityProvidersFile = new File(configFolder, "missing-entities.yml");
        Files.touch(identityProvidersFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        loader.load(bundle, rootProjectDir.getRoot());
        return bundle;
    }
}
