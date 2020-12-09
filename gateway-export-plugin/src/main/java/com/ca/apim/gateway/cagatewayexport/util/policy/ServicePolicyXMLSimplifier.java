package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.ServiceAndPolicyLoaderUtil;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Base64;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.L7_TEMPLATE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;


@Singleton
public class ServicePolicyXMLSimplifier {
    private static final Logger LOGGER = Logger.getLogger(ServicePolicyXMLSimplifier.class.getName());
    /**
     *
     * @param policyElement service policy element.
     * @param service Service
     */
    public void simplifyServicePolicyXML(Element policyElement, Service service) {
        service.getProperties().putIfAbsent(L7_TEMPLATE, "false");
        // [Set as Portal Managed Service] assertion
        simplifyPortalManagedAssertion(policyElement, service);
    }

    private void simplifyPortalManagedAssertion(Element policyElement, Service service) {
        try {
            Element portalManagedElement = getSingleElement(policyElement, API_PORTAL_INTEGRATION);
            Element flagElement = getSingleChildElement(portalManagedElement, API_PORTAL_INTEGRATION_FLAG, true);

            if (flagElement != null && API_PORTAL_INTEGRATION_FLAG_SERVICE.equals(flagElement.getAttribute(STRING_VALUE))) {
                Element enabledElement = getSingleChildElement(portalManagedElement, ENABLED, true);
                Element variablePrefixElement = getSingleChildElement(portalManagedElement, API_PORTAL_INTEGRATION_VARIABLE_PREFIX, true);
                Element apiIdElement = getSingleChildElement(portalManagedElement, API_PORTAL_INTEGRATION_API_ID, true);
                Element apiGroupElement = getSingleChildElement(portalManagedElement, API_PORTAL_INTEGRATION_API_GROUP, true);
                Document document = policyElement.getOwnerDocument();
                Element parentElement = (Element) portalManagedElement.getParentNode();
                boolean isEnabled = enabledElement == null || Boolean.parseBoolean(enabledElement.getAttribute(BOOLEAN_VALUE));
                String variablePrefix = variablePrefixElement != null ? variablePrefixElement.getAttribute(STRING_VALUE) : "portal.managed.service";

                service.getProperties().put(L7_TEMPLATE, Boolean.toString(isEnabled));
                if (isEnabled && ServiceAndPolicyLoaderUtil.migratePortalIntegrationsAssertions()) {
                    LOGGER.info("Migrating [Set as Portal Managed Service] assertion for " + service.getPath());
                    parentElement.insertBefore(
                            DocumentUtils.createElementWithChildren(document, COMMENT_ASSERTION,
                                    DocumentUtils.createElementWithAttribute(document, COMMENT, STRING_VALUE, "Migrated: Set as Portal Managed Service")),
                            portalManagedElement);
                    if (apiIdElement != null) {
                        parentElement.insertBefore(
                                createSetAssertion(policyElement, variablePrefix + ".apiId", apiIdElement.getAttribute(STRING_VALUE)),
                                portalManagedElement);
                    }

                    if (apiGroupElement != null) {
                        parentElement.insertBefore(
                                createSetAssertion(policyElement, variablePrefix + ".apiGroup", apiGroupElement.getAttribute(STRING_VALUE)),
                                portalManagedElement);
                    }
                    parentElement.removeChild(portalManagedElement);
                }
            }
        } catch (DocumentParseException e) {
            // ignoring the exception
        }
    }

    private Element createSetAssertion(Element policyElement, String name, String value) {
        Document document = policyElement.getOwnerDocument();
        Element expressionElement = document.createElement(EXPRESSION);

        expressionElement.appendChild(document.createCDATASection(StringEscapeUtils.escapeXml11(value)));
        return DocumentUtils.createElementWithChildren(document, SET_VARIABLE,
                DocumentUtils.createElementWithAttribute(document, VARIABLE_TO_SET, STRING_VALUE, name),
                expressionElement);
    }
}