package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.INCLUDE;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.POLICY_GUID;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static org.junit.jupiter.api.Assertions.*;

public class IncludeAssertionBuilderTest {
    private Policy policy;
    private Bundle bundle;
    private Document document;
    private IncludeAssertionBuilder includeAssertionBuilder = new IncludeAssertionBuilder();
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
    void testPrepareIncludeAssertion() throws DocumentParseException {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertionElement = createIncludeAssertionElement(document, policyPath);
        document.appendChild(includeAssertionElement);
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(new AnnotatedBundle(bundle, null, null));
        includeAssertionBuilder.buildAssertionElement(includeAssertionElement, policyBuilderContext);

        Element policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
        assertEquals(policy.getGuid(), policyGuidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
        assertFalse(policyGuidElement.hasAttribute(IncludeAssertionBuilder.POLICY_PATH));
    }

    @Test
    void testPrepareIncludeAssertionEncodedPath() throws DocumentParseException {
        String policyPath = "my/policy-_¯-¯_/_¯-¯_-path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertionElement = createIncludeAssertionElement(document, policyPath);
        document.appendChild(includeAssertionElement);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(new AnnotatedBundle(bundle, null, null));
        includeAssertionBuilder.buildAssertionElement(includeAssertionElement, policyBuilderContext);

        Element policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
        assertEquals(policy.getGuid(), policyGuidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
        assertFalse(policyGuidElement.hasAttribute(IncludeAssertionBuilder.POLICY_PATH));
    }

    @Test
    void testPrepareIncludeAssertionNoPolicyGuid() {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertion = document.createElement(INCLUDE);
        document.appendChild(includeAssertion);
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(new AnnotatedBundle(bundle, null, null));


        assertThrows(EntityBuilderException.class, () -> includeAssertionBuilder.buildAssertionElement(includeAssertion, policyBuilderContext));
    }

    @Test
    void testPrepareIncludeAssertionNoPolicyFound() {
        String policyPath = "my/policy/path.xml";
        Policy policy = new Policy();
        policy.setGuid("123-abc-567");
        bundle.getPolicies().put(policyPath, policy);

        Element includeAssertionElement = createIncludeAssertionElement(document, "some/other/path.xml");
        document.appendChild(includeAssertionElement);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(new AnnotatedBundle(bundle, null, null));

        assertThrows(EntityBuilderException.class, () -> includeAssertionBuilder.buildAssertionElement(includeAssertionElement, policyBuilderContext));
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

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(new AnnotatedBundle(bundle, null, null));
        includeAssertionBuilder.buildAssertionElement(includeAssertionElement, policyBuilderContext);

        Element policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
        assertEquals(policy.getGuid(), policyGuidElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));
        assertFalse(policyGuidElement.hasAttribute(IncludeAssertionBuilder.POLICY_PATH));
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

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        policyBuilderContext.withAnnotatedBundle(new AnnotatedBundle(bundle, null, null));

        assertThrows(EntityBuilderException.class, () -> includeAssertionBuilder.buildAssertionElement(includeAssertionElement, policyBuilderContext));
    }

    private Element createIncludeAssertionElement(Document document, String policyPath) {
        Element includeAssertion = document.createElement(INCLUDE);

        Element guidElement = document.createElement(POLICY_GUID);
        guidElement.setAttribute(IncludeAssertionBuilder.POLICY_PATH, policyPath);
        includeAssertion.appendChild(guidElement);
        return includeAssertion;
    }
}
