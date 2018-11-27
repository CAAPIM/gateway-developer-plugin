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
import com.google.common.collect.ImmutableMap;
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
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class JmsDestinationLoaderTest {
    
    private static final String JMS_DESTINATION_NAME = "my-jms-endpoint";
    
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
    void testLoadJmsDestinationYaml() throws IOException {
        String yaml = JMS_DESTINATION_NAME + ":\n" +
                "  isInbound: true\n" + 
                "  isTemplate: false\n" +
                "  providerType: \"TIBCO EMS\"\n" +
                "  initialContextFactoryClassName: \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\"\n" +
                "  jndiUrl: \"tibjmsnaming://machinename:7222\"\n" + 
                "  jndiUsername: \"my-jndi-username\"\n" +
                "  jndiPasswordRef: \"my-jndi-password-ref\"\n" +
                "  jndiProperties:\n" +
                "    additional-jndi-prop-name-1: \"additional-jndi-prop-val-1\"\n" +
                "    additional-jndi-prop-name-2: \"additional-jndi-prop-val-2\"\n" +
                "  destinationType: \"Queue\"\n" +
                "  connectionFactoryName: \"my-qcf-name\"\n" +
                "  destinationName: \"my-jms-destination-name\"\n" +
                "  destinationUsername: \"my-destination-username\"\n" +
                "  destinationPasswordRef: \"my-destination-password-ref\"\n";
        
        loadJmsDestination(yaml, "yml", null);
    }

    @Test
    void testLoadJmsDestinationJson() throws IOException {
        String json = "{\n" +
                "    \"" + JMS_DESTINATION_NAME + "\" : {\n" +
                "    \"isInbound\" : true,\n" +
                "    \"isTemplate\" : false,\n" +
                "    \"providerType\" : \"TIBCO EMS\",\n" +
                "    \"initialContextFactoryClassName\" : \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\",\n" +
                "    \"jndiUrl\" : \"tibjmsnaming://machinename:7222\",\n" +
                "    \"jndiUsername\" : \"my-jndi-username\",\n" +
                "    \"jndiPasswordRef\" : \"my-jndi-password-ref\",\n" +
                "    \"jndiProperties\" : {\n" +
                "      \"additional-jndi-prop-name-1\" : \"additional-jndi-prop-val-1\",\n" +
                "      \"additional-jndi-prop-name-2\" : \"additional-jndi-prop-val-2\"\n" +
                "    },\n" +
                "    \"destinationType\" : \"Queue\",\n" +
                "    \"connectionFactoryName\" : \"my-qcf-name\",\n" +
                "    \"destinationName\" : \"my-jms-destination-name\",\n" +
                "    \"destinationUsername\" : \"my-destination-username\",\n" +
                "    \"destinationPasswordRef\" : \"my-destination-password-ref\"\n" +
                "    }\n" +
                "}\n";

        loadJmsDestination(json, "json", null);
    }
    
    @Test
    void testLoadJmsDestinationMalformedYaml() throws IOException {
        String yaml = JMS_DESTINATION_NAME + ":\n" +
                "  isInbound: true\n" +
                "  isTemplate: false\n" +
                "  providerType: \"TIBCO EMS\"\n" +
                "  initialContextFactoryClassName: \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\"\n" +
                "  jndiUrl: \"tibjmsnaming://machinename:7222\"\n" +
                "  jndiUsername: \"my-jndi-username\"\n" +
                "  jndiPasswordRef "; // Missing value

        loadJmsDestination(yaml, "yml", JsonToolsException.class);
    }

    @Test
    void testLoadJmsDestinationMalformedJson() throws IOException {
        String json = "{\n" +
                "    \"" + JMS_DESTINATION_NAME + "\" : {\n" +
                "    \"isInbound\" : true,\n" +
                "    \"isTemplate\" : false,\n" +
                "    \"providerType\" : \"TIBCO EMS\",\n" +
                "    \"initialContextFactoryClassName\" : \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\",\n" +
                "    \"jndiUrl\" : \"tibjmsnaming://machinename:7222\",\n" +
                "    \"jndiUsername\" : \"my-jndi-username\",\n" +
                "    \"jndiPasswordRef\" : \"my-jndi-password-ref\"\n" +
                // Missing closing '}'
                "}\n";

        loadJmsDestination(json, "json", JsonToolsException.class);
    }

    @Test
    void testLoadJmsDestinationPasswordRefAndPasswordYaml() throws IOException {
        String yaml = JMS_DESTINATION_NAME + ":\n" +
                "  isInbound: true\n" +
                "  isTemplate: false\n" +
                "  providerType: \"TIBCO EMS\"\n" +
                "  initialContextFactoryClassName: \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\"\n" +
                "  jndiUrl: \"tibjmsnaming://machinename:7222\"\n" +
                "  jndiUsername: \"my-jndi-username\"\n" +
                "  jndiPasswordRef: \"my-jndi-password-ref\"\n" +
                "  jndiPassword: \"my-jndi-password\"\n";

        loadJmsDestination(yaml, "yml", ConfigLoadException.class);
    }

    @Test
    void testLoadJmsDestinationPasswordRefAndPasswordJson() throws IOException {
        String json = "{\n" +
                "    \"" + JMS_DESTINATION_NAME + "\" : {\n" +
                "    \"isInbound\" : true,\n" +
                "    \"isTemplate\" : false,\n" +
                "    \"providerType\" : \"TIBCO EMS\",\n" +
                "    \"initialContextFactoryClassName\" : \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\",\n" +
                "    \"jndiUrl\" : \"tibjmsnaming://machinename:7222\",\n" +
                "    \"jndiUsername\" : \"my-jndi-username\",\n" +
                "    \"jndiPasswordRef\" : \"my-jndi-password-ref\",\n" +
                "    \"jndiPassword\" : \"my-jndi-password\"\n" +
                "    }\n" +
                "}\n";

        loadJmsDestination(json, "json", ConfigLoadException.class);
    }
    
    private void loadJmsDestination(String content, String fileType, Class<? extends Exception> expectException) throws IOException {
        final EntityLoader loader = createEntityLoader(jsonTools, new IdGenerator(), createEntityInfo(JmsDestination.class));
        final File configFolder = rootProjectDir.createDirectory("config");
        final File jmsDestinationsFile = new File(configFolder, "jms-destinations." + fileType);
        Files.touch(jmsDestinationsFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        if (expectException != null) {
            assertThrows(expectException, () -> loadJmsDestinations(loader, bundle, rootProjectDir));
            return;
        } else {
            loadJmsDestinations(loader, bundle, rootProjectDir);
        }
        checkJmsDestination(bundle);
    }

    private static void loadJmsDestinations(EntityLoader loader, Bundle bundle, TemporaryFolder rootProjectDir) {
        loader.load(bundle, rootProjectDir.getRoot());
    }

    private static void checkJmsDestination(Bundle bundle) {
        assertFalse(bundle.getJmsDestinations().isEmpty(), "No JMS destinations loaded");
        assertEquals(1, bundle.getJmsDestinations().size(), () -> "Expected 1 JMS destinations, found " + bundle.getJmsDestinations().size());
        
        JmsDestination jmsDestination = bundle.getJmsDestinations().get(JMS_DESTINATION_NAME);
        assertNotNull(jmsDestination);
        assertTrue(jmsDestination.isInbound());
        assertFalse(jmsDestination.isTemplate());
        assertEquals("TIBCO EMS", jmsDestination.getProviderType());
        assertEquals("com.tibco.tibjms.naming.TibjmsInitialContextFactory", jmsDestination.getInitialContextFactoryClassName());
        assertEquals("tibjmsnaming://machinename:7222", jmsDestination.getJndiUrl());
        assertEquals("my-jndi-username", jmsDestination.getJndiUsername());
        assertEquals("my-jndi-password-ref", jmsDestination.getJndiPasswordRef());
        assertNull(jmsDestination.getJndiPassword());
        
        assertPropertiesContent(ImmutableMap.of(
                "additional-jndi-prop-name-1", "additional-jndi-prop-val-1",
                "additional-jndi-prop-name-2", "additional-jndi-prop-val-2"),
                jmsDestination.getJndiProperties());
        
        assertEquals("Queue", jmsDestination.getDestinationType());
        assertEquals("my-qcf-name", jmsDestination.getConnectionFactoryName());
        assertEquals("my-jms-destination-name", jmsDestination.getDestinationName());
        assertEquals("my-destination-username", jmsDestination.getDestinationUsername());
        assertEquals("my-destination-password-ref", jmsDestination.getDestinationPasswordRef());
        assertNull(jmsDestination.getDestinationPassword());
        
        
    }
}
