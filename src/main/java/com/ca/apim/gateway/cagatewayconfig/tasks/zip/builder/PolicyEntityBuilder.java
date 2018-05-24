/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyBackedService;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PolicyEntityBuilder implements EntityBuilder {
    private static final Logger LOGGER = Logger.getLogger(PolicyEntityBuilder.class.getName());

    private static final String STRING_VALUE = "stringValue";
    private static final String POLICY_PATH = "policyPath";

    private final Document document;
    private final DocumentTools documentTools;
    private final DocumentFileUtils documentFileUtils;

    public PolicyEntityBuilder(DocumentFileUtils documentFileUtils, DocumentTools documentTools, Document document) {
        this.documentFileUtils = documentFileUtils;
        this.documentTools = documentTools;
        this.document = document;
    }

    public List<Entity> build(Bundle bundle) {
        bundle.getPolicies().values().forEach(policy -> preparePolicy(policy, bundle));

        List<Policy> orderedPolicies = new LinkedList<>();
        bundle.getPolicies().forEach((path, policy) -> maybeAddPolicy(bundle, policy, orderedPolicies, new HashSet<Policy>()));

        return orderedPolicies.stream().map(policy -> buildPolicyEntity(policy, bundle)).collect(Collectors.toList());
    }

    private void maybeAddPolicy(Bundle bundle, Policy policy, List<Policy> orderedPolicies, Set<Policy> seenPolicies) {
        if (orderedPolicies.contains(policy) || bundle.getServices().get(policy.getPath()) != null) {
            //This is a service policy it should have already be handled by the service entity builder OR This policy has already been added to the policy list
            return;
        }
        if (seenPolicies.contains(policy)) {
            throw new EntityBuilderException("Detected Policy Include cycle containing policies: " + seenPolicies.stream().map(Policy::getPath).collect(Collectors.joining(",")));
        }
        seenPolicies.add(policy);
        policy.getDependencies().forEach(dependency -> maybeAddPolicy(bundle, dependency, orderedPolicies, seenPolicies));
        seenPolicies.remove(policy);
        orderedPolicies.add(policy);
    }

    private void preparePolicy(Policy policy, Bundle bundle) {
        Document policyDocument = stringToXML(policy.getPolicyXML());
        Element policyElement = policyDocument.getDocumentElement();

        prepareAssertion(policyElement, "L7p:Include", assertionElement -> prepareIncludeAssertion(policy, bundle, assertionElement));
        prepareAssertion(policyElement, "L7p:Encapsulated", assertionElement -> prepareEncapsulatedAssertion(bundle, policyDocument, assertionElement));
        prepareAssertion(policyElement, "L7p:SetVariable", assertionElement -> prepareSetVariableAssertion(policyDocument, assertionElement));
        prepareAssertion(policyElement, "L7p:HardcodedResponse", assertionElement -> prepareHardcodedResponseAssertion(policyDocument, assertionElement));

        policy.setPolicyDocument(policyElement);
    }

    private void prepareHardcodedResponseAssertion(Document policyDocument, Element assertionElement) {
        Element responseBodyElement;
        try {
            responseBodyElement = documentTools.getSingleElement(assertionElement, "L7p:ResponseBody");
        } catch (DocumentParseException e) {
            LOGGER.log(Level.FINE, "Did not find 'Expression' tag for SetVariableAssertion. Not generating Base64ed version");
            return;
        }

        String expression = getCDataOrText(responseBodyElement);
        String encoded = Base64.getEncoder().encodeToString(expression.getBytes());
        Element base64ResponseElement = policyDocument.createElement("L7p:Base64ResponseBody");
        base64ResponseElement.setAttribute(STRING_VALUE, encoded);
        assertionElement.insertBefore(base64ResponseElement, responseBodyElement);
        assertionElement.removeChild(responseBodyElement);
    }

    private void prepareSetVariableAssertion(Document policyDocument, Element assertionElement) {
        Element expressionElement;
        try {
            expressionElement = documentTools.getSingleElement(assertionElement, "L7p:Expression");
        } catch (DocumentParseException e) {
            LOGGER.log(Level.FINE, "Did not find 'Expression' tag for SetVariableAssertion. Not generating Base64ed version");
            return;
        }

        String expression = getCDataOrText(expressionElement);
        String encoded = Base64.getEncoder().encodeToString(expression.getBytes());
        Element base64ExpressionElement = policyDocument.createElement("L7p:Base64Expression");
        base64ExpressionElement.setAttribute(STRING_VALUE, encoded);
        assertionElement.insertBefore(base64ExpressionElement, expressionElement);
        assertionElement.removeChild(expressionElement);
    }

    private String getCDataOrText(Element element) {
        StringBuilder content = new StringBuilder();
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            short nodeType = child.getNodeType();
            if (nodeType == Node.TEXT_NODE) {
                content.append(child.getTextContent());
            } else if (nodeType == Node.CDATA_SECTION_NODE) {
                return ((CDATASection) child).getData();
            } else {
                throw new EntityBuilderException("Unexpected set variable assertion expression node type: " + child.getNodeName());
            }
        }
        return content.toString();
    }

    private void prepareEncapsulatedAssertion(Bundle bundle, Document policyDocument, Node encapsulatedAssertionElement) {
        final String policyPath = ((Element) encapsulatedAssertionElement).getAttribute(POLICY_PATH);
        LOGGER.log(Level.FINE, "Looking for referenced encass: {0}", policyPath);
        final AtomicReference<Encass> referenceEncass = new AtomicReference<>(bundle.getEncasses().get(policyPath));
        if (referenceEncass.get() == null) {
            bundle.getDependencies().forEach(b -> {
                if (!referenceEncass.compareAndSet(null, b.getEncasses().get(policyPath))) {
                    throw new EntityBuilderException("Found multiple encasses in dependency bundles with policy path: " + policyPath);
                }
            });
        }
        if (referenceEncass.get() == null) {
            throw new EntityBuilderException("Could not find referenced encass with path: " + policyPath);
        }
        final String guid = referenceEncass.get().getGuid();

        Element encapsulatedAssertionConfigNameElement = policyDocument.createElement("L7p:EncapsulatedAssertionConfigName");
        encapsulatedAssertionConfigNameElement.setAttribute(STRING_VALUE, policyPath);
        Node firstChild = encapsulatedAssertionElement.getFirstChild();
        if (firstChild != null) {
            encapsulatedAssertionElement.insertBefore(encapsulatedAssertionConfigNameElement, firstChild);
        } else {
            encapsulatedAssertionElement.appendChild(encapsulatedAssertionConfigNameElement);
        }

        Element encapsulatedAssertionConfigGuidElement = policyDocument.createElement("L7p:EncapsulatedAssertionConfigGuid");
        encapsulatedAssertionConfigGuidElement.setAttribute(STRING_VALUE, guid);
        encapsulatedAssertionElement.insertBefore(encapsulatedAssertionConfigGuidElement, encapsulatedAssertionElement.getFirstChild());

        ((Element) encapsulatedAssertionElement).removeAttribute(POLICY_PATH);
    }

    private void prepareIncludeAssertion(Policy policy, Bundle bundle, Element includeAssertionElement) {
        Element policyGuidElement;
        try {
            policyGuidElement = documentTools.getSingleElement(includeAssertionElement, "L7p:PolicyGuid");
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Could not find PolicyGuid element in Include Assertion", e);
        }
        final String policyPath = policyGuidElement.getAttribute(POLICY_PATH);
        LOGGER.log(Level.FINE, "Looking for referenced policy include: {0}", policyPath);

        final AtomicReference<Policy> includedPolicy = new AtomicReference<>(bundle.getPolicies().get(policyPath));
        if (includedPolicy.get() != null) {
            policy.getDependencies().add(includedPolicy.get());
        } else {
            bundle.getDependencies().forEach(b -> {
                if (!includedPolicy.compareAndSet(null, b.getPolicies().get(policyPath))) {
                    throw new EntityBuilderException("Found multiple policies in dependency bundles with policy path: " + policyPath);
                }
            });
        }
        if (includedPolicy.get() == null) {
            throw new EntityBuilderException("Could not find referenced policy include with path: " + policyPath);
        }
        policyGuidElement.setAttribute(STRING_VALUE, includedPolicy.get().getGuid());
        policyGuidElement.removeAttribute(POLICY_PATH);
    }

    private void prepareAssertion(Element policyElement, String assertionTag, Consumer<Element> prepareAssertionMethod) {
        NodeList assertionReferences = policyElement.getElementsByTagName(assertionTag);
        for (int i = 0; i < assertionReferences.getLength(); i++) {
            Node assertionElement = assertionReferences.item(i);
            if (!(assertionElement instanceof Element)) {
                throw new EntityBuilderException("Unexpected assertion node type: " + assertionElement.getNodeName());
            }
            prepareAssertionMethod.accept((Element) assertionElement);
        }
    }

    private Entity buildPolicyEntity(Policy policy, Bundle bundle) {
        Element policyDetailElement = document.createElement("l7:PolicyDetail");

        String id = policy.getId();
        policyDetailElement.setAttribute("id", id);
        policyDetailElement.setAttribute("guid", policy.getGuid());
        policyDetailElement.setAttribute("folderId", policy.getParentFolder().getId());
        Element nameElement = document.createElement("l7:Name");
        nameElement.setTextContent(policy.getName());
        policyDetailElement.appendChild(nameElement);

        PolicyTags policyTags = getPolicyTags(policy, bundle);

        Element policyTypeElement = document.createElement("l7:PolicyType");
        policyTypeElement.setTextContent(policyTags == null ? "Include" : policyTags.type);
        policyDetailElement.appendChild(policyTypeElement);

        if (policyTags != null) {
            Element propertiesElement = document.createElement("l7:Properties");
            policyDetailElement.appendChild(propertiesElement);
            Element propertyTagElement = document.createElement("l7:Property");
            propertiesElement.appendChild(propertyTagElement);
            propertyTagElement.setAttribute("key", "tag");
            Element tagStringValueElement = document.createElement("l7:StringValue");
            propertyTagElement.appendChild(tagStringValueElement);
            tagStringValueElement.setTextContent(policyTags.tag);

            Element propertySubTagElement = document.createElement("l7:Property");
            propertiesElement.appendChild(propertySubTagElement);
            propertySubTagElement.setAttribute("key", "subtag");
            Element subTagStringValueElement = document.createElement("l7:StringValue");
            propertySubTagElement.appendChild(subTagStringValueElement);
            subTagStringValueElement.setTextContent(policyTags.subtag);
        }

        Element policyElement = document.createElement("l7:Policy");
        policyElement.setAttribute("id", id);
        policyElement.setAttribute("guid", policy.getGuid());
        policyElement.appendChild(policyDetailElement);

        Element resourcesElement = document.createElement("l7:Resources");

        Element resourceSetElement = document.createElement("l7:ResourceSet");
        resourceSetElement.setAttribute("tag", "policy");

        Element resourceElement = document.createElement("l7:Resource");
        resourceElement.setAttribute("type", "policy");

        resourceElement.setTextContent(documentFileUtils.elementToString(policy.getPolicyDocument()));

        resourceSetElement.appendChild(resourceElement);
        resourcesElement.appendChild(resourceSetElement);
        policyElement.appendChild(resourcesElement);
        return new Entity("POLICY", policy.getName(), id, policyElement);
    }

    private PolicyTags getPolicyTags(Policy policy, Bundle bundle) {
        final AtomicReference<PolicyTags> policyTags = new AtomicReference<>();
        for (PolicyBackedService pbs : bundle.getPolicyBackedServices().values()) {
            pbs.getOperations().stream().filter(o -> o.getPolicy().equals(policy.getPath())).forEach(o -> {
                if (!policyTags.compareAndSet(null, new PolicyTags("Service Operation", pbs.getInterfaceName(), o.getOperationName()))) {
                    throw new EntityBuilderException("Found multiple policy backed service operations for policy: " + policy.getPath());
                }
            });
        }
        return policyTags.get();
    }

    private Document stringToXML(String string) {
        Document documentElement;
        try {
            documentElement = documentTools.parse(string);
            documentTools.cleanup(documentElement);
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Could not load policy: " + e.getMessage(), e);
        }
        return documentElement;
    }

    private class PolicyTags {
        private final String type;
        private final String tag;
        private final String subtag;

        private PolicyTags(String type, String tag, String subtag) {
            this.type = type;
            this.tag = tag;
            this.subtag = subtag;
        }
    }
}
