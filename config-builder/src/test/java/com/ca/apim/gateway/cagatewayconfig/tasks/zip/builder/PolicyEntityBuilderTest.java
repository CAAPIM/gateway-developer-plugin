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

import java.util.HashSet;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.PolicyEntityBuilder.*;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
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