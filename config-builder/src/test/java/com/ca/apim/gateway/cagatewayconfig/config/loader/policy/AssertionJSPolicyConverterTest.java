/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader.policy;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.STRING_VALUE;
import static org.junit.jupiter.api.Assertions.*;

class AssertionJSPolicyConverterTest {

    private AssertionJSPolicyConverter assertionJSPolicyConverter;
    private Document document;

    @BeforeEach
    void beforeEach() {
        assertionJSPolicyConverter = new AssertionJSPolicyConverter();
        document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
    }

    @Test
    void canConvertTest() {
        Element policyElement = createPolicy(createJSAssertion("myPolicy", "", false, "my-javascript"));

        //success
        assertTrue(assertionJSPolicyConverter.canConvert("myPolicy", policyElement));
        // fail - different policy name
        assertFalse(assertionJSPolicyConverter.canConvert("someOtherPolicyName", policyElement));

        //fail - custom execution time
        policyElement = createPolicy(createJSAssertion("myPolicy", "123", false, "my-javascript"));
        assertFalse(assertionJSPolicyConverter.canConvert("myPolicy", policyElement));

        // fail - script is attribute
        policyElement = createPolicy(createJSAssertion("myPolicy", "", true, "my-javascript"));
        assertFalse(assertionJSPolicyConverter.canConvert("myPolicy", policyElement));

        // fail - more then just a single js assertion
        policyElement = createPolicy(createJSAssertion("myPolicy", "", false, "my-javascript"), document.createElement("L7p:SomeOtherAssertion"));
        assertFalse(assertionJSPolicyConverter.canConvert("myPolicy", policyElement));

        // fail - no assertions
        policyElement = createPolicy();
        assertFalse(assertionJSPolicyConverter.canConvert("myPolicy", policyElement));

        // fail - not js assertion
        policyElement = createPolicy(document.createElement("L7p:SomeOtherAssertion"));
        assertFalse(assertionJSPolicyConverter.canConvert("myPolicy", policyElement));
    }

    @Test
    void convertFromPolicyElement() throws IOException {
        String jsStringOriginal = "my-javascript";
        Element policyElement = createPolicy(createJSAssertion("myPolicy", "", false, jsStringOriginal));

        InputStream jsStream = assertionJSPolicyConverter.convertFromPolicyElement(policyElement);
        String jsString = IOUtils.toString(jsStream, StandardCharsets.UTF_8);
        assertEquals(jsStringOriginal, jsString);

    }

    private Element createJSAssertion(String name, String executionTime, boolean scriptAsAtr, String jsString) {
        Element jsAssertion = document.createElement("L7p:JavaScript");
        jsAssertion.appendChild(DocumentUtils.createElementWithAttribute(document, "L7p:ExecutionTimeout", STRING_VALUE, executionTime));
        jsAssertion.appendChild(DocumentUtils.createElementWithAttribute(document, "L7p:Name", STRING_VALUE, name));

        Element scriptElement = DocumentUtils.createElementWithAttribute(document, "L7p:Script", scriptAsAtr ? STRING_VALUE : "stringValueReference", scriptAsAtr ? jsString : "inline");
        if (!scriptAsAtr) {
            scriptElement.appendChild(document.createCDATASection(jsString));
        }
        jsAssertion.appendChild(scriptElement);

        return jsAssertion;
    }

    private Element createPolicy(Element... assertionElements) {
        Element policyElement = document.createElement("wsp:Policy");
        Element rootAllAssertion = document.createElement("wsp:All");
        policyElement.appendChild(rootAllAssertion);
        for (Element assertionElement : assertionElements) {
            rootAllAssertion.appendChild(assertionElement);
        }
        return policyElement;
    }

}