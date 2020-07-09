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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuthenticationAssertionBuilderTest {

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
        Element authenticationAssertion = createAuthenticationAssertion(document, providerName);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        authenticationAssertionBuilder.buildAssertionElement(authenticationAssertion, policyBuilderContext);

        Element idProvideNameElement = getSingleChildElement(authenticationAssertion, ID_PROV_NAME, true);
        assertNotNull(idProvideNameElement);
        assertEquals("::" + projectInfo.getGroupName() + "::" + providerName + "::1.0", idProvideNameElement.getAttributes().getNamedItem(STRING_VALUE).getTextContent());
    }

    @Test
    public void testBuildAssertionElementForInternalIDProvider() {
        String providerName = INTERNAL_IDP_NAME;
        Element authenticationAssertion = createAuthenticationAssertion(document, providerName);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        authenticationAssertionBuilder.buildAssertionElement(authenticationAssertion, policyBuilderContext);

        Element idProvideNameElement = getSingleChildElement(authenticationAssertion, ID_PROV_NAME, true);
        assertNotNull(idProvideNameElement);
        assertEquals(providerName, idProvideNameElement.getAttributes().getNamedItem(STRING_VALUE).getTextContent());
    }

    @NotNull
    private Element createAuthenticationAssertion(Document document, String providerName) {
        Element authenticationElement = createElementWithChildren(
                document,
                AUTHENTICATION,
                createElementWithAttribute(document, PolicyXMLElements.ID_PROV_NAME, STRING_VALUE, providerName)
        );

        return authenticationElement;
    }

}
