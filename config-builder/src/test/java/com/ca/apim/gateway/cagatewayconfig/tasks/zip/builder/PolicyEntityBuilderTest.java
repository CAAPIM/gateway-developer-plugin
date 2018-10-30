/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants;
import com.ca.apim.gateway.cagatewayconfig.util.string.EncodeDecodeUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.PolicyEntityBuilder.BOOLEAN_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.PolicyEntityBuilder.*;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.PolicyEntityBuilder.STRING_VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class PolicyEntityBuilderTest {

    private Policy policy;
    private Bundle bundle;
    private Document document;
    private final String TEST_ENCASS = "EncassName";

    @BeforeEach
    void beforeEach() {
        policy = new Policy();
        policy.setPath("/test/policy/path.xml");
        bundle = new Bundle();
        bundle.setDependencies(new HashSet<>());
        document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
    }

    @Test
    void testPrepareSetVariableAssertionNoENV() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "my-var", "base64Text");
        PolicyEntityBuilder.prepareSetVariableAssertion(document, setVariableAssertionElement);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("my-var", nameElement.getAttribute(STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        String b64 = expressionElement.getAttribute(STRING_VALUE);
        assertEquals(Base64.getEncoder().encodeToString("base64Text".getBytes(StandardCharsets.UTF_8)), b64);
    }

    @Test
    void testPrepareSetVariableAssertionENV() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "ENV.my-var", "base64Text");
        PolicyEntityBuilder.prepareSetVariableAssertion(document, setVariableAssertionElement);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("ENV.my-var", nameElement.getAttribute(STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        assertEquals("ENV.my-var", expressionElement.getAttribute(ENV_PARAM_NAME));
        assertFalse(expressionElement.hasAttribute(STRING_VALUE));
    }

    @Test
    void testPrepareSetVariableAssertionMissingVariableToSet() {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element expression = document.createElement(EXPRESSION);
        expression.appendChild(document.createCDATASection("ashdkjsah"));
        setVariableAssertion.appendChild(expression);

        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareSetVariableAssertion(document, setVariableAssertion));
    }

    @Test
    void testPrepareSetVariableAssertionMissingBase64Value() throws DocumentParseException {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element variableToSet = document.createElement(VARIABLE_TO_SET);
        variableToSet.setAttribute(STRING_VALUE, "my.var");
        setVariableAssertion.appendChild(variableToSet);
        PolicyEntityBuilder.prepareSetVariableAssertion(document, setVariableAssertion);

        Element nameElement = getSingleElement(setVariableAssertion, VARIABLE_TO_SET);
        assertEquals("my.var", nameElement.getAttribute(STRING_VALUE));

        Element expressionElement = getSingleChildElement(setVariableAssertion, BASE_64_EXPRESSION, true);
        assertNull(expressionElement);
    }

    @Test
    void testPrepareSetVariableAssertionNotENVEmptyValue() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "my-var", null);
        PolicyEntityBuilder.prepareSetVariableAssertion(document, setVariableAssertionElement);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("my-var", nameElement.getAttribute(STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        String b64 = expressionElement.getAttribute(STRING_VALUE);
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
        variableToSet.setAttribute(STRING_VALUE, "my-var");
        setVariableAssertion.appendChild(variableToSet);

        PolicyEntityBuilder.prepareSetVariableAssertion(document, setVariableAssertion);

        Element nameElement = getSingleElement(setVariableAssertion, VARIABLE_TO_SET);
        assertEquals("my-var", nameElement.getAttribute(STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertion, BASE_64_EXPRESSION);
        String b64 = expressionElement.getAttribute(STRING_VALUE);
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
        variableToSet.setAttribute(STRING_VALUE, "my-var");
        setVariableAssertion.appendChild(variableToSet);

        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareSetVariableAssertion(document, setVariableAssertion));
    }

    @Test
    void testPrepareEncapsulatedAssertion() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Encass encass = new Encass();
        encass.setGuid("123");
        encass.setPolicy(policyPath);
        bundle.getEncasses().put(TEST_ENCASS, encass);
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);

        PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionEncoded() throws DocumentParseException {
        String policyPath = "my/policy-_¯-¯_/_¯-¯_-path.xml";
        Encass encass = new Encass();
        encass.setGuid("123");
        encass.setPolicy(policyPath);
        bundle.getEncasses().put(TEST_ENCASS, encass);
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);

        PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionEncassInDependencyBundle() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Encass encass = new Encass();
        encass.setGuid("123");
        encass.setPolicy(policyPath);
        Bundle dependencyBundle = new Bundle();
        dependencyBundle.getEncasses().put(TEST_ENCASS, encass);
        bundle.getDependencies().add(dependencyBundle);
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);

        PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionEncassInMultipleDependencyBundle() {
        String policyPath = "my/policy/path.xml";
        Encass encass = new Encass();
        encass.setGuid("123");
        encass.setPolicy(policyPath);
        Bundle dependencyBundle = new Bundle();
        dependencyBundle.getEncasses().put(TEST_ENCASS, encass);
        bundle.getDependencies().add(dependencyBundle);

        Bundle dependencyBundle2 = new Bundle();
        dependencyBundle2.getEncasses().put(TEST_ENCASS, encass);
        bundle.getDependencies().add(dependencyBundle2);

        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);

        EntityBuilderException exception = assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement));
        assertTrue(exception.getMessage().contains(TEST_ENCASS));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingEncass() {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);

        EntityBuilderException exception = assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement));
        assertTrue(exception.getMessage().contains(TEST_ENCASS));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingEncassWithNoOp() throws DocumentParseException {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document);

        Element noOpElement = document.createElement(NO_OP_IF_CONFIG_MISSING);
        encapsulatedAssertionElement.appendChild(noOpElement);
        noOpElement.setAttribute(BOOLEAN_VALUE, "true");

        PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(TEST_ENCASS, nameElement.getAttribute(STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(ZERO_GUID, guidElement.getAttribute(STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingPolicyPath() {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, "my-encass", "ad620794-a27f-4d94-85b7-669ba838367b");

        EntityBuilderException exception = assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement));
        assertTrue(exception.getMessage().contains(policy.getPath()));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingPolicyPathWithNoOp() throws DocumentParseException {
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, "my-encass", "ad620794-a27f-4d94-85b7-669ba838367b");

        Element noOpElement = document.createElement(NO_OP_IF_CONFIG_MISSING);
        encapsulatedAssertionElement.appendChild(noOpElement);
        noOpElement.setAttribute(BOOLEAN_VALUE, "true");

        PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals("my-encass", nameElement.getAttribute(STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals("ad620794-a27f-4d94-85b7-669ba838367b", guidElement.getAttribute(STRING_VALUE));
    }

    @Test
    void testPrepareHardcodedResponseAssertion() throws DocumentParseException {
        Element hardcodedAssertionElement = createHardcodedAssertionElement(document, "assertion body");
        PolicyEntityBuilder.prepareHardcodedResponseAssertion(document, hardcodedAssertionElement);

        Element b64BodyElement = getSingleElement(hardcodedAssertionElement, BASE_64_RESPONSE_BODY);
        String b64 = b64BodyElement.getAttribute(STRING_VALUE);
        assertEquals(Base64.getEncoder().encodeToString("assertion body".getBytes(StandardCharsets.UTF_8)), b64);

    }

    @Test
    void testPrepareIncludeAssertion() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertionElement = createIncludeAssertionElement(document, policyPath);

        PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertionElement);

        Element policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
        assertEquals(policy.getGuid(), policyGuidElement.getAttribute(STRING_VALUE));
        assertFalse(policyGuidElement.hasAttribute(POLICY_PATH));
    }

    @Test
    void testPrepareIncludeAssertionEncodedPath() throws DocumentParseException {
        String policyPath = "my/policy-_¯-¯_/_¯-¯_-path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertionElement = createIncludeAssertionElement(document, policyPath);

        PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertionElement);

        Element policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
        assertEquals(policy.getGuid(), policyGuidElement.getAttribute(STRING_VALUE));
        assertFalse(policyGuidElement.hasAttribute(POLICY_PATH));
    }

    @Test
    void testPrepareIncludeAssertionNoPolicyGuid() {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertion = document.createElement(INCLUDE);
        document.appendChild(includeAssertion);

        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertion));
    }

    @Test
    void testPrepareIncludeAssertionNoPolicyFound() {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertionElement = createIncludeAssertionElement(document, "some/other/path.xml");

        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertionElement));
    }

    @Test
    void testPrepareIncludeAssertionPolicyInDependentBundle() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        Bundle dependentBundle = new Bundle();
        dependentBundle.getPolicies().put(policyPath, policy);
        bundle.getDependencies().add(dependentBundle);
        bundle.getDependencies().add(new Bundle());

        Element includeAssertionElement = createIncludeAssertionElement(document, policyPath);

        PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertionElement);

        Element policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
        assertEquals(policy.getGuid(), policyGuidElement.getAttribute(STRING_VALUE));
        assertFalse(policyGuidElement.hasAttribute(POLICY_PATH));
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

        assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareIncludeAssertion(policy, bundle, includeAssertionElement));
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
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentFileUtils.INSTANCE, DocumentTools.INSTANCE);

        Policy policyToBuild = new Policy();
        policyToBuild.setName("path");
        policyToBuild.setId("policy-id");
        policyToBuild.setGuid("policy-guid-123");
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
    }

    @Test
    void buildPolicyEntityTestEncodedPath() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentFileUtils.INSTANCE, DocumentTools.INSTANCE);

        Policy policyToBuild = new Policy();
        policyToBuild.setName("policy-_¯-¯_");
        policyToBuild.setId("policy-id");
        policyToBuild.setGuid("policy-guid-123");
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
        assertEquals(EncodeDecodeUtils.decodePath(policyToBuild.getName()), policyEntity.getName());
    }

    @Test
    void buildPolicyEntityTestPBS() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentFileUtils.INSTANCE, DocumentTools.INSTANCE);

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
        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
    }

    @Test
    void buildPolicyEntityTestGlobal() {
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentFileUtils.INSTANCE, DocumentTools.INSTANCE);

        Policy policyToBuild = new Policy();
        policyToBuild.setPath("my/policy/global.xml");
        policyToBuild.setName("global");
        policyToBuild.setId("global-policy-id");
        policyToBuild.setGuid("global-policy-guid-123");
        policyToBuild.setTag("global-policy");
        policyToBuild.setPolicyType(PolicyType.GLOBAL);
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
        assertEquals(policyToBuild.getName(), policyEntity.getName());
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
        PolicyEntityBuilder policyEntityBuilder = new PolicyEntityBuilder(DocumentFileUtils.INSTANCE, DocumentTools.INSTANCE);

        Policy policyToBuild = new Policy();
        policyToBuild.setPath("my/policy/internal.xml");
        policyToBuild.setName("internal");
        policyToBuild.setId("internal-policy-id");
        policyToBuild.setGuid("internal-policy-guid-123");
        policyToBuild.setTag("internal-policy");
        policyToBuild.setPolicyType(PolicyType.INTERNAL);
        Folder parentFolder = new Folder();
        parentFolder.setId("folder-id");
        policyToBuild.setParentFolder(parentFolder);

        Entity policyEntity = policyEntityBuilder.buildPolicyEntity(policyToBuild, bundle, document);

        assertEquals(policyToBuild.getId(), policyEntity.getId());
        assertEquals(policyToBuild.getName(), policyEntity.getName());
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

    private Element createIncludeAssertionElement(Document document, String policyPath) {
        Element includeAssertion = document.createElement(INCLUDE);
        document.appendChild(includeAssertion);
        Element guidElement = document.createElement(POLICY_GUID);
        guidElement.setAttribute(POLICY_PATH, policyPath);
        includeAssertion.appendChild(guidElement);
        return includeAssertion;
    }

    private Element createHardcodedAssertionElement(Document document, String body) {
        Element hardcodedAssertion = document.createElement(HARDCODED_RESPONSE);
        document.appendChild(hardcodedAssertion);
        Element bodyElement = document.createElement(RESPONSE_BODY);
        bodyElement.appendChild(document.createCDATASection(body));
        hardcodedAssertion.appendChild(bodyElement);
        return hardcodedAssertion;
    }

    @NotNull
    private Element createSetVariableAssertion(Document document, String variableName, String variableValue) {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element expression = document.createElement(EXPRESSION);
        if (variableValue != null) {
            expression.appendChild(document.createCDATASection(variableValue));
        }
        setVariableAssertion.appendChild(expression);
        Element variableToSet = document.createElement(VARIABLE_TO_SET);
        variableToSet.setAttribute(STRING_VALUE, variableName);
        setVariableAssertion.appendChild(variableToSet);
        return setVariableAssertion;
    }

    private Element createEncapsulatedAssertionElement(Document document) {
        Element setEncapsulatedAssertion = document.createElement(ENCAPSULATED);
        document.appendChild(setEncapsulatedAssertion);
        setEncapsulatedAssertion.setAttribute(ENCASS_NAME, TEST_ENCASS);
        return setEncapsulatedAssertion;
    }

    @NotNull
    private Element createEncapsulatedAssertionElement(Document document, String name, String guid) {
        Element setEncapsulatedAssertion = document.createElement(ENCAPSULATED);
        document.appendChild(setEncapsulatedAssertion);


        Element nameElement = document.createElement(ENCAPSULATED_ASSERTION_CONFIG_NAME);
        setEncapsulatedAssertion.appendChild(nameElement);
        nameElement.setAttribute(STRING_VALUE, name);

        Element guidElement = document.createElement(ENCAPSULATED_ASSERTION_CONFIG_GUID);
        setEncapsulatedAssertion.appendChild(guidElement);
        guidElement.setAttribute(STRING_VALUE, guid);
        return setEncapsulatedAssertion;
    }
}