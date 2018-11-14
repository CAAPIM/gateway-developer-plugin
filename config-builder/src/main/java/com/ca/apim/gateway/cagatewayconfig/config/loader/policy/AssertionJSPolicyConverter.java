/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.STRING_VALUE;

@Singleton
public class AssertionJSPolicyConverter implements PolicyConverter {
    @Override
    public String getPolicyTypeExtension() {
        return ".assertion.js";
    }

    @Override
    public String getPolicyXML(Policy policy, String policyString) {
        return "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\">\n" +
                "        <L7p:JavaScript>\n" +
                "            <L7p:ExecutionTimeout stringValue=\"\"/>\n" +
                "            <L7p:Name stringValue=\"" + policy.getName() + "\"/>\n" +
                "            <L7p:Script stringValueReference=\"inline\"><![CDATA[" + policyString + "]]></L7p:Script>\n" +
                "        </L7p:JavaScript>\n" +
                "    </wsp:All>\n" +
                "</wsp:Policy>";
    }

    @Override
    public boolean canConvert(String policyName, Element policy) {
        return getScriptString(policyName, policy).isPresent();
    }

    @Override
    public InputStream convertFromPolicyElement(Element policy) {
        Optional<String> jsPolicy = getScriptString(null, policy);
        return IOUtils.toInputStream(jsPolicy.orElseThrow(() -> new PolicyConverterException("Cannot Convert JS Policy")), StandardCharsets.UTF_8);
    }

    /**
     * Returns javascript from a policy that has a single javascript assertion.
     *
     * @param policyName The policy name, if not null it must match the name in the javascript assertion.
     * @param policy     The policy element
     * @return The Script as a String in an optional. The optional is empty if the policy does not contain a single javascript assertion.
     */
    private Optional<String> getScriptString(@Nullable String policyName, Element policy) {
        Element rootAllAssertion = DocumentUtils.getSingleChildElement(policy, "wsp:All");
        if (rootAllAssertion.getChildNodes().getLength() == 1) {
            Element jsAssertion = DocumentUtils.getSingleChildElement(rootAllAssertion, "L7p:JavaScript", true);
            if (jsAssertion != null) {
                String executionTimeout = DocumentUtils.getSingleChildElementAttribute(jsAssertion, "L7p:ExecutionTimeout", STRING_VALUE);
                String name = DocumentUtils.getSingleChildElementAttribute(jsAssertion, "L7p:Name", STRING_VALUE);
                Element scriptElement = DocumentUtils.getSingleChildElement(jsAssertion, "L7p:Script");
                if ((policyName == null || policyName.equals(name)) && (executionTimeout == null || executionTimeout.isEmpty()) && !scriptElement.getAttribute("stringValueReference").isEmpty()) {
                    return Optional.of(scriptElement.getTextContent());
                }
            }
        }
        return Optional.empty();
    }
}
