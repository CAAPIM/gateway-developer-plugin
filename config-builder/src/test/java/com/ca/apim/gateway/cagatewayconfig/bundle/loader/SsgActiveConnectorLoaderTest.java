package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.SsgActiveConnector;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SsgActiveConnectorLoaderTest {
    private static final String ACTIVE_CONNECTOR = "<l7:Item>\n" +
            "                    <l7:Name>MQNativeTest</l7:Name>\n" +
            "                    <l7:Id>28fae8079e03cde98bda9fd54f744949</l7:Id>\n" +
            "                    <l7:Type>SSG_ACTIVE_CONNECTOR</l7:Type>\n" +
            "                    <l7:TimeStamp>2020-06-12T13:34:27.309+05:30</l7:TimeStamp>\n" +
            "                    <l7:Resource>\n" +
            "                        <l7:ActiveConnector id=\"28fae8079e03cde98bda9fd54f744949\" version=\"2\">\n" +
            "                            <l7:Name>MQNativeTest</l7:Name>\n" +
            "                            <l7:Enabled>true</l7:Enabled>\n" +
            "                            <l7:Type>MqNative</l7:Type>\n" +
            "                            <l7:Properties>\n" +
            "                                    <l7:Property key=\"MqNativeChannel\">\n" +
            "<l7:StringValue>SYSTEM.DEF.SVRCONN</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeCipherSuite\">\n" +
            "<l7:StringValue>TLS_DHE_RSA_WITH_AES_128_CBC_SHA</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeHostName\">\n" +
            "<l7:StringValue>asdfasd</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeInboundIsFailedQueuePutMessageOptionsUsed\">\n" +
            "<l7:StringValue>false</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeInboundIsGetMessageOptionsUsed\">\n" +
            "<l7:StringValue>false</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeInboundIsOpenOptionsUsed\">\n" +
            "<l7:StringValue>false</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeInboundIsReplyQueuePutMessageOptionsUsed\">\n" +
            "<l7:StringValue>false</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeIsQueueCredentialRequired\">\n" +
            "<l7:StringValue>true</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeIsSslEnabled\">\n" +
            "<l7:StringValue>true</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeIsSslKeystoreUsed\">\n" +
            "<l7:StringValue>true</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeOutboundIsReplyQueueGetMessageOptionsUsed\">\n" +
            "<l7:StringValue>false</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeOutboundIsTemplateQueue\">\n" +
            "\t<l7:StringValue>false</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeOutboundMessageFormat\">\n" +
            "\t<l7:StringValue>AUTOMATIC</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativePort\">\n" +
            "\t<l7:StringValue>42223</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeQueueManagerName\">\n" +
            "\t<l7:StringValue>asfa</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeReplyType\">\n" +
            "\t<l7:StringValue>REPLY_NONE</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeSecurePasswordOid\">\n" +
            "\t<l7:StringValue>20bc1e5bbdc0e124827b262884086343</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeTargetQueueName\">\n" +
            "\t<l7:StringValue>asdfasd</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"MqNativeUserId\">\n" +
            "\t<l7:StringValue>admin</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "<l7:Property key=\"inbound\">\n" +
            "\t<l7:StringValue>false</l7:StringValue>\n" +
            "</l7:Property>\n" +
            "                            </l7:Properties>\n" +
            "                        </l7:ActiveConnector>\n" +
            "                    </l7:Resource>\n" +
            "                </l7:Item>";

    @Test
    public void testLoad() throws DocumentParseException {
        SsgActiveConnectorLoader ssgActiveConnectorLoader = new SsgActiveConnectorLoader();
        Bundle bundle = new Bundle();
        Document document = DocumentTools.INSTANCE.parse(ACTIVE_CONNECTOR);
        ssgActiveConnectorLoader.load(bundle, document.getDocumentElement());
        SsgActiveConnector ssgActiveConnector = bundle.getSsgActiveConnectors().get("MQNativeTest");
        Assert.assertEquals("MqNative", ssgActiveConnector.getType());
        Assert.assertNull(ssgActiveConnector.getTargetServiceReference());
        Assert.assertTrue(ssgActiveConnector.getAnnotations().contains(new Annotation(AnnotationConstants.ANNOTATION_TYPE_BUNDLE_ENTITY)));
    }
}
