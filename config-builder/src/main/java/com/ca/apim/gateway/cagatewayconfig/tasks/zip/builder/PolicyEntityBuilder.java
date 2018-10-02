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
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.w3c.dom.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

@Singleton
public class PolicyEntityBuilder implements EntityBuilder {
    private static final Logger LOGGER = Logger.getLogger(PolicyEntityBuilder.class.getName());

    private static final String STRING_VALUE = "stringValue";
    private static final String POLICY_PATH = "policyPath";
    private static final String ENV_PARAM_NAME = "ENV_PARAM_NAME";
    private static final String TAG = "tag";
    private static final String SUBTAG = "subtag";
    private static final String TYPE = "type";
    private static final String POLICY = "policy";
    private static final String POLICY_TYPE_INCLUDE = "Include";
    private static final Integer ORDER = 200;

    private final DocumentTools documentTools;
    private final DocumentFileUtils documentFileUtils;

    @Inject
    PolicyEntityBuilder(DocumentFileUtils documentFileUtils, DocumentTools documentTools) {
        this.documentFileUtils = documentFileUtils;
        this.documentTools = documentTools;
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        bundle.getPolicies().values().forEach(policy -> preparePolicy(policy, bundle, document));

        List<Policy> orderedPolicies = new LinkedList<>();
        bundle.getPolicies().forEach((path, policy) -> maybeAddPolicy(bundle, policy, orderedPolicies, new HashSet<Policy>()));

        return orderedPolicies.stream().map(policy -> buildPolicyEntity(policy, bundle, document)).collect(Collectors.toList());
    }

    @Override
    public Integer getOrder() {
        return ORDER;
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

    private void preparePolicy(Policy policy, Bundle bundle, Document bundleDocument) {
        Document policyDocument = stringToXML(policy.getPolicyXML());
        Element policyElement = policyDocument.getDocumentElement();

        prepareAssertion(policyElement, INCLUDE, assertionElement -> prepareIncludeAssertion(policy, bundle, assertionElement));
        prepareAssertion(policyElement, ENCAPSULATED, assertionElement -> prepareEncapsulatedAssertion(bundle, policyDocument, assertionElement));
        prepareAssertion(policyElement, SET_VARIABLE, assertionElement -> prepareSetVariableAssertion(policyDocument, assertionElement, bundleDocument));
        prepareAssertion(policyElement, HARDCODED_RESPONSE, assertionElement -> prepareHardcodedResponseAssertion(policyDocument, assertionElement));

        policy.setPolicyDocument(policyElement);
    }

    private void prepareHardcodedResponseAssertion(Document policyDocument, Element assertionElement) {
        prepareBase64Element(policyDocument, assertionElement, RESPONSE_BODY, BASE_64_RESPONSE_BODY);
    }

    private void prepareSetVariableAssertion(Document policyDocument, Element assertionElement, Document bundleDocument) {
        Element nameElement;
        try {
            nameElement = getSingleElement(assertionElement, VARIABLE_TO_SET);
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Could not find VariableToSet element in a SetVariable Assertion.");
        }

        String variableName = nameElement.getAttribute(STRING_VALUE);
        if(variableName.startsWith(PREFIX_ENV)){
            assertionElement.insertBefore(
                    createElementWithAttribute(bundleDocument, BASE_64_EXPRESSION, ENV_PARAM_NAME, variableName),
                    assertionElement.getFirstChild()
            );
        } else {
            prepareBase64Element(policyDocument, assertionElement, EXPRESSION, BASE_64_EXPRESSION);
        }
    }

    private void prepareBase64Element(Document policyDocument, Element assertionElement, String elementName, String base64ElementName) {
        Element element;
        try {
            element = getSingleElement(assertionElement, elementName);
        } catch (DocumentParseException e) {
            LOGGER.log(Level.FINE, "Did not find '" + elementName + "' tag for SetVariableAssertion. Not generating Base64ed version");
            return;
        }

        String expression = getCDataOrText(element);
        String encoded = Base64.getEncoder().encodeToString(expression.getBytes());
        assertionElement.insertBefore(createElementWithAttribute(policyDocument, base64ElementName, STRING_VALUE, encoded), element);
        assertionElement.removeChild(element);
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

        Element encapsulatedAssertionConfigNameElement = createElementWithAttribute(
                policyDocument,
                ENCAPSULATED_ASSERTION_CONFIG_NAME,
                STRING_VALUE,
                policyPath
        );
        Node firstChild = encapsulatedAssertionElement.getFirstChild();
        if (firstChild != null) {
            encapsulatedAssertionElement.insertBefore(encapsulatedAssertionConfigNameElement, firstChild);
        } else {
            encapsulatedAssertionElement.appendChild(encapsulatedAssertionConfigNameElement);
        }

        Element encapsulatedAssertionConfigGuidElement = createElementWithAttribute(
                policyDocument,
                ENCAPSULATED_ASSERTION_CONFIG_GUID,
                STRING_VALUE,
                guid
        );
        encapsulatedAssertionElement.insertBefore(encapsulatedAssertionConfigGuidElement, encapsulatedAssertionElement.getFirstChild());

        ((Element) encapsulatedAssertionElement).removeAttribute(POLICY_PATH);
    }

    private void prepareIncludeAssertion(Policy policy, Bundle bundle, Element includeAssertionElement) {
        Element policyGuidElement;
        try {
            policyGuidElement = getSingleElement(includeAssertionElement, POLICY_GUID);
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

    private Entity buildPolicyEntity(Policy policy, Bundle bundle, Document document) {
        String id = policy.getId();
        PolicyTags policyTags = getPolicyTags(policy, bundle);

        Element policyDetailElement = createElementWithAttributesAndChildren(
                document,
                POLICY_DETAIL,
                ImmutableMap.of(ATTRIBUTE_ID, id, ATTRIBUTE_GUID, policy.getGuid(), ATTRIBUTE_FOLDER_ID, policy.getParentFolder().getId()),
                createElementWithTextContent(document, NAME, policy.getName()),
                createElementWithTextContent(document, POLICY_TYPE, policyTags == null ? POLICY_TYPE_INCLUDE : policyTags.type)
        );

        if (policyTags != null) {
            buildAndAppendPropertiesElement(
                    ImmutableMap.of(TAG, policyTags.tag, SUBTAG, policyTags.subtag),
                    document,
                    policyDetailElement
            );
        }

        Element policyElement = createElementWithAttributes(
                document,
                BundleElementNames.POLICY,
                ImmutableMap.of(ATTRIBUTE_ID, id, ATTRIBUTE_GUID, policy.getGuid())
        );
        policyElement.appendChild(policyDetailElement);

        Element resourcesElement = document.createElement(RESOURCES);
        Element resourceSetElement = createElementWithAttribute(document, RESOURCE_SET, TAG, POLICY);
        Element resourceElement = createElementWithAttribute(document, RESOURCE, TYPE, POLICY);
        resourceElement.setTextContent(documentFileUtils.elementToString(policy.getPolicyDocument()));

        resourceSetElement.appendChild(resourceElement);
        resourcesElement.appendChild(resourceSetElement);
        policyElement.appendChild(resourcesElement);
        return new Entity(EntityTypes.POLICY_TYPE, policy.getName(), id, policyElement);
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
