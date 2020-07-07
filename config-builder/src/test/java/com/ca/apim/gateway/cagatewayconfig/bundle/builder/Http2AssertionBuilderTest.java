package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.STRING_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Http2AssertionBuilderTest {
    private Policy policy;
    private Bundle bundle;
    private Document document;
    private Http2AssertionBuilder http2AssertionBuilder = new Http2AssertionBuilder();
    private PolicyBuilderContext policyBuilderContext;

    @BeforeEach
    void beforeEach() {
        policy = new Policy();
        policy.setPath("test/policy/path.xml");
        bundle = new Bundle(new ProjectInfo("TestProject", "TestGroup", "1.0"));
        bundle.setDependencies(new HashSet<>());
        document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
    }

    @Test
    void testPrepareHttp2AssertionClientConfigIds() {
        bundle.getGenericEntities().put("http2client",
                createHttp2ClientConfigWithAnnotation(AnnotationType.BUNDLE_HINTS, "a2097d7f50280e9411c277aafedc180d"));

        Element http2RoutingAssertionElement = createHttp2Assertion(document);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        http2AssertionBuilder.buildAssertionElement(http2RoutingAssertionElement, policyBuilderContext);

        final Element clientConfigId = getSingleChildElement(http2RoutingAssertionElement, HTTP2_CLIENT_CONFIG_GOID, true);
        assertNotNull(clientConfigId);
        assertEquals("a2097d7f50280e9411c277aafedc180d", clientConfigId.getAttributes().getNamedItem(GOID_VALUE).getTextContent());
    }

    @NotNull
    private Element createHttp2Assertion(Document document) {
        return createElementWithChildren(
                document,
                HTTP2_ROUTING_ASSERTION,
                createElementWithAttribute(document, "L7p:ProtectedServiceUrl", STRING_VALUE, "http://apim-hugh-new.lvn.broadcom.net:90"),
                createElementWithAttribute(document, HTTP2_CLIENT_CONFIG_NAME, STRING_VALUE, "http2client")
        );
    }

    @NotNull
    private GenericEntity createHttp2ClientConfigWithAnnotation(final String type, final String id) {
        GenericEntity genericEntity = new GenericEntity();
        genericEntity.setName("http2client");
        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation(type);
        annotation.setId(id);
        annotations.add(annotation);
        genericEntity.setAnnotations(annotations);
        return genericEntity;
    }
}
