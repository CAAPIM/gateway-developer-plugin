package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SpecificUserAssertionBuilderTest {
    private Policy policy;
    private Bundle bundle;
    private Document document;
    private AuthenticationAssertionBuilder authenticationAssertionBuilder = new AuthenticationAssertionBuilder();
    private PolicyBuilderContext policyBuilderContext;
    private ProjectInfo projectInfo = new ProjectInfo("TestProject", "TestGroup", "1.0.0");

    @BeforeEach
    void beforeEach() {
        policy = new Policy();
        policy.setPath("test/policy/path.xml");
        bundle = new Bundle(projectInfo);
        bundle.setDependencies(new HashSet<>());
        document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
    }

    @Test
    public void testBuildAssertionElement() {
        String providerName = "testLdap";
        Element authenticationAssertion = createSpecificUserAssertion(document, providerName);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        authenticationAssertionBuilder.buildAssertionElement(authenticationAssertion, policyBuilderContext);

        Element idProvideOidElement = getSingleChildElement(authenticationAssertion, ID_PROV_OID, true);
        assertNotNull(idProvideOidElement);
    }

    @Test
    public void testBuildAssertionElementForInternalIDProvider() {
        String providerName = INTERNAL_IDP_NAME;
        Element authenticationAssertion = createSpecificUserAssertion(document, providerName);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        authenticationAssertionBuilder.buildAssertionElement(authenticationAssertion, policyBuilderContext);

        Element idProvideOidElement = getSingleChildElement(authenticationAssertion, ID_PROV_OID, true);
        assertNotNull(idProvideOidElement);
    }

    @NotNull
    private Element createSpecificUserAssertion(Document document, String providerName) {
        Element authenticationElement = createElementWithChildren(
                document,
                SPECIFIC_USER,
                createElementWithAttribute(document, PolicyXMLElements.ID_PROV_NAME, STRING_VALUE, providerName)
        );

        return authenticationElement;
    }
}
