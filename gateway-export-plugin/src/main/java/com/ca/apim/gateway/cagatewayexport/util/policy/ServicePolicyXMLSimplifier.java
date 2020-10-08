package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;


@Singleton
public class ServicePolicyXMLSimplifier {
    private static final Logger LOGGER = Logger.getLogger(ServicePolicyXMLSimplifier.class.getName());
    /**
     *
     * @param policyElement service policy element.
     * @return true if ApiPortalIntegration assertion is present and PortalManagedApiFlag is ApiPortalManagedServiceAssertion.
     */
    public String simplifyServicePolicyXML(Element policyElement) {
        Element portalIntegrationElement = null;
        String portalManagedApiFlag = null;
        try {
            portalIntegrationElement = getSingleElement(policyElement, API_PORTAL_INTEGRATION);
        } catch (DocumentParseException e) {
            LOGGER.log(Level.INFO, "ApiPortalIntegration assertion is not found in service policy");
        }

        if (portalIntegrationElement != null) {
            Element portalManagedApiFlagElement = getSingleChildElement(portalIntegrationElement, PORTAL_MANAGED_API_FLAG, true);
            if(portalManagedApiFlagElement != null){
                portalManagedApiFlag = portalManagedApiFlagElement.getAttribute(API_PORTAL_SERVICE_ASSERTION);
                portalIntegrationElement.removeChild(portalManagedApiFlagElement);
            }
        }
        return Boolean.toString(portalManagedApiFlag != null);
    }
}