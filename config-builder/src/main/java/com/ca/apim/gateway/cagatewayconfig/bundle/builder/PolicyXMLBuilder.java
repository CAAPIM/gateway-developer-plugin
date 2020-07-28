package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Document;
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
 * This class is responsible for invoking all policy xml builder implementations and executing them for each policy xml.
 */
@Singleton
public class PolicyXMLBuilder {
    private Map<String, PolicyAssertionBuilder> buildersByTag = new HashMap<>();

    @Inject
    public PolicyXMLBuilder(final Set<PolicyAssertionBuilder> policyAssertionBuilders) {
        policyAssertionBuilders.forEach(s -> buildersByTag.put(s.getAssertionTagName(), s));
        buildersByTag = unmodifiableMap(buildersByTag);
    }

    public void buildPolicyXML(PolicyBuilderContext policyBuilderContext) {
        buildersByTag.forEach((tag, builder) -> findAndBuildAssertion(tag, builder, policyBuilderContext));
    }

    private static void findAndBuildAssertion(String assertionTag, PolicyAssertionBuilder policyAssertionBuilder, PolicyBuilderContext policyBuilderContext) {
        final Document policyDocument = policyBuilderContext.getPolicyDocument();
        Element policyElement = policyDocument.getDocumentElement();
        NodeList includeReferences = policyElement.getElementsByTagName(assertionTag);
        for (int i = 0; i < includeReferences.getLength(); i++) {
            Node includeElement = includeReferences.item(i);
            if (!(includeElement instanceof Element)) {
                throw new EntityBuilderException("Unexpected Assertion node type: " + includeElement.getNodeType());
            }
            try {
                policyAssertionBuilder.buildAssertionElement((Element) includeElement, policyBuilderContext);
            } catch (DocumentParseException e) {
                throw new EntityBuilderException(e.getMessage(), e);
            }
        }
    }

    public Map<String, PolicyAssertionBuilder> getAssertionBuildersByTag() {
        return buildersByTag;
    }
}
