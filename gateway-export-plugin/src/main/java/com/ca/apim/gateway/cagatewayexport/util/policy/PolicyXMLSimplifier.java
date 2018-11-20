/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.LinkerException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.PolicyWriter;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_ID;
import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;

public class PolicyXMLSimplifier {
    public static final PolicyXMLSimplifier INSTANCE = new PolicyXMLSimplifier();

    private static final Logger LOGGER = Logger.getLogger(PolicyWriter.class.getName());

    public void simplifyPolicyXML(Element policyElement, Bundle bundle, Bundle resultantBundle) {
        findAndSimplifyAssertion(policyElement, INCLUDE, element -> simplifyIncludeAssertion(bundle, element));
        findAndSimplifyAssertion(policyElement, ENCAPSULATED, element -> simplifyEncapsulatedAssertion(bundle, element));
        findAndSimplifyAssertion(policyElement, SET_VARIABLE, element -> simplifySetVariable(element, resultantBundle));
        findAndSimplifyAssertion(policyElement, HARDCODED_RESPONSE, this::simplifyHardcodedResponse);
        findAndSimplifyAssertion(policyElement, AUTHENTICATION, element -> simplifyAuthenticationAssertion(bundle, element));
    }

    @VisibleForTesting
    void simplifyHardcodedResponse(Element element) {
        Element base64ResponseBodyElement;
        try {
            base64ResponseBodyElement = getSingleElement(element, BASE_64_RESPONSE_BODY);
        } catch (DocumentParseException e) {
            LOGGER.log(Level.FINE, "Base64ResponseBody missing from hardcoded assertion.");
            return;
        }
        String base64Expression = base64ResponseBodyElement.getAttribute(STRING_VALUE);
        byte[] decoded = base64Decode(base64Expression);

        Element expressionElement = element.getOwnerDocument().createElement(RESPONSE_BODY);
        expressionElement.appendChild(element.getOwnerDocument().createCDATASection(new String(decoded)));
        element.insertBefore(expressionElement, base64ResponseBodyElement);
        element.removeChild(base64ResponseBodyElement);
    }

    private static byte[] base64Decode(String base64Expression) {
        try {
            return Base64.decodeBase64(base64Expression.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to decode: " + base64Expression, e);
        }
    }

    @VisibleForTesting
    void simplifySetVariable(Element element, Bundle resultantBundle) throws DocumentParseException {
        Element base64ExpressionElement = getSingleElement(element, BASE_64_EXPRESSION);
        String base64Expression = base64ExpressionElement.getAttribute(STRING_VALUE);
        byte[] decodedValue = base64Decode(base64Expression);

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
            resultantBundle.getEntities(EnvironmentProperty.class).put(environmentProperty.getId(), environmentProperty);
        } else {
            Element expressionElement = element.getOwnerDocument().createElement(EXPRESSION);
            expressionElement.appendChild(element.getOwnerDocument().createCDATASection(new String(decodedValue)));
            element.insertBefore(expressionElement, base64ExpressionElement);
        }
        element.removeChild(base64ExpressionElement);
    }

    @VisibleForTesting
    void simplifyEncapsulatedAssertion(Bundle bundle, Element encapsulatedAssertionElement) throws DocumentParseException {
        Element encassGuidElement = getSingleElement(encapsulatedAssertionElement, ENCAPSULATED_ASSERTION_CONFIG_GUID);
        String encassGuid = encassGuidElement.getAttribute(STRING_VALUE);
        Optional<Encass> encassEntity = bundle.getEntities(Encass.class).values().stream().filter(e -> encassGuid.equals(e.getGuid())).findAny();
        if (encassEntity.isPresent()) {
            Policy policyEntity = bundle.getPolicies().values().stream().filter(p -> encassEntity.get().getPolicyId().equals(p.getId())).findFirst().orElse(null);
            if (policyEntity != null) {
                encapsulatedAssertionElement.setAttribute("encassName", encassEntity.get().getName());
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

    @VisibleForTesting
    void simplifyAuthenticationAssertion(Bundle bundle, Element authenticationAssertionElement) throws DocumentParseException {
        final Element idProviderGoidElement = getSingleElement(authenticationAssertionElement, ID_PROV_OID);
        final String idProviderGoid = idProviderGoidElement.getAttribute(GOID_VALUE);
        final Optional<IdentityProvider> idProv = bundle.getEntities(IdentityProvider.class).values().stream().filter(e -> e.getId().equals(idProviderGoid)).findAny();
        if (idProv.isPresent()) {
            updateAuthenticationAssertionElement(authenticationAssertionElement, idProviderGoidElement, idProv.get().getName());
        } else if (INTERNAL_IDP_ID.equals(idProviderGoid)) {
            updateAuthenticationAssertionElement(authenticationAssertionElement, idProviderGoidElement, INTERNAL_IDP_NAME);
        } else {
            LOGGER.log(Level.WARNING, "Could not find referenced identity provider with id: {0}", idProviderGoid);
        }
    }

    private void updateAuthenticationAssertionElement(Element authenticationAssertionElement, Element goidElementToRemove, String internalIdpName) {
        final Node firstChild = authenticationAssertionElement.getFirstChild();
        final Element idProviderNameElement = createElementWithAttribute(authenticationAssertionElement.getOwnerDocument(), ID_PROV_NAME, STRING_VALUE, internalIdpName);
        if (firstChild != null) {
            authenticationAssertionElement.insertBefore(idProviderNameElement, firstChild);
        } else {
            authenticationAssertionElement.appendChild(idProviderNameElement);
        }
        authenticationAssertionElement.removeChild(goidElementToRemove);
    }

    @VisibleForTesting
    void simplifyIncludeAssertion(Bundle bundle, Element assertionElement) throws DocumentParseException {
        Element policyGuidElement = getSingleElement(assertionElement, POLICY_GUID);
        String includedPolicyGuid = policyGuidElement.getAttribute(STRING_VALUE);
        Optional<Policy> policyEntity = bundle.getEntities(Policy.class).values().stream().filter(p -> includedPolicyGuid.equals(p.getGuid())).findAny();
        if (policyEntity.isPresent()) {
            policyGuidElement.setAttribute("policyPath", getPolicyPath(bundle, policyEntity.get()));
            policyGuidElement.removeAttribute(STRING_VALUE);
        } else {
            LOGGER.log(Level.WARNING, "Could not find referenced policy include with guid: {0}", includedPolicyGuid);
        }
    }

    private void findAndSimplifyAssertion(Element policyElement, String assertionTagName, AssertionProcessor simplifier) {
        NodeList includeReferences = policyElement.getElementsByTagName(assertionTagName);
        for (int i = 0; i < includeReferences.getLength(); i++) {
            Node includeElement = includeReferences.item(i);
            if (!(includeElement instanceof Element)) {
                throw new BundleLoadException("Unexpected Assertion node type: " + includeElement.getNodeType());
            }
            try {
                simplifier.process((Element) includeElement);
            } catch (DocumentParseException e) {
                throw new BundleLoadException(e.getMessage());
            }
        }
    }

    private String getPolicyPath(Bundle bundle, Policy policyEntity) {
        Folder folder = bundle.getFolderTree().getFolderById(policyEntity.getParentFolder().getId());
        Path folderPath = bundle.getFolderTree().getPath(folder);
        return PathUtils.unixPath(folderPath.toString(), policyEntity.getName());
    }

    interface AssertionProcessor {
        void process(Element t) throws DocumentParseException;
    }
}
