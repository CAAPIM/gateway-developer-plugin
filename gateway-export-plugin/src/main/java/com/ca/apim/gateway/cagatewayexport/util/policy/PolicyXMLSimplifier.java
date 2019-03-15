/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;

/**
 * This class is responsible for invoking all policy simplifier implementations and executing them for each policy xml.
 */
@Singleton
public class PolicyXMLSimplifier {

    private Map<String, PolicyAssertionSimplifier> simplifiersByTag = new HashMap<>();

    @Inject
    public PolicyXMLSimplifier(final Set<PolicyAssertionSimplifier> simplifiers) {
        simplifiers.forEach(s -> simplifiersByTag.put(s.getAssertionTagName(), s));
        simplifiersByTag = unmodifiableMap(simplifiersByTag);
    }

    public void simplifyPolicyXML(Element policyElement, String policyName, Bundle bundle, Bundle resultantBundle) {
        PolicySimplifierContext context = new PolicySimplifierContext(policyName, bundle, resultantBundle);
        simplifiersByTag.forEach((tag, simplifier) -> findAndSimplifyAssertion(policyElement, tag, simplifier, context));
    }

    private static void findAndSimplifyAssertion(Element policyElement,
                                                 String assertionTagName,
                                                 PolicyAssertionSimplifier simplifier,
                                                 PolicySimplifierContext context) {
        NodeList includeReferences = policyElement.getElementsByTagName(assertionTagName);
        for (int i = 0; i < includeReferences.getLength(); i++) {
            Node includeElement = includeReferences.item(i);
            if (!(includeElement instanceof Element)) {
                throw new BundleLoadException("Unexpected Assertion node type: " + includeElement.getNodeType());
            }
            try {
                simplifier.simplifyAssertionElement(context.withAssertionElement((Element) includeElement));
            } catch (DocumentParseException e) {
                throw new BundleLoadException(e.getMessage(), e);
            }
        }
    }

    public Map<String, PolicyAssertionSimplifier> getSimplifiersByTag() {
        return simplifiersByTag;
    }
}
