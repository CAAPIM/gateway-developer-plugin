package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.DEPLOYMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static org.junit.jupiter.api.Assertions.*;

public class EncapsulatedAssertionBuilderTest {
    private Policy policy;
    private Bundle bundle;
    private Document document;
    private final String TEST_ENCASS = "EncassName";
    private EncapsulatedAssertionBuilder encapsulatedAssertionBuilder = new EncapsulatedAssertionBuilder();
    private PolicyBuilderContext policyBuilderContext;
    private static final ProjectInfo projectInfo = new ProjectInfo("my-bundle", "my-bundle-group", "1.0");

    @BeforeEach
    void beforeEach() {
        policy = new Policy();
        policy.setPath("test/policy/path.xml");
        bundle = new Bundle();
        bundle.setDependencies(new HashSet<>());
        document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
    }

    private Element createEncapsulatedAssertionElement(Document document) {
        Element setEncapsulatedAssertion = document.createElement(ENCAPSULATED);

        setEncapsulatedAssertion.setAttribute(EncapsulatedAssertionBuilder.ENCASS_NAME, TEST_ENCASS);
        return setEncapsulatedAssertion;
    }

    @NotNull
    private Element createEncapsulatedAssertionElement(Document document, String name, String guid) {
        Element setEncapsulatedAssertion = document.createElement(ENCAPSULATED);

        Element nameElement = document.createElement(ENCAPSULATED_ASSERTION_CONFIG_NAME);
        setEncapsulatedAssertion.appendChild(nameElement);
        nameElement.setAttribute(PolicyEntityBuilder.STRING_VALUE, name);

        Element guidElement = document.createElement(ENCAPSULATED_ASSERTION_CONFIG_GUID);
        setEncapsulatedAssertion.appendChild(guidElement);
        guidElement.setAttribute(PolicyEntityBuilder.STRING_VALUE, guid);
        return setEncapsulatedAssertion;
    }

    @Test
    void testPrepareEncapsulatedAssertion() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Encass encass = new Encass();
        encass.setGuid("123");
        encass.setName(TEST_ENCASS);
        encass.setPolicy(policyPath);
        bundle.getEncasses().put(TEST_ENCASS, encass);
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        document.appendChild(encapsulatedAssertionElement);
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(null);
        encapsulatedAssertionBuilder.buildAssertionElement(encapsulatedAssertionElement, policyBuilderContext);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionWithWrongGuidAndGoid() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Encass encass = new Encass();
        encass.setGuid("123");
        encass.setName(TEST_ENCASS);
        encass.setPolicy(policyPath);
        Set<Annotation> annotations = new HashSet<>();
        annotations.add(new Annotation(AnnotationType.SHARED));
        Annotation annotation = new Annotation(AnnotationType.BUNDLE_HINTS);
        annotation.setGuid("");
        annotation.setId("");
        annotations.add(annotation);
        encass.setAnnotations(annotations);
        bundle.getEncasses().put(TEST_ENCASS, encass);

