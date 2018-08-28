/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleBuilderException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EnvironmentProperty;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.LinkerException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.PolicyWriter;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayexport.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getSingleElement;

public class PolicyXMLSimplifier {
    public static final PolicyXMLSimplifier INSTANCE = new PolicyXMLSimplifier();

    private static final Logger LOGGER = Logger.getLogger(PolicyWriter.class.getName());

    public void simplifyPolicyXML(Element policyElement, Bundle bundle, Bundle resultantBundle) {
        findAndSimplifyAssertion(policyElement, INCLUDE, element -> simplifyIncludeAssertion(bundle, element));
        findAndSimplifyAssertion(policyElement, ENCAPSULATED, element -> simplifyEncapsulatedAssertion(bundle, element));
        findAndSimplifyAssertion(policyElement, SET_VARIABLE, element -> simplifySetVariable(element, resultantBundle));
        findAndSimplifyAssertion(policyElement, HARDCODED_RESPONSE, this::simplifyHardcodedResponse);
    }

    private void simplifyHardcodedResponse(Element element) {
        Element base64ResponseBodyElement;
        try {
            base64ResponseBodyElement = getSingleElement(element, BASE_64_RESPONSE_BODY);
        } catch (BundleBuilderException e) {
            LOGGER.log(Level.FINE, "Base64ResponseBody missing from hardcoded assertion.");
            return;
        }
        String base64Expression = base64ResponseBodyElement.getAttribute(STRING_VALUE);
        byte[] decoded = Base64.getDecoder().decode(base64Expression);

        Element expressionElement = element.getOwnerDocument().createElement(RESPONSE_BODY);
        expressionElement.appendChild(element.getOwnerDocument().createCDATASection(new String(decoded)));
        element.insertBefore(expressionElement, base64ResponseBodyElement);
        element.removeChild(base64ResponseBodyElement);
    }

    private void simplifySetVariable(Element element, Bundle resultantBundle) {
        Element base64ExpressionElement = getSingleElement(element, BASE_64_EXPRESSION);
        String base64Expression = base64ExpressionElement.getAttribute(STRING_VALUE);
        byte[] decodedValue = Base64.getDecoder().decode(base64Expression);

        Element variableToSetElement = getSingleElement(element, VARIABLE_TO_SET);
        String variableName = variableToSetElement.getAttribute(STRING_VALUE);
        if (variableName.startsWith("ENV.")) {
            if (variableName.startsWith("ENV.gateway.")) {
                throw new LinkerException("Cannot have local environment property start with the prefix `ENV.gateway.`. Property: " + variableName);
            }
            EnvironmentProperty environmentProperty = new EnvironmentProperty(variableName.substring(4), new String(decodedValue), EnvironmentProperty.Type.LOCAL);
            EnvironmentProperty existingEnvironmentProperty = resultantBundle.getEntities(EnvironmentProperty.class).get(environmentProperty.getId());
            if (existingEnvironmentProperty != null) {
                throw new LinkerException("Found duplicate environment property: `" + variableName.substring(4) + "`. Cannot have multiple environment properties with the same name.");
            }
            resultantBundle.addEntity(environmentProperty);
        } else {
            Element expressionElement = element.getOwnerDocument().createElement(EXPRESSION);
            expressionElement.appendChild(element.getOwnerDocument().createCDATASection(new String(decodedValue)));
            element.insertBefore(expressionElement, base64ExpressionElement);
        }
        element.removeChild(base64ExpressionElement);
    }

    private void simplifyEncapsulatedAssertion(Bundle bundle, Element encapsulatedAssertionElement) {
        Element encassGuidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        String encassGuid = encassGuidElement.getAttribute(STRING_VALUE);
        Optional<EncassEntity> encassEntity = bundle.getEntities(EncassEntity.class).values().stream().filter(e -> encassGuid.equals(e.getGuid())).findAny();
        if (encassEntity.isPresent()) {
            PolicyEntity policyEntity = bundle.getEntities(PolicyEntity.class).get(encassEntity.get().getPolicyId());
            if (policyEntity != null) {
                encapsulatedAssertionElement.setAttribute("policyPath", getPolicyPath(bundle, policyEntity));
                Element encapsulatedAssertionConfigNameElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_NAME);
                encapsulatedAssertionElement.removeChild(encapsulatedAssertionConfigNameElement);
                encapsulatedAssertionElement.removeChild(encassGuidElement);
            } else {
                LOGGER.log(Level.WARNING, "Could not find referenced encass policy with id: {0}", encassEntity.get().getPolicyId());
            }
        } else {
            LOGGER.log(Level.WARNING, "Could not find referenced encass with guid: {0}", encassGuid);
        }
    }

    private void simplifyIncludeAssertion(Bundle bundle, Element assertionElement) {
        Element policyGuidElement = getSingleElement(assertionElement, POLICY_GUID);
        String includedPolicyGuid = policyGuidElement.getAttribute(STRING_VALUE);
        Optional<PolicyEntity> policyEntity = bundle.getEntities(PolicyEntity.class).values().stream().filter(p -> includedPolicyGuid.equals(p.getGuid())).findAny();
        if (policyEntity.isPresent()) {
            policyGuidElement.setAttribute("policyPath", getPolicyPath(bundle, policyEntity.get()));
            policyGuidElement.removeAttribute(STRING_VALUE);
        } else {
            LOGGER.log(Level.WARNING, "Could not find referenced policy include with guid: {0}", includedPolicyGuid);
        }
    }

    private void findAndSimplifyAssertion(Element policyElement, String assertionTagName, Consumer<Element> simplifier) {
        NodeList includeReferences = policyElement.getElementsByTagName(assertionTagName);
        for (int i = 0; i < includeReferences.getLength(); i++) {
            Node includeElement = includeReferences.item(i);
            if (!(includeElement instanceof Element)) {
                throw new WriteException("Unexpected Assertion node type: " + includeElement.getNodeType());
            }
            simplifier.accept((Element) includeElement);
        }
    }

    private String getPolicyPath(Bundle bundle, PolicyEntity policyEntity) {
        Folder folder = bundle.getFolderTree().getFolderById(policyEntity.getFolderId());
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return Paths.get(folderPath.toString(), policyEntity.getName() + ".xml").toString();
    }
}
