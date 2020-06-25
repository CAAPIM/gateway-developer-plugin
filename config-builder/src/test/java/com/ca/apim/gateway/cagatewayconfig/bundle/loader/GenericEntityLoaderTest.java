package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class GenericEntityLoaderTest {

    @Test
    public void testHttp2GenericEntity() throws DocumentParseException {
        GenericEntityLoader loader = new GenericEntityLoader();
        Bundle bundle = new Bundle();
        Document document = DocumentTools.INSTANCE.parse(HTTP2_CLIENT_CONFIG);
        loader.load(bundle, document.getDocumentElement());

        GenericEntity genericEntity = bundle.getGenericEntities().get("default");
        assertEquals("com.l7tech.external.assertions.http2.routing.model.Http2ClientConfigurationEntity",
                genericEntity.getEntityClassName());
        assertNotNull(genericEntity.getValueXml());
        assertThat(genericEntity.getValueXml(), containsString("<void property=\"tlsVersion\">"));
        assertThat(genericEntity.getValueXml(), containsString("<void property=\"readTimeout\">"));
        assertTrue(genericEntity.getAnnotations().contains(new Annotation(AnnotationConstants.ANNOTATION_TYPE_BUNDLE_ENTITY)));
    }

    private static final String HTTP2_CLIENT_CONFIG = "<l7:Item>\n" +
            "                    <l7:Name>default</l7:Name>\n" +
            "                    <l7:Id>6183c11a61d2a42729506f690aa8eab9</l7:Id>\n" +
            "                    <l7:Type>GENERIC</l7:Type>\n" +
            "                    <l7:TimeStamp>2020-06-18T09:30:00.926Z</l7:TimeStamp>\n" +
            "                    <l7:Resource>\n" +
            "                        <l7:GenericEntity id=\"6183c11a61d2a42729506f690aa8eab9\" version=\"3\">\n" +
            "                            <l7:Name>default</l7:Name>\n" +
            "                            <l7:Description>default</l7:Description>\n" +
            "                            <l7:EntityClassName>com.l7tech.external.assertions.http2.routing.model.Http2ClientConfigurationEntity</l7:EntityClassName>\n" +
            "                            <l7:Enabled>true</l7:Enabled>\n" +
            "                            <l7:ValueXml>&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;\n" +
            "&lt;java version=&quot;1.8.0_242&quot; class=&quot;java.beans.XMLDecoder&quot;&gt;\n" +
            " &lt;object class=&quot;com.l7tech.external.assertions.http2.routing.model.Http2ClientConfigurationEntity&quot;&gt;\n" +
            "  &lt;void property=&quot;connectionTimeout&quot;&gt;\n" +
            "   &lt;int&gt;1000&lt;/int&gt;\n" +
            "  &lt;/void&gt;\n" +
            "  &lt;void property=&quot;description&quot;&gt;\n" +
            "   &lt;string&gt;default&lt;/string&gt;\n" +
            "  &lt;/void&gt;\n" +
            "  &lt;void property=&quot;id&quot;&gt;\n" +
            "   &lt;string&gt;6183c11a61d2a42729506f690aa8eab9&lt;/string&gt;\n" +
            "  &lt;/void&gt;\n" +
            "  &lt;void property=&quot;name&quot;&gt;\n" +
            "   &lt;string&gt;default&lt;/string&gt;\n" +
            "  &lt;/void&gt;\n" +
            "  &lt;void property=&quot;readTimeout&quot;&gt;\n" +
            "   &lt;int&gt;1001&lt;/int&gt;\n" +
            "  &lt;/void&gt;\n" +
            "  &lt;void property=&quot;tlsCipherSuites&quot;&gt;\n" +
            "   &lt;string&gt;TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_DHE_RSA_WITH_AES_128_GCM_SHA256&lt;/string&gt;\n" +
            "  &lt;/void&gt;\n" +
            "  &lt;void property=&quot;tlsVersion&quot;&gt;\n" +
            "   &lt;string&gt;TLSv1.2&lt;/string&gt;\n" +
            "  &lt;/void&gt;\n" +
            "  &lt;void property=&quot;valueXml&quot;&gt;\n" +
            "   &lt;string&gt;&lt;/string&gt;\n" +
            "  &lt;/void&gt;\n" +
            "  &lt;void property=&quot;version&quot;&gt;\n" +
            "   &lt;int&gt;2&lt;/int&gt;\n" +
            "  &lt;/void&gt;\n" +
            " &lt;/object&gt;\n" +
            "&lt;/java&gt;\n" +
            "</l7:ValueXml>\n" +
            "                        </l7:GenericEntity>\n" +
            "                    </l7:Resource>\n" +
            "                </l7:Item>";
}
