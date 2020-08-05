package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.JSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ExtendWith(MockitoExtension.class)
public class UnsupportedEntityLoaderTest {

    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;

    @BeforeEach
    void before() {
        jsonTools = new JsonTools(fileUtils);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testNoEntities(TemporaryFolder temporaryFolder) {
        UnsupportedEntityLoader unsupportedEntityLoader = new UnsupportedEntityLoader(jsonTools, new IdGenerator(), DocumentTools.INSTANCE);

        Bundle bundle = new Bundle();
        unsupportedEntityLoader.load(bundle, temporaryFolder.getRoot());
        assertTrue(bundle.getServices().isEmpty());
    }

    @Test
    void testEntityInfo() {
        UnsupportedEntityLoader unsupportedEntityLoader = new UnsupportedEntityLoader(jsonTools, new IdGenerator(), DocumentTools.INSTANCE);
        assertEquals("unsupported-entities", unsupportedEntityLoader.getFileName());
        assertEquals("UNSUPPORTED", unsupportedEntityLoader.getEntityType());
        assertEquals(UnsupportedGatewayEntity.class, unsupportedEntityLoader.getBeanClass());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadYaml(TemporaryFolder temporaryFolder) throws IOException, SAXException, DocumentParseException {
        DocumentTools documentTools = Mockito.mock(DocumentTools.class);
        UnsupportedEntityLoader unsupportedEntityLoader = new UnsupportedEntityLoader(jsonTools, new IdGenerator(), documentTools);
        String yaml = "SSG_ACTIVE/Test MQ:\n" +
                "  type: \"SSG_ACTIVE\"\n" +
                "  id: \"101c874561f1c09907094335ae786924\"\n";
        File configFolder = temporaryFolder.createDirectory("config");

        File yamlFile = new File(configFolder, "unsupported-entities.yml");
        Files.touch(yamlFile);

        String xml = "<l7:Items xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\"><l7:Item>\n" +
                "        <l7:Name>Test MQ</l7:Name>\n" +
                "        <l7:Id>101c874561f1c09907094335ae786924</l7:Id>\n" +
                "        <l7:Type>SSG_ACTIVE</l7:Type>\n" +
                "        <l7:Resource>\n" +
                "            <l7:ActiveConnector id=\"101c874561f1c09907094335ae786924\">\n" +
                "                <l7:Name>Test MQ</l7:Name>\n" +
                "                <l7:Enabled>true</l7:Enabled>\n" +
                "                <l7:Type>MqNative</l7:Type>\n" +
                "                <l7:Properties>\n" +
                "                    <l7:Property key=\"MqNativeChannel\">\n" +
                "                        <l7:StringValue>SYSTEM.DEF.SVRCONN</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeHostName\">\n" +
                "                        <l7:StringValue>sfad</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeInboundIsFailedQueuePutMessageOptionsUsed\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeInboundIsGetMessageOptionsUsed\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeInboundIsOpenOptionsUsed\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeInboundIsReplyQueuePutMessageOptionsUsed\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeIsQueueCredentialRequired\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeIsSslEnabled\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeOutboundIsReplyQueueGetMessageOptionsUsed\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeOutboundIsTemplateQueue\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeOutboundMessageFormat\">\n" +
                "                        <l7:StringValue>AUTOMATIC</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativePort\">\n" +
                "                        <l7:StringValue>24</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeQueueManagerName\">\n" +
                "                        <l7:StringValue>sfasdf</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeReplyType\">\n" +
                "                        <l7:StringValue>REPLY_NONE</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeTargetQueueName\">\n" +
                "                        <l7:StringValue>adsfa</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"inbound\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                </l7:Properties>\n" +
                "            </l7:ActiveConnector>\n" +
                "        </l7:Resource>\n" +
                "    </l7:Item></l7:Items>";
        File xmlFile = new File(configFolder, "unsupported-entities.xml");
        Files.touch(xmlFile);
        Mockito.when(documentTools.parse(Mockito.any(File.class))).thenReturn(DocumentTools.INSTANCE.parse(xml));
        Mockito.when(fileUtils.getInputStream(Mockito.any(File.class))).thenReturn(new ByteArrayInputStream(yaml.getBytes(Charset.forName("UTF-8"))));

        Bundle bundle = new Bundle();
        unsupportedEntityLoader.load(bundle, temporaryFolder.getRoot());

        verifyConfig(bundle);
    }

    @Test
    void testLoadWithNullConfigFolderPath() throws JsonProcessingException {
        UnsupportedEntityLoader unsupportedEntityLoader = new UnsupportedEntityLoader(jsonTools, new IdGenerator(), DocumentTools.INSTANCE);
        UnsupportedGatewayEntity gatewayEntity = new UnsupportedGatewayEntity();
        gatewayEntity.setType("SSG_ACTIVE");
        gatewayEntity.setName("Test MQ");
        gatewayEntity.setId("101c874561f1c09907094335ae786924");
        String yaml = jsonTools.getObjectWriter(JSON).writeValueAsString(gatewayEntity);
        Bundle bundle = new Bundle();
        unsupportedEntityLoader.load(bundle, "SSG_ACTIVE/Test MQ", yaml, null);

        UnsupportedGatewayEntity entity = bundle.getUnsupportedEntities().get("SSG_ACTIVE/Test MQ");
        assertEquals(1, bundle.getUnsupportedEntities().size());
        assertEquals("SSG_ACTIVE", entity.getType());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadWithEnvironmentConfigFolderPath(TemporaryFolder temporaryFolder) throws IOException, DocumentParseException {
        DocumentTools documentTools = Mockito.mock(DocumentTools.class);
        File configFolder = temporaryFolder.createDirectory("config");
        String xml = "<l7:Items xmlns:l7=\"http://ns.l7tech.com/2010/04/gateway-management\"><l7:Item>\n" +
                "        <l7:Name>Test MQ</l7:Name>\n" +
                "        <l7:Id>101c874561f1c09907094335ae786924</l7:Id>\n" +
                "        <l7:Type>SSG_ACTIVE</l7:Type>\n" +
                "        <l7:Resource>\n" +
                "            <l7:ActiveConnector id=\"101c874561f1c09907094335ae786924\">\n" +
                "                <l7:Name>Test MQ</l7:Name>\n" +
                "                <l7:Enabled>true</l7:Enabled>\n" +
                "                <l7:Type>MqNative</l7:Type>\n" +
                "                <l7:Properties>\n" +
                "                    <l7:Property key=\"MqNativeChannel\">\n" +
                "                        <l7:StringValue>SYSTEM.DEF.SVRCONN</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeHostName\">\n" +
                "                        <l7:StringValue>sfad</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeInboundIsFailedQueuePutMessageOptionsUsed\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeInboundIsGetMessageOptionsUsed\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeInboundIsOpenOptionsUsed\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeInboundIsReplyQueuePutMessageOptionsUsed\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeIsQueueCredentialRequired\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeIsSslEnabled\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeOutboundIsReplyQueueGetMessageOptionsUsed\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeOutboundIsTemplateQueue\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeOutboundMessageFormat\">\n" +
                "                        <l7:StringValue>AUTOMATIC</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativePort\">\n" +
                "                        <l7:StringValue>24</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeQueueManagerName\">\n" +
                "                        <l7:StringValue>sfasdf</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeReplyType\">\n" +
                "                        <l7:StringValue>REPLY_NONE</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"MqNativeTargetQueueName\">\n" +
                "                        <l7:StringValue>adsfa</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                    <l7:Property key=\"inbound\">\n" +
                "                        <l7:StringValue>false</l7:StringValue>\n" +
                "                    </l7:Property>\n" +
                "                </l7:Properties>\n" +
                "            </l7:ActiveConnector>\n" +
                "        </l7:Resource>\n" +
                "    </l7:Item></l7:Items>";
        File xmlFile = new File(configFolder, "unsupported-entities.xml");
        Files.touch(xmlFile);
        Mockito.when(documentTools.parse(Mockito.any(File.class))).thenReturn(DocumentTools.INSTANCE.parse(xml));
        UnsupportedEntityLoader unsupportedEntityLoader = new UnsupportedEntityLoader(jsonTools, new IdGenerator(), documentTools);
        UnsupportedGatewayEntity gatewayEntity = new UnsupportedGatewayEntity();
        gatewayEntity.setType("SSG_ACTIVE");
        gatewayEntity.setName("Test MQ");
        gatewayEntity.setId("101c874561f1c09907094335ae786924");
        String yaml = jsonTools.getObjectWriter(JSON).writeValueAsString(gatewayEntity);
        Bundle bundle = new Bundle();
        unsupportedEntityLoader.load(bundle, "SSG_ACTIVE/Test MQ", yaml, configFolder.getPath());

        verifyConfig(bundle);
    }

    private void verifyConfig(Bundle bundle) {
        UnsupportedGatewayEntity entity = bundle.getUnsupportedEntities().get("SSG_ACTIVE/Test MQ");
        Element element = entity.getElement();
        assertEquals(1, bundle.getUnsupportedEntities().size());
        assertEquals("SSG_ACTIVE", entity.getType());
        assertEquals("Test MQ", entity.getName());
        assertEquals("l7:ActiveConnector", element.getTagName());
    }
}
