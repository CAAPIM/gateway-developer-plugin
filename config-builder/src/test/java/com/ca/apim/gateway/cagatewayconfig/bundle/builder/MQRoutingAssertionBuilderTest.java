package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.SsgActiveConnector;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
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
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.GOID_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MQRoutingAssertionBuilderTest {

    private Policy policy;
    private Bundle bundle;
    private Document document;
    private MQRoutingAssertionBuilder mqRoutingAssertionBuilder = new MQRoutingAssertionBuilder();
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
    void testPrepareMqRoutingAssertionIds() {
        bundle.putAllSsgActiveConnectors(ImmutableMap.of("activeConnector1", createActiveConnectorWithAnnotation(AnnotationType.BUNDLE_HINTS, "2cd473fe16d98cd6b9348ffb404517bc")));

        Element mqRoutingAssertionElement = createMqRoutingAssertion(document);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        mqRoutingAssertionBuilder.buildAssertionElement(mqRoutingAssertionElement, policyBuilderContext);

        final Element connectorGoid = getSingleChildElement(mqRoutingAssertionElement, ACTIVE_CONNECTOR_GOID, true);
        final Element connectorid = getSingleChildElement(mqRoutingAssertionElement, ACTIVE_CONNECTOR_ID, true);
        assertNotNull(connectorGoid);
        assertNotNull(connectorid);
        assertEquals("2cd473fe16d98cd6b9348ffb404517bc", connectorGoid.getAttributes().getNamedItem(GOID_VALUE).getTextContent());
        assertEquals("2cd473fe16d98cd6b9348ffb404517bc", connectorid.getAttributes().getNamedItem(GOID_VALUE).getTextContent());
    }

    @NotNull
    private Element createMqRoutingAssertion(Document document) {
        return createElementWithAttributesAndChildren(
                document,
                MQ_ROUTING_ASSERTION,
                org.testcontainers.shaded.com.google.common.collect.ImmutableMap.of("stringArrayValue", "included"),
                createElementWithAttribute(document, PolicyXMLElements.ACTIVE_CONNECTOR_NAME, STRING_VALUE, "activeConnector1")
        );

    }

    @NotNull
    private SsgActiveConnector createActiveConnectorWithAnnotation(final String type, final String id) {
        SsgActiveConnector activeConnector = new SsgActiveConnector();
        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation(type);
        annotation.setId(id);
        annotations.add(annotation);
        activeConnector.setAnnotations(annotations);
        return activeConnector;
    }
}
