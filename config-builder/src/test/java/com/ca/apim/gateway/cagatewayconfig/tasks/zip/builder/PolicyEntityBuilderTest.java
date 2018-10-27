/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.PolicyEntityBuilder.*;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static org.junit.jupiter.api.Assertions.*;

class PolicyEntityBuilderTest {

    private Policy policy;
    private Bundle bundle;
    private Document document;

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
    void testPrepareSetVariableAssertionMissingVariableToSet() throws DocumentParseException {
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
    void testPrepareSetVariableAssertionNotENVElementNode() throws DocumentParseException {
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
        bundle.getEncasses().put(policyPath, encass);
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, policyPath);

        PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(policyPath, nameElement.getAttribute(STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionEncassInDependencyBundle() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Encass encass = new Encass();
        encass.setGuid("123");
        Bundle dependencyBundle = new Bundle();
        dependencyBundle.getEncasses().put(policyPath, encass);
        bundle.getDependencies().add(dependencyBundle);
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, policyPath);

        PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(policyPath, nameElement.getAttribute(STRING_VALUE));

        Element guidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        assertEquals(encass.getGuid(), guidElement.getAttribute(STRING_VALUE));
    }

    @Test
    void testPrepareEncapsulatedAssertionEncassInMultipleDependencyBundle() {
        String policyPath = "my/policy/path.xml";
        Encass encass = new Encass();
        encass.setGuid("123");
        Bundle dependencyBundle = new Bundle();
        dependencyBundle.getEncasses().put(policyPath, encass);
        bundle.getDependencies().add(dependencyBundle);

        Bundle dependencyBundle2 = new Bundle();
        dependencyBundle2.getEncasses().put(policyPath, encass);
        bundle.getDependencies().add(dependencyBundle2);

        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, policyPath);

        EntityBuilderException exception = assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement));
        assertTrue(exception.getMessage().contains(policyPath));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingEncass() {
        String policyPath = "my/policy/path.xml";
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, policyPath);

        EntityBuilderException exception = assertThrows(EntityBuilderException.class, () -> PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement));
        assertTrue(exception.getMessage().contains(policyPath));
    }

    @Test
    void testPrepareEncapsulatedAssertionMissingEncassWithNoOp() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Element encapsulatedAssertionElement = createEncapsulatedAssertionElement(document, policyPath);

        Element noOpElement = document.createElement(NO_OP_IF_CONFIG_MISSING);
        encapsulatedAssertionElement.appendChild(noOpElement);
        noOpElement.setAttribute(BOOLEAN_VALUE, "true");

        PolicyEntityBuilder.prepareEncapsulatedAssertion(policy, bundle, document, encapsulatedAssertionElement);

        Element nameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
        assertEquals(policyPath, nameElement.getAttribute(STRING_VALUE));

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

    @NotNull
    private Element createEncapsulatedAssertionElement(Document document, String policyPath) {
        Element setEncapsulatedAssertion = document.createElement(ENCAPSULATED);
        document.appendChild(setEncapsulatedAssertion);
        setEncapsulatedAssertion.setAttribute(POLICY_PATH, policyPath);
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