        //empty guid and goid
        AnnotatedEntity annotatedEntity = encass.getAnnotatedEntity();
        AnnotatedBundle annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity, projectInfo);
        annotatedBundle.putAllEncasses(org.testcontainers.shaded.com.google.common.collect.ImmutableMap.of(TEST_ENCASS, encass));
        annotatedBundle.getPolicies().put(policyPath, policy);
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        document.appendChild(encapsulatedAssertionElement);
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(annotatedBundle);
        encapsulatedAssertionBuilder.buildAssertionElement(encapsulatedAssertionElement, policyBuilderContext);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        String name = annotatedBundle.applyUniqueName(TEST_ENCASS, DEPLOYMENT, false);
        assertEquals(name, nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        //wrong guid and goid
        annotatedEntity = encass.getAnnotatedEntity();
        annotations = new HashSet<>();
        annotations.add(new Annotation(AnnotationType.SHARED));
        annotation = new Annotation(AnnotationType.BUNDLE_HINTS);
        annotation.setGuid("wrongGuid");
        annotation.setId("wrongId");
        annotations.add(annotation);
        encass.setAnnotations(annotations);
        encass.setAnnotatedEntity(null);
        annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity, projectInfo);
        annotatedBundle.putAllEncasses(org.testcontainers.shaded.com.google.common.collect.ImmutableMap.of(TEST_ENCASS, encass));
        annotatedBundle.getPolicies().put(policyPath, policy);
        encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(annotatedBundle);
        encapsulatedAssertionBuilder.buildAssertionElement(encapsulatedAssertionElement, policyBuilderContext);

        guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionEncoded() throws DocumentParseException {
        String policyPath = "my/policy-_¯-¯_/_¯-¯_-path.xml";
        Encass encass = new Encass();
        encass.setGuid("123");
        encass.setName(TEST_ENCASS);
        encass.setPolicy(policyPath);
        bundle.getEncasses().put(TEST_ENCASS, encass);
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        document.appendChild(encapsulatedAssertionElement);
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(null);
        encapsulatedAssertionBuilder.buildAssertionElement(encapsulatedAssertionElement, policyBuilderContext);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionEncassInDependencyBundle() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Encass encass = new Encass();
        encass.setGuid("123");
        encass.setId("id1");
        encass.setPolicy(policyPath);
        encass.setName(TEST_ENCASS);

        Bundle dependencyBundle = new Bundle();
        dependencyBundle.getEncasses().put(TEST_ENCASS, encass);
        bundle.getDependencies().add(dependencyBundle);
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        document.appendChild(encapsulatedAssertionElement);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(null);
        encapsulatedAssertionBuilder.buildAssertionElement(encapsulatedAssertionElement, policyBuilderContext);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingEncass() {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        document.appendChild(encapsulatedAssertionElement);
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(null);
        EntityBuilderException exception = assertThrows(EntityBuilderException.class, () -> encapsulatedAssertionBuilder.buildAssertionElement(encapsulatedAssertionElement, policyBuilderContext));
        assertTrue(exception.getMessage().contains(TEST_ENCASS));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingEncassWithNoOp() throws DocumentParseException {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        document.appendChild(encapsulatedAssertionElement);

        Element noOpElement = document.createElement(NO_OP_IF_CONFIG_MISSING);
        encapsulatedAssertionElement.appendChild(noOpElement);
        noOpElement.setAttribute(EncapsulatedAssertionBuilder.BOOLEAN_VALUE, "true");
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(null);
        encapsulatedAssertionBuilder.buildAssertionElement(encapsulatedAssertionElement, policyBuilderContext);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(PolicyEntityBuilder.ZERO_GUID, guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingPolicyPath() {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, "my-encass", "ad620794-a27f-4d94-85b7-669ba838367b");
        document.appendChild(encapsulatedAssertionElement);
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(null);
        EntityBuilderException exception = assertThrows(EntityBuilderException.class, () -> encapsulatedAssertionBuilder.buildAssertionElement(encapsulatedAssertionElement, policyBuilderContext));
        assertTrue(exception.getMessage().contains(policy.getPath()));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingPolicyPathWithNoOp() throws DocumentParseException {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, "my-encass", "ad620794-a27f-4d94-85b7-669ba838367b");
        document.appendChild(encapsulatedAssertionElement);

        Element noOpElement = document.createElement(NO_OP_IF_CONFIG_MISSING);
        encapsulatedAssertionElement.appendChild(noOpElement);
        noOpElement.setAttribute(EncapsulatedAssertionBuilder.BOOLEAN_VALUE, "true");
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(null);
        encapsulatedAssertionBuilder.buildAssertionElement(encapsulatedAssertionElement, policyBuilderContext);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals("my-encass", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals("ad620794-a27f-4d94-85b7-669ba838367b", guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
    }
}
