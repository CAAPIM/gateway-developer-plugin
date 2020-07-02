/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationType;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.MAP_BY;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingProperties.MAP_TO;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.STRING_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.VERIFY_HOSTNAME;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.junit.jupiter.api.Assertions.*;

class PolicyEntityBuilderTest {

    private Policy policy;
    private Bundle bundle;
    private Document document;
    private final String TEST_ENCASS = "EncassName";

    @BeforeEach
    void beforeEach() {
        policy = new Policy();
        policy.setPath("test/policy/path.xml");
        bundle = new Bundle();
        bundle.setDependencies(new HashSet<>());
        document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
    }

    @Test
    void build() {
        Element policyElement = createElementWithAttributesAndChildren(
                document,
                "wsp:Policy",
                ImmutableMap.of("xmlns:L7p", "http://www.layer7tech.com/ws/policy", "xmlns:wsp", "http://schemas.xmlsoap.org/ws/2002/12/policy&quot"),
                createSetVariableAssertion(document, "var", "value"),
                createIncludeAssertionElement(document, "include"),
                createHardcodedAssertionElement(document, "response")
        );
        Element encassElement = createEncapsulatedAssertionElement(document, TEST_ENCASS, "encass");
        encassElement.setAttribute(PolicyEntityBuilder.ENCASS_NAME, TEST_ENCASS);
        policyElement.appendChild(encassElement);
        document.appendChild(policyElement);

        policy.setPolicyXML(DocumentTools.INSTANCE.elementToString(document.getDocumentElement()));
        policy.setParentFolder(Folder.ROOT_FOLDER);
        policy.setGuid("policyGuid");
        policy.setId("policyID");
        policy.setName(policy.getPath());
        Encass encass = new Encass();
        encass.setGuid("encass");
        encass.setName(TEST_ENCASS);
        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation(AnnotationType.SHARED);
        annotations.add(annotation);
        policy.setAnnotations(annotations);
        bundle.getEncasses().put(TEST_ENCASS, encass);
        bundle.getPolicies().put("Policy", policy);
        Policy include = new Policy();
        include.setParentFolder(Folder.ROOT_FOLDER);
        include.setPath("test/policy/include");
        include.setName(include.getPath());
        include.setId("includeID");
        include.setGuid("includeGuid");
        annotations = new HashSet<>();
        annotation = new Annotation(AnnotationType.SHARED);
        annotations.add(annotation);
        include.setAnnotations(annotations);
        include.setPolicyXML("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<wsp:Policy xmlns:L7p=\"http://www.layer7tech.com/ws/policy\" xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\">\n" +
                "        <L7p:CommentAssertion>\n" +
                "            <L7p:Comment stringValue=\"Policy Fragment: includedPolicy\"/>\n" +
                "        </L7p:CommentAssertion>\n" +
                "    </wsp:All>\n" +
                "</wsp:Policy>");
        bundle.getPolicies().put("include", include);

        PolicyEntityBuilder builder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        final List<Entity> entities = builder.build(bundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(2, entities.size());
        entities.stream().collect(toMap(Entity::getId, identity())).forEach((i, e) -> {
            assertEquals(EntityTypes.POLICY_TYPE, e.getType());
            assertNotNull(e.getXml());
            assertTrue(e.getMappingProperties().containsKey(MAP_BY));
            assertEquals(MappingProperties.PATH, e.getMappingProperties().get(MAP_BY));
            assertTrue(e.getMappingProperties().containsKey(MAP_TO));
            assertEquals(e.getName(), e.getMappingProperties().get(MAP_TO));
            assertTrue("test/policy/include".equals(e.getName()) || "test/policy/path.xml".equals(e.getName()));
            assertTrue("includeID".equals(e.getId()) || "policyID".equals(e.getId()));
        });
    }

    @Test
    void buildDeploymentBundleWithWrongGuidAndId() {
        Element policyElement = createElementWithAttributesAndChildren(
                document,
                "wsp:Policy",
                ImmutableMap.of("xmlns:L7p", "http://www.layer7tech.com/ws/policy", "xmlns:wsp", "http://schemas.xmlsoap.org/ws/2002/12/policy&quot"),
                createSetVariableAssertion(document, "var", "value"),
                createHardcodedAssertionElement(document, "response")
        );
        Element encassElement = createEncapsulatedAssertionElement(document, TEST_ENCASS, "encass");
        encassElement.setAttribute(PolicyEntityBuilder.ENCASS_NAME, TEST_ENCASS);
        policyElement.appendChild(encassElement);
        document.appendChild(policyElement);

        policy.setPolicyXML(DocumentTools.INSTANCE.elementToString(document.getDocumentElement()));
        policy.setParentFolder(Folder.ROOT_FOLDER);
        policy.setGuid("policyGuid");
        policy.setId("policyID");
        policy.setName(policy.getPath());

        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation(AnnotationType.BUNDLE_HINTS);
        annotation.setGuid("");
        annotation.setId("");
        annotations.add(annotation);
        policy.setAnnotations(annotations);

        Encass encass = new Encass();
        encass.setGuid("encassGuid");
        encass.setName(TEST_ENCASS);
        Annotation encassAnnotation = new Annotation(AnnotationType.SHARED);
        annotations = new HashSet<>();
        annotations.add(encassAnnotation);
        encass.setAnnotations(annotations);
        bundle.getEncasses().put(TEST_ENCASS, encass);
        bundle.getPolicies().put("Policy", policy);


        AnnotatedEntity annotatedEntity = new AnnotatedEntity(encass);
        annotatedEntity.setEntityName(encass.getName());
        AnnotatedBundle annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity, new ProjectInfo("", "", ""));
        annotatedBundle.putAllEncasses(org.testcontainers.shaded.com.google.common.collect.ImmutableMap.of(TEST_ENCASS, encass));
        annotatedBundle.getPolicies().put("Policy", policy);

        PolicyEntityBuilder builder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        List<Entity> entities = builder.build(annotatedBundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());
        Entity e = entities.get(0);
        assertEquals(EntityTypes.POLICY_TYPE, e.getType());
        assertEquals(e.getId(), "policyID");

        //wrong guid and id
        annotations = new HashSet<>();
        annotation = new Annotation(AnnotationType.SHARED);
        annotations.add(annotation);
        annotation = new Annotation(AnnotationType.BUNDLE_HINTS);
        annotation.setGuid("wrongGuid");
        annotation.setId("wrongId");
        annotations.add(annotation);
        policy.setAnnotations(annotations);
        policy.setAnnotatedEntity(null);

        builder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        entities = builder.build(annotatedBundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());
        e = entities.get(0);
        assertEquals(EntityTypes.POLICY_TYPE, e.getType());
        assertEquals(e.getId(), "policyID");
    }

