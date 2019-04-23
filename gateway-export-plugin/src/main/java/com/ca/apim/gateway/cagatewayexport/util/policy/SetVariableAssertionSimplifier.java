/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ContextVariableEnvironmentProperty;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.LinkerException;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.insertPrefixToEnvironmentVariable;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_GATEWAY;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static com.ca.apim.gateway.cagatewayexport.util.policy.PolicySimplifierUtils.base64Decode;

/**
 * Simplifier for SetVariable Assertion
 */
@Singleton
public class SetVariableAssertionSimplifier implements PolicyAssertionSimplifier {

    @Override
    public void simplifyAssertionElement(PolicySimplifierContext context) throws DocumentParseException {
        Element element = context.getAssertionElement();
        Bundle resultantBundle = context.getResultantBundle();

        Element base64ExpressionElement = getSingleElement(element, BASE_64_EXPRESSION);
        String base64Expression = base64ExpressionElement.getAttribute(STRING_VALUE);
        byte[] decodedValue = base64Decode(base64Expression);

        Element variableToSetElement = getSingleElement(element, VARIABLE_TO_SET);
        String variableName = variableToSetElement.getAttribute(STRING_VALUE);
        if (variableName.startsWith(PREFIX_ENV)) {
            if (variableName.startsWith(PREFIX_ENV + PREFIX_GATEWAY)) {
                throw new LinkerException("Cannot have local environment property start with the prefix `ENV.gateway.`. Property: " + variableName);
            }
            ContextVariableEnvironmentProperty contextVarEnvironmentProperty = new ContextVariableEnvironmentProperty(insertPrefixToEnvironmentVariable(variableName, context.getPolicyName()).substring(4), new String(decodedValue));
            ContextVariableEnvironmentProperty existingContextVarEnvironmentProperty = resultantBundle.getEntities(ContextVariableEnvironmentProperty.class).get(contextVarEnvironmentProperty.getName());
            if (existingContextVarEnvironmentProperty != null) {
                throw new LinkerException("Found duplicate environment property: `" + variableName.substring(4) + "`. Cannot have multiple environment properties with the same name.");
            }
            resultantBundle.getEntities(ContextVariableEnvironmentProperty.class).put(contextVarEnvironmentProperty.getName(), contextVarEnvironmentProperty);
        } else {
            Element expressionElement = element.getOwnerDocument().createElement(EXPRESSION);
            String value = new String(decodedValue);
            expressionElement.appendChild(element.getOwnerDocument().createCDATASection(StringEscapeUtils.escapeXml11(value)));
            element.insertBefore(expressionElement, base64ExpressionElement);
        }
        element.removeChild(base64ExpressionElement);
    }

    @Override
    public String getAssertionTagName() {
        return SET_VARIABLE;
    }
}
