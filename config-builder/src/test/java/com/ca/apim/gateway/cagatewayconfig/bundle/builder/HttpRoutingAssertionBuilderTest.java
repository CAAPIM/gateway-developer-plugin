package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.TrustedCert;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.VERIFY_HOSTNAME;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpRoutingAssertionBuilderTest {

    private Policy policy;
    private Bundle bundle;
    private Document document;
    private HttpRoutingAssertionBuilder httpRoutingAssertionBuilder = new HttpRoutingAssertionBuilder();
    private PolicyBuilderContext policyBuilderContext;

    @BeforeEach
    void beforeEach() {
        policy = new Policy();
        policy.setPath("test/policy/path.xml");
        bundle = new Bundle();
        bundle.setDependencies(new HashSet<>());
        document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
    }

    @Test
    void testPrepareRoutingAssertionCertificateIds() {
        bundle.putAllTrustedCerts(ImmutableMap.of("fake-cert-1", createTrustedCertWithAnnotation(AnnotationType.BUNDLE_HINTS, "2cd473fe16d98cd6b9348ffb404517bc")));
        bundle.putAllTrustedCerts(ImmutableMap.of("fake-cert-2", createTrustedCertWithAnnotation(AnnotationType.BUNDLE_HINTS, "28be78b936aa61bc75bd0df2089789cd")));

        Element httpRoutingAssertionElement = createHttpRoutingAssertionWithCertNames(document);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        httpRoutingAssertionBuilder.buildAssertionElement(httpRoutingAssertionElement, policyBuilderContext);

        final Element trustedCertIDElement = getSingleChildElement(httpRoutingAssertionElement, TLS_TRUSTED_CERT_IDS, true);
        assertNotNull(trustedCertIDElement);
        assertEquals(2, trustedCertIDElement.getChildNodes().getLength());
        assertEquals("2cd473fe16d98cd6b9348ffb404517bc", trustedCertIDElement.getChildNodes().item(0).getAttributes().getNamedItem(GOID_VALUE).getTextContent());
        assertEquals("28be78b936aa61bc75bd0df2089789cd", trustedCertIDElement.getChildNodes().item(1).getAttributes().getNamedItem(GOID_VALUE).getTextContent());
    }

    @NotNull
    private Element createHttpRoutingAssertionWithCertNames(Document document) {
        Element trustedCertNamesElement = createElementWithAttributesAndChildren(
                document,
                TLS_TRUSTED_CERT_NAMES,
                org.testcontainers.shaded.com.google.common.collect.ImmutableMap.of("stringArrayValue", "included"),
                createElementWithAttribute(document, PolicyXMLElements.ITEM, STRING_VALUE, "fake-cert-1"),
                createElementWithAttribute(document, PolicyXMLElements.ITEM, STRING_VALUE, "fake-cert-2")
        );

        return createElementWithChildren(
                document,
                HTTP_ROUTING_ASSERTION,
                trustedCertNamesElement
        );
    }

    @NotNull
    private TrustedCert createTrustedCertWithAnnotation(final String type, final String id) {
        TrustedCert cert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), null);
        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation(type);
        annotation.setId(id);
        annotations.add(annotation);
        cert.setAnnotations(annotations);
        return cert;
    }
}
