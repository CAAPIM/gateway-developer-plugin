package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils;
import com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TemporaryFolderExtension.class)
public class UnsupportedEntityWriterTest {

    @Test
    void testWrite(final TemporaryFolder temporaryFolder) throws DocumentParseException, IOException {
        UnsupportedEntityWriter writer = new UnsupportedEntityWriter(DocumentFileUtils.INSTANCE, DocumentTools.INSTANCE);
        String entityName = "Test MQ";
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
        Document document = DocumentTools.INSTANCE.parse(xml);
        Bundle bundle = new Bundle();
        UnsupportedGatewayEntity unsupportedGatewayEntity = new UnsupportedGatewayEntity();
        unsupportedGatewayEntity.setName(entityName);
        unsupportedGatewayEntity.setId("testId");
        unsupportedGatewayEntity.setType("SSG_ACTIVE");
        NodeList nodeList = document.getElementsByTagName("l7:ActiveConnector");
        Node node = nodeList.item(0);
        unsupportedGatewayEntity.setElement((Element)node);

        bundle.getUnsupportedEntities().put("Test MQ", unsupportedGatewayEntity);

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File configFolder = new File(temporaryFolder.getRoot(), "config");
        assertTrue(configFolder.exists());

        File unsupportedEntitiesXml = new File(configFolder, "unsupported-entities.xml");
        assertTrue(unsupportedEntitiesXml.exists());

        EntityUtils.GatewayEntityInfo gatewayEntityInfo = EntityUtils.createEntityInfo(UnsupportedGatewayEntity.class);
        assertEquals("unsupported-entities", gatewayEntityInfo.getFileName());
        WriterHelper.writeFile(temporaryFolder.getRoot(), DocumentFileUtils.INSTANCE, JsonTools.INSTANCE,
                bundle.getUnsupportedEntities(), gatewayEntityInfo.getFileName(), UnsupportedGatewayEntity.class);

        File unsupportedEntitiesYml = new File(configFolder, "unsupported-entities.yml");
        assertTrue(unsupportedEntitiesYml.exists());

        final String ymlContent = new String(Files.readAllBytes(unsupportedEntitiesYml.toPath()), Charset.forName("utf-8"));
        assertTrue(ymlContent.contains("SSG_ACTIVE/Test MQ:"));
    }
}
