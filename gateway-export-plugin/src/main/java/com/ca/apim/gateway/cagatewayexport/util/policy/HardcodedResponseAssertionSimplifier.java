/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static com.ca.apim.gateway.cagatewayexport.util.policy.PolicySimplifierUtils.base64Decode;

/**
 * Simplifies the Hardcoded Assertion XML tags, removing the base64 encoded and setting up as plain text.
 */
@Singleton
public class HardcodedResponseAssertionSimplifier implements PolicyAssertionSimplifier {

    private static final Logger LOGGER = Logger.getLogger(HardcodedResponseAssertionSimplifier.class.getName());

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) {
        Element element = context.getAssertionElement();
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

    @Override
    public String getAssertionTagName() {
        return HARDCODED_RESPONSE;
    }
}
