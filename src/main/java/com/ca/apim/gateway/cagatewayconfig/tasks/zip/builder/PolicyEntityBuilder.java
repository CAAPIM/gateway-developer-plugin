/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PolicyEntityBuilder implements EntityBuilder {
    private static final Logger LOGGER = Logger.getLogger(PolicyEntityBuilder.class.getName());
    private final Document document;
    private final IdGenerator idGenerator;
    private final DocumentTools documentTools;
    private final DocumentFileUtils documentFileUtils;

    public PolicyEntityBuilder(DocumentFileUtils documentFileUtils, DocumentTools documentTools, Document document, IdGenerator idGenerator) {
        this.documentFileUtils = documentFileUtils;
        this.documentTools = documentTools;
        this.document = document;
        this.idGenerator = idGenerator;
    }

    public List<Entity> build(Bundle bundle) {
        List<Runnable> secondStageActions = new LinkedList<>();

        bundle.getPolicies().values().forEach(policy -> preparePolicy(policy, bundle, secondStageActions));
        secondStageActions.forEach(Runnable::run);

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

    private void preparePolicy(Policy policy, Bundle bundle, List<Runnable> secondStageActions) {
        String guid = idGenerator.generateGuid();
        policy.setGuid(guid);

        Element policyElement = stringToXML(policy.getPolicyXML());

        NodeList includeReferences = policyElement.getElementsByTagName("L7p:Include");
        for (int i = 0; i < includeReferences.getLength(); i++) {
            Node includeElement = includeReferences.item(i);
            if (!(includeElement instanceof Element)) {
                throw new EntityBuilderException("Unexpected Include assertion node type: " + includeElement.getNodeType());
            }
            Element policyGuidElement;
            try {
                policyGuidElement = documentTools.getSingleElement((Element) includeElement, "L7p:PolicyGuid");
            } catch (DocumentParseException e) {
                throw new EntityBuilderException("Could not find PolicyGuid element in Include Assertion", e);
            }
            String policyPath = policyGuidElement.getAttribute("policyPath");
            Policy includedPolicy = bundle.getPolicies().get(policyPath);
            if (includedPolicy != null) {
                policy.getDependencies().add(includedPolicy);
                //need to do this in a second stage since the included policy might not have its guid set yet
                secondStageActions.add(() -> {
                    policyGuidElement.setAttribute("stringValue", includedPolicy.getGuid());
                    policyGuidElement.removeAttribute("policyPath");
                });
            } else {
                throw new EntityBuilderException("Could not find referenced policy include with path: " + policyPath);
            }
        }
        policy.setPolicyDocument(policyElement);
    }

    private Entity buildPolicyEntity(Policy policy) {
        Element policyDetailElement = document.createElement("l7:PolicyDetail");

        String id = idGenerator.generate();
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

    private Element stringToXML(String string) {
        Document documentElement;
        try {
            documentElement = documentTools.parse(string);
            documentTools.cleanup(documentElement);
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Could not load policy: " + e.getMessage(), e);
        }
        return documentElement.getDocumentElement();
    }
}
