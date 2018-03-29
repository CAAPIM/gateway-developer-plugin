/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PolicyEntityBuilder implements EntityBuilder {
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

        return orderedPolicies.stream().map(this::buildPolicyEntity).collect(Collectors.toList());
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

        policy.setPolicyDocument(policyElement);
    }

    private void prepareAssertion(Element policyElement, String assertionTag, Consumer<Element> prepareAssertionMethod) {
        NodeList assertionReferences = policyElement.getElementsByTagName(assertionTag);
        for (int i = 0; i < assertionReferences.getLength(); i++) {
            Node assertionElement = assertionReferences.item(i);
            if (!(assertionElement instanceof Element)) {
                throw new EntityBuilderException("Unexpected assertion node type: " + assertionElement.getNodeType());
            }
            prepareAssertionMethod.accept((Element) assertionElement);
        }
    }

    private void prepareEncapsulatedAssertion(Bundle bundle, Document policyDocument, Node encapsulatedAssertionElement) {
        String policyPath = ((Element) encapsulatedAssertionElement).getAttribute("policyPath");
        Encass referenceEncass = bundle.getEncasses().get(policyPath);
        if (referenceEncass != null) {
            Element encapsulatedAssertionConfigNameElement = policyDocument.createElement("L7p:EncapsulatedAssertionConfigName");
            encapsulatedAssertionConfigNameElement.setAttribute("stringValue", policyPath);
            Node firstChild = encapsulatedAssertionElement.getFirstChild();
            if (firstChild != null) {
                encapsulatedAssertionElement.insertBefore(encapsulatedAssertionConfigNameElement, firstChild);
            } else {
                encapsulatedAssertionElement.appendChild(encapsulatedAssertionConfigNameElement);
            }

            Element encapsulatedAssertionConfigGuidElement = policyDocument.createElement("L7p:EncapsulatedAssertionConfigGuid");
            encapsulatedAssertionConfigGuidElement.setAttribute("stringValue", referenceEncass.getGuid());
            encapsulatedAssertionElement.insertBefore(encapsulatedAssertionConfigGuidElement, encapsulatedAssertionElement.getFirstChild());

            ((Element) encapsulatedAssertionElement).removeAttribute("policyPath");
        } else {
            throw new EntityBuilderException("Could not find referenced encass with path: " + policyPath);
        }
    }

    private void prepareIncludeAssertion(Policy policy, Bundle bundle, Element includeAssertionElement) {
        Element policyGuidElement;
        try {
            policyGuidElement = documentTools.getSingleElement(includeAssertionElement, "L7p:PolicyGuid");
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Could not find PolicyGuid element in Include Assertion", e);
        }
        String policyPath = policyGuidElement.getAttribute("policyPath");
        Policy includedPolicy = bundle.getPolicies().get(policyPath);
        if (includedPolicy != null) {
            policy.getDependencies().add(includedPolicy);
            //need to do this in a second stage since the included policy might not have its guid set yet
            policyGuidElement.setAttribute("stringValue", includedPolicy.getGuid());
            policyGuidElement.removeAttribute("policyPath");
        } else {
            throw new EntityBuilderException("Could not find referenced policy include with path: " + policyPath);
        }
    }

    private Entity buildPolicyEntity(Policy policy) {
        Element policyDetailElement = document.createElement("l7:PolicyDetail");

        String id = policy.getId();
        policyDetailElement.setAttribute("id", id);
        policyDetailElement.setAttribute("guid", policy.getGuid());
        policyDetailElement.setAttribute("folderId", policy.getParentFolder().getId());
        Element nameElement = document.createElement("l7:Name");
        nameElement.setTextContent(policy.getName());
        policyDetailElement.appendChild(nameElement);

        Element policyTypeElement = document.createElement("l7:PolicyType");
        policyTypeElement.setTextContent("Include");
        policyDetailElement.appendChild(policyTypeElement);


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
}