    @Test
    void testPrepareSetVariableAssertionNoENV() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "my-var", "base64Text");
        document.appendChild(setVariableAssertionElement);
        String prefix = "prefix";

        PolicyEntityBuilder.prepareSetVariableAssertion(prefix, document, setVariableAssertionElement);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("my-var", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        String b64 = expressionElement.getAttribute(PolicyEntityBuilder.STRING_VALUE);
        assertEquals(Base64.getEncoder().encodeToString("base64Text".getBytes(StandardCharsets.UTF_8)), b64);
    }

    @Test
    void testPrepareSetVariableAssertionUnescaping() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "userSession", "&lt;usersession&gt;\n"
                + " &lt;user&gt;&lt;![CDATA[${current.username}]]&gt;&lt;/user&gt;\n"
                + " &lt;role&gt;&lt;![CDATA[${current.user.role}]]&gt;&lt;/role&gt;\n"
                + " &lt;lookupUser&gt;&lt;![CDATA[${lookupUser}]]&gt;&lt;/lookupUser&gt;\n"
                + " &lt;synchToken&gt;&lt;![CDATA[${xpathSynchToken.result}]]&gt;&lt;/synchToken&gt;\n"
                + "&lt;/usersession&gt;");
        document.appendChild(setVariableAssertionElement);
        String prefix = "prefix";

        PolicyEntityBuilder.prepareSetVariableAssertion(prefix, document, setVariableAssertionElement);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("userSession", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        String expectedB64 = "PHVzZXJzZXNzaW9uPgogPHVzZXI+PCFbQ0RBVEFbJHtjdXJyZW50LnVzZXJuYW1lfV1dPjwvdXNlcj4KIDxyb2xlPjwhW0NEQVRBWyR7Y3VycmVudC51c2VyLnJvbGV9XV0+PC9yb2xlPgogPGxvb2t1cFVzZXI+PCFbQ0RBVEFbJHtsb29rdXBVc2VyfV1dPjwvbG9va3VwVXNlcj4KIDxzeW5jaFRva2VuPjwhW0NEQVRBWyR7eHBhdGhTeW5jaFRva2VuLnJlc3VsdH1dXT48L3N5bmNoVG9rZW4+CjwvdXNlcnNlc3Npb24+";
        String b64 = expressionElement.getAttribute(PolicyEntityBuilder.STRING_VALUE);
        assertEquals(expectedB64, b64);

        String value = new String(decodeBase64(b64));
        assertEquals(
                "<usersession>\n"
                        + " <user><![CDATA[${current.username}]]></user>\n"
                        + " <role><![CDATA[${current.user.role}]]></role>\n"
                        + " <lookupUser><![CDATA[${lookupUser}]]></lookupUser>\n"
                        + " <synchToken><![CDATA[${xpathSynchToken.result}]]></synchToken>\n"
                        + "</usersession>", value);
    }

    @Test
    void testPrepareSetVariableAssertionENV() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "ENV.my-var", "base64Text");
        document.appendChild(setVariableAssertionElement);
        String prefix = "prefix";

        PolicyEntityBuilder.prepareSetVariableAssertion(prefix, document, setVariableAssertionElement);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("ENV.my-var", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        assertEquals(PREFIX_ENV + prefix + ".my-var", expressionElement.getAttribute(PolicyEntityBuilder.ENV_PARAM_NAME));
        assertFalse(expressionElement.hasAttribute(PolicyEntityBuilder.STRING_VALUE));
    }

    @Test
    void testPrepareSetVariableAssertionMissingVariableToSet() {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element expression = document.createElement(EXPRESSION);
        expression.appendChild(document.createCDATASection("ashdkjsah"));
        setVariableAssertion.appendChild(expression);
        String prefix = "prefix";

        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareSetVariableAssertion(prefix, document, setVariableAssertion));
    }

    @Test
    void testPrepareSetVariableAssertionMissingBase64Value() throws DocumentParseException {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element variableToSet = document.createElement(VARIABLE_TO_SET);
        variableToSet.setAttribute(PolicyEntityBuilder.STRING_VALUE, "my.var");
        setVariableAssertion.appendChild(variableToSet);
        String prefix = "prefix";

        PolicyEntityBuilder.prepareSetVariableAssertion(prefix, document, setVariableAssertion);

        Element nameElement = getSingleElement(setVariableAssertion, VARIABLE_TO_SET);
        assertEquals("my.var", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleChildElement(setVariableAssertion, BASE_64_EXPRESSION, true);
        assertNull(expressionElement);
    }

    @Test
    void testPrepareSetVariableAssertionNotENVEmptyValue() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "my-var", null);
        document.appendChild(setVariableAssertionElement);
        String prefix = "prefix";

        PolicyEntityBuilder.prepareSetVariableAssertion(prefix, document, setVariableAssertionElement);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("my-var", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        String b64 = expressionElement.getAttribute(PolicyEntityBuilder.STRING_VALUE);
        assertTrue(b64.isEmpty());
    }

    @Test
    void testPrepareSetVariableAssertionNotENVTextNode() throws DocumentParseException {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element expression = document.createElement(EXPRESSION);
        expression.setTextContent("my \n Text \r\n Content");
        setVariableAssertion.appendChild(expression);
        Element variableToSet = document.createElement(VARIABLE_TO_SET);
        variableToSet.setAttribute(PolicyEntityBuilder.STRING_VALUE, "my-var");
        setVariableAssertion.appendChild(variableToSet);
        String prefix = "prefix";

        PolicyEntityBuilder.prepareSetVariableAssertion(prefix, document, setVariableAssertion);

        Element nameElement = getSingleElement(setVariableAssertion, VARIABLE_TO_SET);
        assertEquals("my-var", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertion, BASE_64_EXPRESSION);
        String b64 = expressionElement.getAttribute(PolicyEntityBuilder.STRING_VALUE);
        assertEquals(Base64.getEncoder().encodeToString("my \n Text \r\n Content".getBytes(StandardCharsets.UTF_8)), b64);
    }

    @Test
    void testPrepareSetVariableAssertionNotENVElementNode() {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element expression = document.createElement(EXPRESSION);
        expression.appendChild(document.createElement("ashdkjsah"));
        setVariableAssertion.appendChild(expression);
        Element variableToSet = document.createElement(VARIABLE_TO_SET);
        variableToSet.setAttribute(PolicyEntityBuilder.STRING_VALUE, "my-var");
        setVariableAssertion.appendChild(variableToSet);
        String prefix = "prefix";

        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareSetVariableAssertion(prefix, document, setVariableAssertion));
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
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        policyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement, null);

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
        AnnotatedEntity annotatedEntity = new AnnotatedEntity(encass);
        AnnotatedBundle annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity, null);
        annotatedBundle.putAllEncasses(org.testcontainers.shaded.com.google.common.collect.ImmutableMap.of(TEST_ENCASS, encass));
        annotatedBundle.getPolicies().put(policyPath, policy);
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        document.appendChild(encapsulatedAssertionElement);
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        policyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement, annotatedBundle);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        //wrong guid and goid
        annotatedEntity = new AnnotatedEntity(encass);
        annotations = new HashSet<>();
        annotations.add(new Annotation(AnnotationType.SHARED));
        annotation = new Annotation(AnnotationType.BUNDLE_HINTS);
        annotation.setGuid("wrongGuid");
        annotation.setId("wrongId");
        annotations.add(annotation);
        encass.setAnnotations(annotations);
        encass.setAnnotatedEntity(null);
        annotatedBundle = new AnnotatedBundle(bundle, annotatedEntity, null);
        annotatedBundle.putAllEncasses(org.testcontainers.shaded.com.google.common.collect.ImmutableMap.of(TEST_ENCASS, encass));
        annotatedBundle.getPolicies().put(policyPath, policy);
        encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        policyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement, annotatedBundle);

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
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        policyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement, null);

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

        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        policyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement, null);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingEncass() {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        document.appendChild(encapsulatedAssertionElement);
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        EntityBuilderException exception = assertThrows(EntityBuilderException.class, () -> policyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement, null));
        assertTrue(exception.getMessage().contains(TEST_ENCASS));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingEncassWithNoOp() throws DocumentParseException {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);
        document.appendChild(encapsulatedAssertionElement);

        Element noOpElement = document.createElement(NO_OP_IF_CONFIG_MISSING);
        encapsulatedAssertionElement.appendChild(noOpElement);
        noOpElement.setAttribute(PolicyEntityBuilder.BOOLEAN_VALUE, "true");
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        policyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement, null);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(PolicyEntityBuilder.ZERO_GUID, guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingPolicyPath() {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, "my-encass", "ad620794-a27f-4d94-85b7-669ba838367b");
        document.appendChild(encapsulatedAssertionElement);
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        EntityBuilderException exception = assertThrows(EntityBuilderException.class, () -> policyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement, null));
        assertTrue(exception.getMessage().contains(policy.getPath()));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingPolicyPathWithNoOp() throws DocumentParseException {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, "my-encass", "ad620794-a27f-4d94-85b7-669ba838367b");
        document.appendChild(encapsulatedAssertionElement);

        Element noOpElement = document.createElement(NO_OP_IF_CONFIG_MISSING);
        encapsulatedAssertionElement.appendChild(noOpElement);
        noOpElement.setAttribute(PolicyEntityBuilder.BOOLEAN_VALUE, "true");
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        policyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement, null);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals("my-encass", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals("ad620794-a27f-4d94-85b7-669ba838367b", guidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
    }

    @Test
    void testPrepareHardcodedResponseAssertion() throws DocumentParseException {
        Element hardcodedAssertionElement = createHardcodedAssertionElement(document, "assertion body");
        document.appendChild(hardcodedAssertionElement);

        PolicyEntityBuilder.prepareHardcodedResponseAssertion(document, hardcodedAssertionElement);

        Element b64BodyElement = getSingleElement(hardcodedAssertionElement, BASE_64_RESPONSE_BODY);
        String b64 = b64BodyElement.getAttribute(PolicyEntityBuilder.STRING_VALUE);
        assertEquals(Base64.getEncoder().encodeToString("assertion body".getBytes(StandardCharsets.UTF_8)), b64);

    }

    @Test
    void testPrepareIncludeAssertion() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertionElement = createIncludeAssertionElement(document, policyPath);
        document.appendChild(includeAssertionElement);

        PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertionElement, new AnnotatedBundle(bundle, null, null));

        Element policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
        assertEquals(policy.getGuid(), policyGuidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
        assertFalse(policyGuidElement.hasAttribute(PolicyEntityBuilder.POLICY_PATH));
    }

    @Test
    void testPrepareIncludeAssertionEncodedPath() throws DocumentParseException {
        String policyPath = "my/policy-_¯-¯_/_¯-¯_-path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertionElement = createIncludeAssertionElement(document, policyPath);
        document.appendChild(includeAssertionElement);

        PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertionElement, new AnnotatedBundle(bundle, null, null));

        Element policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
        assertEquals(policy.getGuid(), policyGuidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
        assertFalse(policyGuidElement.hasAttribute(PolicyEntityBuilder.POLICY_PATH));
    }

    @Test
    void testPrepareIncludeAssertionNoPolicyGuid() {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertion = document.createElement(INCLUDE);
        document.appendChild(includeAssertion);

        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertion, new AnnotatedBundle(bundle, null, null)));
    }

    @Test
    void testPrepareIncludeAssertionNoPolicyFound() {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertionElement = createIncludeAssertionElement(document, "some/other/path.xml");
        document.appendChild(includeAssertionElement);

        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertionElement, new AnnotatedBundle(bundle, null, null)));
    }

    @Test
    void testPrepareIncludeAssertionPolicyInDependentBundle() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        policy.setId("id1");
        policy.setName("path.xml");
        Bundle dependentBundle = new Bundle();
        dependentBundle.getPolicies().put(policyPath, policy);
        bundle.getDependencies().add(dependentBundle);

        bundle.getDependencies().add(new Bundle());

        Element includeAssertionElement = createIncludeAssertionElement(document, policyPath);
        document.appendChild(includeAssertionElement);

        PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertionElement, new AnnotatedBundle(bundle, null, null));

        Element policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
        assertEquals(policy.getGuid(), policyGuidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
        assertFalse(policyGuidElement.hasAttribute(PolicyEntityBuilder.POLICY_PATH));
    }

    @Test
    void testPrepareIncludeAssertionPolicyInMultipleDependentBundle() {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        Bundle dependentBundle = new Bundle();
        dependentBundle.getPolicies().put(policyPath, policy);
        bundle.getDependencies().add(dependentBundle);
        Bundle dependentBundle2 = new Bundle();
        dependentBundle2.getPolicies().put(policyPath, policy);
        bundle.getDependencies().add(dependentBundle2);

        Element includeAssertionElement = createIncludeAssertionElement(document, policyPath);
        document.appendChild(includeAssertionElement);

        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertionElement, new AnnotatedBundle(bundle, null, null)));
    }

    @Test
    void maybeAddPolicy() {
        Policy policy1 = new Policy();
        Policy policy2 = new Policy();
        policy1.getDependencies().add(policy2);

        ArrayList<Policy> orderedPolicies = new ArrayList<>();
        HashSet<Policy> seenPolicies = new HashSet<>();
        PolicyEntityBuilder.maybeAddPolicy(bundle, policy1, orderedPolicies, seenPolicies);

        assertEquals(2, orderedPolicies.size());
        assertEquals(policy1, orderedPolicies.get(1));
        assertEquals(policy2, orderedPolicies.get(0));

        PolicyEntityBuilder.maybeAddPolicy(bundle, policy2, orderedPolicies, seenPolicies);

        assertEquals(2, orderedPolicies.size());
        assertEquals(policy1, orderedPolicies.get(1));
        assertEquals(policy2, orderedPolicies.get(0));

        // test dependency loop
        policy2.getDependencies().add(policy1);
        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.maybeAddPolicy(bundle, policy2, new ArrayList<>(), new HashSet<>()));
    }

    @Test
    void buildPolicyEntityTest() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());

        Policy policyToBuild = new Policy();
        policyToBuild.setPath("/path");
        policyToBuild.setName("path");
        policyToBuild.setId("policy-id");
        policyToBuild.setGuid("policy-guid-123");
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, null, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
    }

    @Test
    void buildPolicyEntityTestPBS() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());

        Policy policyToBuild = new Policy();
        policyToBuild.setPath("my/policy/path.xml");
        policyToBuild.setName("path");
        policyToBuild.setId("policy-id");
        policyToBuild.setGuid("policy-guid-123");
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        PolicyBackedService policyBackedService = new PolicyBackedService();
        policyBackedService.setInterfaceName("pbs-interface");
        policyBackedService.setOperations(Sets.newHashSet(new PolicyBackedServiceOperation("my-op", policyToBuild.getPath())));
        bundle.getPolicyBackedServices().put("pbs", policyBackedService);
        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, null, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
    }

    @Test
    void buildPolicyEntityTestGlobal() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());

        Policy policyToBuild = new Policy();
        policyToBuild.setPath("my/policy/global.xml");
        policyToBuild.setName("global.xml");
        policyToBuild.setId("global-policy-id");
        policyToBuild.setGuid("global-policy-guid-123");
        policyToBuild.setTag("global-policy");
        policyToBuild.setPolicyType(PolicyType.GLOBAL);
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, null, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
        assertEquals(policyToBuild.getPath(), policyEntity.getName());
        assertNotNull(policyEntity.getXml());
        Element policyDetail = getSingleChildElement(policyEntity.getXml(), POLICY_DETAIL);
        assertNotNull(policyDetail);
        String type = getSingleChildElementTextContent(policyDetail, POLICY_TYPE);
        assertNotNull(type);
        assertEquals(PolicyType.GLOBAL.getType(), type);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(policyDetail, PROPERTIES), PROPERTIES);
        assertFalse(properties.isEmpty());
        assertEquals("global-policy", properties.get(PropertyConstants.PROPERTY_TAG));
        assertNull(properties.get(PropertyConstants.PROPERTY_SUBTAG));
    }

    @Test
    void buildPolicyEntityTestInternal() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());

        Policy policyToBuild = new Policy();
        policyToBuild.setPath("my/policy/internal.xml");
        policyToBuild.setName("internal.xml");
        policyToBuild.setId("internal-policy-id");
        policyToBuild.setGuid("internal-policy-guid-123");
        policyToBuild.setTag("internal-policy");
        policyToBuild.setPolicyType(PolicyType.INTERNAL);
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, null, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
        assertEquals(policyToBuild.getPath(), policyEntity.getName());
        assertNotNull(policyEntity.getXml());
        Element policyDetail = getSingleChildElement(policyEntity.getXml(), POLICY_DETAIL);
        assertNotNull(policyDetail);
        String type = getSingleChildElementTextContent(policyDetail, POLICY_TYPE);
        assertNotNull(type);
        assertEquals(PolicyType.INTERNAL.getType(), type);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(policyDetail, PROPERTIES), PROPERTIES);
        assertFalse(properties.isEmpty());
        assertEquals("internal-policy", properties.get(PropertyConstants.PROPERTY_TAG));
        assertNull(properties.get(PropertyConstants.PROPERTY_SUBTAG));
    }

    @Test
    void testPrepareRoutingAssertionCertificateIds() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        bundle.putAllTrustedCerts(ImmutableMap.of("fake-cert-1", createTrustedCertWithAnnotation(AnnotationType.BUNDLE_HINTS, "2cd473fe16d98cd6b9348ffb404517bc")));
        bundle.putAllTrustedCerts(ImmutableMap.of("fake-cert-2", createTrustedCertWithAnnotation(AnnotationType.BUNDLE_HINTS, "28be78b936aa61bc75bd0df2089789cd")));

        Element httpRoutingAssertionElement = createHttpRoutingAssertionWithCertNames(document);
        policyEntityBuilder.prepareRoutingAssertionCertificateIds(document, bundle, httpRoutingAssertionElement);

        final Element trustedCertIDElement = getSingleChildElement(httpRoutingAssertionElement, TLS_TRUSTED_CERT_IDS, true);
        assertNotNull(trustedCertIDElement);
        assertEquals(2, trustedCertIDElement.getChildNodes().getLength());
        assertEquals("2cd473fe16d98cd6b9348ffb404517bc", trustedCertIDElement.getChildNodes().item(0).getAttributes().getNamedItem(GOID_VALUE).getTextContent());
        assertEquals("28be78b936aa61bc75bd0df2089789cd", trustedCertIDElement.getChildNodes().item(1).getAttributes().getNamedItem(GOID_VALUE).getTextContent());
    }

    @Test
    void testPrepareHttp2AssertionClientConfigIds() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        bundle.getGenericEntities().put("http2client",
                createHttp2ClientConfigWithAnnotation(AnnotationType.BUNDLE_HINTS, "a2097d7f50280e9411c277aafedc180d"));

        Element http2RoutingAssertionElement = createHttp2Assertion(document);
        policyEntityBuilder.prepareHttp2RoutingAssertion(document, bundle, http2RoutingAssertionElement);

        final Element clientConfigId = getSingleChildElement(http2RoutingAssertionElement, HTTP2_CLIENT_CONFIG_GOID, true);
        assertNotNull(clientConfigId);
        assertEquals("a2097d7f50280e9411c277aafedc180d", clientConfigId.getAttributes().getNamedItem(GOID_VALUE).getTextContent());
    }

    @Test
    void testPrepareMqRoutingAssertionIds() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentTools.INSTANCE, new IdGenerator());
        bundle.putAllSsgActiveConnectors(ImmutableMap.of("activeConnector1", createActiveConnectorWithAnnotation(AnnotationType.BUNDLE_HINTS, "2cd473fe16d98cd6b9348ffb404517bc")));

        Element mqRoutingAssertionElement = createMqRoutingAssertion(document);
        policyEntityBuilder.prepareMQRoutingAssertion(document, bundle, mqRoutingAssertionElement);

        final Element connectorGoid = getSingleChildElement(mqRoutingAssertionElement, ACTIVE_CONNECTOR_GOID, true);
        final Element connectorid = getSingleChildElement(mqRoutingAssertionElement, ACTIVE_CONNECTOR_ID, true);
        assertNotNull(connectorGoid);
        assertNotNull(connectorid);
        assertEquals("2cd473fe16d98cd6b9348ffb404517bc", connectorGoid.getAttributes().getNamedItem(GOID_VALUE).getTextContent());
        assertEquals("2cd473fe16d98cd6b9348ffb404517bc", connectorid.getAttributes().getNamedItem(GOID_VALUE).getTextContent());
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
    private Element createHttp2Assertion(Document document) {
        return createElementWithChildren(
                document,
                HTTP2_ROUTING_ASSERTION,
                createElementWithAttribute(document, "L7p:ProtectedServiceUrl", STRING_VALUE, "http://apim-hugh-new.lvn.broadcom.net:90"),
                createElementWithAttribute(document, HTTP2_CLIENT_CONFIG_NAME, STRING_VALUE, "http2client")
        );
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
    private TrustedCert createTrustedCertWithAnnotation(final String type, final String id) {
        TrustedCert cert = new TrustedCert(ImmutableMap.of(VERIFY_HOSTNAME, true), null);
        Set<Annotation> annotations = new HashSet<>();
        Annotation annotation = new Annotation(type);
        annotation.setId(id);
        annotations.add(annotation);
        cert.setAnnotations(annotations);
        return cert;
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

    private Element createIncludeAssertionElement(Document document, String policyPath) {
        Element includeAssertion = document.createElement(INCLUDE);

        Element guidElement = document.createElement(POLICY_GUID);
        guidElement.setAttribute(PolicyEntityBuilder.POLICY_PATH, policyPath);
        includeAssertion.appendChild(guidElement);
        return includeAssertion;
    }

    private Element createHardcodedAssertionElement(Document document, String body) {
        Element hardcodedAssertion = document.createElement(HARDCODED_RESPONSE);

        Element bodyElement = document.createElement(RESPONSE_BODY);
        bodyElement.appendChild(document.createCDATASection(body));
        hardcodedAssertion.appendChild(bodyElement);
        return hardcodedAssertion;
    }

    @NotNull
    private Element createSetVariableAssertion(Document document, String variableName, String variableValue) {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);

        Element expression = document.createElement(EXPRESSION);
        if (variableValue != null) {
            expression.appendChild(document.createCDATASection(variableValue));
        }
        setVariableAssertion.appendChild(expression);
        Element variableToSet = document.createElement(VARIABLE_TO_SET);
        variableToSet.setAttribute(PolicyEntityBuilder.STRING_VALUE, variableName);
        setVariableAssertion.appendChild(variableToSet);
        return setVariableAssertion;
    }

    private Element createEncapsulatedAssertionElement(Document document) {
        Element setEncapsulatedAssertion = document.createElement(ENCAPSULATED);

        setEncapsulatedAssertion.setAttribute(PolicyEntityBuilder.ENCASS_NAME, TEST_ENCASS);
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
}