/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
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
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.createEntityInfo;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.EntityLoaderUtils.createEntityLoader;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class JmsDestinationLoaderTest {
    
    private static final String JMS_DESTINATION_NAME = "jms-1";
    
    private TemporaryFolder rootProjectDir;
    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;
    
    @BeforeEach
    void setup(final TemporaryFolder temporaryFolder) {
        jsonTools = new JsonTools(fileUtils);
        rootProjectDir = temporaryFolder;
    }
    
    @Test
    void loadJmsDestinationYaml() throws IOException {
        String yaml = JMS_DESTINATION_NAME + ":\n" +
                "  isInbound: true\n" + 
                "  isTemplate: false\n" +
                "  providerType: \"TIBCO EMS\"\n";
        
        loadJmsDestination(yaml, "yml", false);
    }

    @Test
    void loadJmsDestinationJson() throws IOException {
        String json = "{\n" +
                "    \"" + JMS_DESTINATION_NAME + "\" : {\n" +
                "    \"isInbound\" : true,\n" +
                "    \"isTemplate\" : false,\n" +
                "    \"providerType\" : \"TIBCO EMS\"\n" +
                "    }\n" +
                "}\n";

        loadJmsDestination(json, "json", false);
    }
    
    private void loadJmsDestination(String content, String fileType, boolean expectException) throws IOException {
        final EntityLoader loader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(JmsDestination.class));
        final File configFolder = rootProjectDir.createDirectory("config");
        final File jmsDestinationsFile = new File(configFolder, "jms-destinations." + fileType);
        Files.touch(jmsDestinationsFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        if (expectException) {
            assertThrows(JsonToolsException.class, () -> loadJmsDestinations(loader, bundle, rootProjectDir));
            return;
        } else {
            loadJmsDestinations(loader, bundle, rootProjectDir);
        }
        checkListenPort(bundle);
    }

    private static void loadJmsDestinations(EntityLoader loader, Bundle bundle, TemporaryFolder rootProjectDir) {
        loader.load(bundle, rootProjectDir.getRoot());
    }

    private static void checkListenPort(Bundle bundle) {
        assertFalse(bundle.getJmsDestinations().isEmpty(), "No JMS destinations loaded");
        assertEquals(1, bundle.getJmsDestinations().size(), () -> "Expected 1 JMS destinations, found " + bundle.getJmsDestinations().size());
        
        JmsDestination jmsDestination = bundle.getJmsDestinations().get(JMS_DESTINATION_NAME);
        assertNotNull(jmsDestination, "JMS destination not found");
        assertTrue(jmsDestination.isInbound());
        assertFalse(jmsDestination.isTemplate());
        assertEquals("TIBCO EMS", jmsDestination.getProviderType());
    }
}
