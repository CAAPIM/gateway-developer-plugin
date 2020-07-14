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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HardcodedResponseAssertionBuilderTest {

    private Policy policy;
    private Bundle bundle;
    private Document document;
    private HardcodedResponseAssertionBuilder hardcodedResponseAssertionBuilder = new HardcodedResponseAssertionBuilder();
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
    void testPrepareHardcodedResponseAssertion() throws DocumentParseException {
        Element hardcodedAssertionElement = createHardcodedAssertionElement(document, "assertion body");
        document.appendChild(hardcodedAssertionElement);

        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
        policyBuilderContext.withPolicy(policy);
        hardcodedResponseAssertionBuilder.buildAssertionElement(hardcodedAssertionElement, policyBuilderContext);

        Element b64BodyElement = getSingleElement(hardcodedAssertionElement, BASE_64_RESPONSE_BODY);
        String b64 = b64BodyElement.getAttribute(PolicyEntityBuilder.STRING_VALUE);
        assertEquals(Base64.getEncoder().encodeToString("assertion body".getBytes(StandardCharsets.UTF_8)), b64);

    }

    private Element createHardcodedAssertionElement(Document document, String body) {
        Element hardcodedAssertion = document.createElement(HARDCODED_RESPONSE);

        Element bodyElement = document.createElement(RESPONSE_BODY);
        bodyElement.appendChild(document.createCDATASection(body));
        hardcodedAssertion.appendChild(bodyElement);
        return hardcodedAssertion;
    }
}
