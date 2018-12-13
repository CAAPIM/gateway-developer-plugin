/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.AcknowledgeType;
import com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.ServiceResolutionSettings;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestinationDetail.ReplyType;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.ConnectionPoolingSettings;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.MessageFormat;
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
import static com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.ContentTypeSource.*;
import static com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.PoolingType.CONNECTION;
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
    
    // (kpak) - add more tests
    
    @Test
    void testLoadInboundJmsDestinationYaml() throws IOException {
        String yaml = JMS_DESTINATION_NAME + ":\n" +
                "  providerType: \"TIBCO EMS\"\n" +
                "  initialContextFactoryClassName: \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\"\n" +
                "  jndiUrl: \"tibjmsnaming://machinename:7222\"\n" + 
                "  jndiUsername: \"my-jndi-username\"\n" +
                "  jndiPasswordRef: \"my-jndi-password-ref\"\n" +
                "  jndiProperties:\n" +
                "    additional-jndi-prop-name-1: \"additional-jndi-prop-val-1\"\n" +
                "    additional-jndi-prop-name-2: \"additional-jndi-prop-val-2\"\n" +
                "  destinationType: \"QUEUE\"\n" +
                "  connectionFactoryName: \"my-qcf-name\"\n" +
                "  destinationName: \"my-jms-destination-name\"\n" +
                "  destinationUsername: \"my-destination-username\"\n" +
                "  destinationPasswordRef: \"my-destination-password-ref\"\n" +
                "  inboundDetail:\n" +
                "    acknowledgeType: \"ON_COMPLETION\"\n" +
                "    replyType: \"SPECIFIED_QUEUE\"\n" +
                "    replyToQueueName: \"my-reply-Q\"\n" +
                "    useRequestCorrelationId: false\n" +
                "    serviceResolutionSettings:\n" +
                "      serviceRef: \"my-associated-service\"\n" +
                "      soapActionMessagePropertyName: \"my-jms-soap-action-prop-name\"\n" +
                "      contentTypeSource: \"FREE_FORM\"\n" +
                "      contentType: \"text/yaml\"\n" +
                "    failureQueueName: \"my-failure-Q\"\n" +
                "    isEnabled: true\n" +
                "    numOfConsumerConnections: 5\n" +
                "    maxMessageSizeBytes: 2048\n";
        
        loadJmsDestination(yaml, true, "yml", null);
    }

    @Test
    void testLoadOutboundJmsDestinationJson() throws IOException {
        String json = "{\n" +
                "  \"" + JMS_DESTINATION_NAME + "\" : {\n" +
                "    \"providerType\" : \"TIBCO EMS\",\n" +
                "    \"initialContextFactoryClassName\" : \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\",\n" +
                "    \"jndiUrl\" : \"tibjmsnaming://machinename:7222\",\n" +
                "    \"jndiUsername\" : \"my-jndi-username\",\n" +
                "    \"jndiPasswordRef\" : \"my-jndi-password-ref\",\n" +
                "    \"jndiProperties\" : {\n" +
                "      \"additional-jndi-prop-name-1\" : \"additional-jndi-prop-val-1\",\n" +
                "      \"additional-jndi-prop-name-2\" : \"additional-jndi-prop-val-2\"\n" +
                "    },\n" +
                "    \"destinationType\" : \"QUEUE\",\n" +
                "    \"connectionFactoryName\" : \"my-qcf-name\",\n" +
                "    \"destinationName\" : \"my-jms-destination-name\",\n" +
                "    \"destinationUsername\" : \"my-destination-username\",\n" +
                "    \"destinationPasswordRef\" : \"my-destination-password-ref\",\n" +
                "    \"outboundDetail\" : {\n" +
                "      \"isTemplate\" : false,\n" +
                "      \"replyType\" : \"SPECIFIED_QUEUE\",\n" +
                "      \"replyToQueueName\" : \"my-reply-Q\",\n" +
                "      \"useRequestCorrelationId\" : false,\n" +
                "      \"messageFormat\" : \"BYTES\",\n" +
                "      \"poolingType\" : \"CONNECTION\",\n" +
                "      \"connectionPoolingSettings\" : {\n" +
                "        \"size\" : 10,\n" +
                "        \"minIdle\" : 5,\n" +
                "        \"maxWaitMs\" : 10000\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        loadJmsDestination(json, false, "json", null);
    }
    
    @Test
    void testLoadInboundJmsDestinationMalformedYaml() throws IOException {
        String yaml = JMS_DESTINATION_NAME + ":\n" +
                "  providerType \"TIBCO EMS\"\n" + // missing colon
                "  initialContextFactoryClassName: \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\"\n" +
                "  jndiUrl: \"tibjmsnaming://machinename:7222\"\n" +
                "  jndiUsername: \"my-jndi-username\"\n" +
                "  jndiPasswordRef: \"my-jndi-password-ref\"\n" +
                "  jndiProperties:\n" +
                "    additional-jndi-prop-name-1: \"additional-jndi-prop-val-1\"\n" +
                "    additional-jndi-prop-name-2: \"additional-jndi-prop-val-2\"\n" +
                "  destinationType: \"QUEUE\"\n" +
                "  connectionFactoryName: \"my-qcf-name\"\n" +
                "  destinationName: \"my-jms-destination-name\"\n" +
                "  destinationUsername: \"my-destination-username\"\n" +
                "  destinationPasswordRef: \"my-destination-password-ref\"\n" +
                "  inboundDetail:\n" +
                "    acknowledgeType: \"ON_COMPLETION\"\n" +
                "    replyType: \"SPECIFIED_QUEUE\"\n" +
                "    replyToQueueName: \"my-reply-Q\"\n" +
                "    useRequestCorrelationId: false\n" +
                "    serviceResolutionSettings:\n" +
                "      serviceRef: \"my-associated-service\"\n" +
                "      soapActionMessagePropertyName: \"my-jms-soap-action-prop-name\"\n" +
                "      contentTypeSource: \"FREE_FORM\"\n" +
                "      contentType: \"text/yaml\"\n" +
                "    failureQueueName: \"my-failure-Q\"\n" +
                "    isEnabled: true\n" +
                "    numOfConsumerConnections: 5\n" +
                "    maxMessageSizeBytes: 2048\n";

        loadJmsDestination(yaml, true, "yml", JsonToolsException.class);
    }

    @Test
    void testLoadOutboundJmsDestinationMalformedJson() throws IOException {
        String json = "{\n" +
                "  \"" + JMS_DESTINATION_NAME + "\" : {\n" +
                "    \"providerType\" : \"TIBCO EMS\",\n" +
                "    \"initialContextFactoryClassName\" : \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\",\n" +
                "    \"jndiUrl\" : \"tibjmsnaming://machinename:7222\",\n" +
                "    \"jndiUsername\" : \"my-jndi-username\",\n" +
                "    \"jndiPasswordRef\" : \"my-jndi-password-ref\",\n" +
                "    \"jndiProperties\" : {\n" +
                "      \"additional-jndi-prop-name-1\" : \"additional-jndi-prop-val-1\",\n" +
                "      \"additional-jndi-prop-name-2\" : \"additional-jndi-prop-val-2\"\n" +
                "    },\n" +
                "    \"destinationType\" : \"QUEUE\",\n" +
                "    \"connectionFactoryName\" : \"my-qcf-name\",\n" +
                "    \"destinationName\" : \"my-jms-destination-name\",\n" +
                "    \"destinationUsername\" : \"my-destination-username\",\n" +
                "    \"destinationPasswordRef\" : \"my-destination-password-ref\",\n" +
                "    \"outboundDetail\" : {\n" +
                "      \"isTemplate\" : false,\n" +
                "      \"replyType\" : \"SPECIFIED_QUEUE\",\n" +
                "      \"replyToQueueName\" : \"my-reply-Q\",\n" +
                "      \"useRequestCorrelationId\" : false,\n" +
                "      \"messageFormat\" : \"BYTES\",\n" +
                "      \"poolingType\" : \"CONNECTION\",\n" +
                "      \"connectionPoolingSettings\" : {\n" +
                "        \"size\" : 10,\n" +
                "        \"minIdle\" : 5,\n" +
                "        \"maxWaitMs\" : 10000\n" +
                "      }\n" +
                "    }\n" +
                "  }\n";
                // Missing last closing };

        loadJmsDestination(json, false, "json", JsonToolsException.class);
    }

    @Test
    void testLoadInboundJmsDestination_JndiPasswordRefAndPasswordYaml() throws IOException {
        String yaml = JMS_DESTINATION_NAME + ":\n" +
                "  providerType: \"TIBCO EMS\"\n" +
                "  initialContextFactoryClassName: \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\"\n" +
                "  jndiUrl: \"tibjmsnaming://machinename:7222\"\n" +
                "  jndiUsername: \"my-jndi-username\"\n" +
                "  jndiPasswordRef: \"my-jndi-password-ref\"\n" +
                "  jndiPassword: \"my-jndi-password\"\n" + // JNDI password defined twice.
                "  jndiProperties:\n" +
                "    additional-jndi-prop-name-1: \"additional-jndi-prop-val-1\"\n" +
                "    additional-jndi-prop-name-2: \"additional-jndi-prop-val-2\"\n" +
                "  destinationType: \"QUEUE\"\n" +
                "  connectionFactoryName: \"my-qcf-name\"\n" +
                "  destinationName: \"my-jms-destination-name\"\n" +
                "  destinationUsername: \"my-destination-username\"\n" +
                "  destinationPasswordRef: \"my-destination-password-ref\"\n" +
                "  inboundDetail:\n" +
                "    acknowledgeType: \"ON_COMPLETION\"\n" +
                "    replyType: \"SPECIFIED_QUEUE\"\n" +
                "    replyToQueueName: \"my-reply-Q\"\n" +
                "    useRequestCorrelationId: false\n" +
                "    serviceResolutionSettings:\n" +
                "      serviceRef: \"my-associated-service\"\n" +
                "      soapActionMessagePropertyName: \"my-jms-soap-action-prop-name\"\n" +
                "      contentTypeSource: \"FREE_FORM\"\n" +
                "      contentType: \"text/yaml\"\n" +
                "    failureQueueName: \"my-failure-Q\"\n" +
                "    isEnabled: true\n" +
                "    numOfConsumerConnections: 5\n" +
                "    maxMessageSizeBytes: 2048\n";

        loadJmsDestination(yaml, true,"yml", ConfigLoadException.class);
    }

    @Test
    void testLoadOutboundJmsDestination_DestinationPasswordRefAndPasswordJson() throws IOException {
        String json = "{\n" +
                "    \"" + JMS_DESTINATION_NAME + "\" : {\n" +
                "    \"providerType\" : \"TIBCO EMS\",\n" +
                "    \"initialContextFactoryClassName\" : \"com.tibco.tibjms.naming.TibjmsInitialContextFactory\",\n" +
                "    \"jndiUrl\" : \"tibjmsnaming://machinename:7222\",\n" +
                "    \"jndiUsername\" : \"my-jndi-username\",\n" +
                "    \"jndiPasswordRef\" : \"my-jndi-password-ref\",\n" +
                "    \"jndiProperties\" : {\n" +
                "      \"additional-jndi-prop-name-1\" : \"additional-jndi-prop-val-1\",\n" +
                "      \"additional-jndi-prop-name-2\" : \"additional-jndi-prop-val-2\"\n" +
                "    },\n" +
                "    \"destinationType\" : \"QUEUE\",\n" +
                "    \"connectionFactoryName\" : \"my-qcf-name\",\n" +
                "    \"destinationName\" : \"my-jms-destination-name\",\n" +
                "    \"destinationUsername\" : \"my-destination-username\",\n" +
                "    \"destinationPasswordRef\" : \"my-destination-password-ref\",\n" +
                "    \"destinationPassword\" : \"my-destination-password\",\n" + // Destination password defined twice.
                "    \"outboundDetail\" : {\n" +
                "      \"isTemplate\" : false,\n" +
                "      \"replyType\" : \"SPECIFIED_QUEUE\",\n" +
                "      \"replyToQueueName\" : \"my-reply-Q\",\n" +
                "      \"useRequestCorrelationId\" : false,\n" +
                "      \"messageFormat\" : \"BYTES\",\n" +
                "      \"poolingType\" : \"CONNECTION\",\n" +
                "      \"connectionPoolingSettings\" : {\n" +
                "        \"size\" : 10,\n" +
                "        \"minIdle\" : 5,\n" +
                "        \"maxWaitMs\" : 10000\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

        loadJmsDestination(json, false, "json", ConfigLoadException.class);
    }
    
    private void loadJmsDestination(String content, boolean isInbound, String fileType, Class<? extends Exception> expectException) throws IOException {
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
        checkJmsDestination(bundle, isInbound);
    }

    private static void loadJmsDestinations(EntityLoader loader, Bundle bundle, TemporaryFolder rootProjectDir) {
        loader.load(bundle, rootProjectDir.getRoot());
    }

    private static void checkJmsDestination(Bundle bundle, boolean isInbound) {
        assertFalse(bundle.getJmsDestinations().isEmpty(), "No JMS destinations loaded");
        assertEquals(1, bundle.getJmsDestinations().size(), () -> "Expected 1 JMS destinations, found " + bundle.getJmsDestinations().size());
        
        JmsDestination jmsDestination = bundle.getJmsDestinations().get(JMS_DESTINATION_NAME);
        assertNotNull(jmsDestination);
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
        
        assertEquals(JmsDestination.DestinationType.QUEUE, jmsDestination.getDestinationType());
        assertEquals("my-qcf-name", jmsDestination.getConnectionFactoryName());
        assertEquals("my-jms-destination-name", jmsDestination.getDestinationName());
        assertEquals("my-destination-username", jmsDestination.getDestinationUsername());
        assertEquals("my-destination-password-ref", jmsDestination.getDestinationPasswordRef());
        assertNull(jmsDestination.getDestinationPassword());
        
        if (isInbound) {
            InboundJmsDestinationDetail inboundDetail = jmsDestination.getInboundDetail();
            assertNotNull(inboundDetail);
            assertNull(jmsDestination.getOutboundDetail());
            
            assertEquals(AcknowledgeType.ON_COMPLETION, inboundDetail.getAcknowledgeType());
            assertEquals(ReplyType.SPECIFIED_QUEUE, inboundDetail.getReplyType());
            assertEquals("my-reply-Q", inboundDetail.getReplyToQueueName());
            assertFalse(inboundDetail.useRequestCorrelationId());
            
            ServiceResolutionSettings serviceResolutionSettings = inboundDetail.getServiceResolutionSettings();
            assertEquals("my-associated-service", serviceResolutionSettings.getServiceRef());
            assertEquals("my-jms-soap-action-prop-name", serviceResolutionSettings.getSoapActionMessagePropertyName());
            assertEquals(FREE_FORM, serviceResolutionSettings.getContentTypeSource());
            assertEquals("text/yaml", serviceResolutionSettings.getContentType());
            
            assertEquals("my-failure-Q", inboundDetail.getFailureQueueName());
            assertTrue(inboundDetail.isEnabled());
            assertEquals(new Integer(5), inboundDetail.getNumOfConsumerConnections());
            assertEquals(new Integer(2048), inboundDetail.getMaxMessageSizeBytes());
        } else {
            OutboundJmsDestinationDetail outboundDetail  = jmsDestination.getOutboundDetail();
            assertNotNull(outboundDetail);
            assertNull(jmsDestination.getInboundDetail());
            
            assertFalse(outboundDetail.isTemplate());
            assertEquals(ReplyType.SPECIFIED_QUEUE, outboundDetail.getReplyType());
            assertEquals("my-reply-Q", outboundDetail.getReplyToQueueName());
            assertFalse(outboundDetail.useRequestCorrelationId());
            assertEquals(MessageFormat.BYTES, outboundDetail.getMessageFormat());
            assertEquals(CONNECTION, outboundDetail.getPoolingType());
            
            ConnectionPoolingSettings connectionPoolingSettings = outboundDetail.getConnectionPoolingSettings();
            assertEquals(new Integer(10), connectionPoolingSettings.getSize());
            assertEquals(new Integer(5), connectionPoolingSettings.getMinIdle());
            assertEquals(new Integer(10000), connectionPoolingSettings.getMaxWaitMs());
        }
    }
}
