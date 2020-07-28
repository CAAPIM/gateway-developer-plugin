package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.*;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.insertPrefixToEnvironmentVariable;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;

public class SetVariableAssertionBuilder implements PolicyAssertionBuilder {
    private static final Logger LOGGER = Logger.getLogger(SetVariableAssertionBuilder.class.getName());
    static final String ENV_PARAM_NAME = "ENV_PARAM_NAME";

    @Override
    public void buildAssertionElement(Element assertionElement, PolicyBuilderContext policyBuilderContext) throws DocumentParseException {
        final Document policyDocument = policyBuilderContext.getPolicyDocument();
        Element nameElement;
        try {
            nameElement = getSingleElement(assertionElement, VARIABLE_TO_SET);
        } catch (DocumentParseException e) {
            throw new EntityBuilderException("Could not find VariableToSet element in a SetVariable Assertion.");
        }

        String variableName = nameElement.getAttribute(STRING_VALUE);
        if (variableName.startsWith(PREFIX_ENV)) {
            assertionElement.insertBefore(
                    createElementWithAttribute(policyDocument, BASE_64_EXPRESSION, ENV_PARAM_NAME, insertPrefixToEnvironmentVariable(variableName, policyBuilderContext.getPolicyName())),
                    assertionElement.getFirstChild()
            );
        } else {
            try{
                PolicyAssertionBuilder.prepareBase64Element(policyDocument, assertionElement, EXPRESSION, BASE_64_EXPRESSION);
            } catch (EntityBuilderException e){
                LOGGER.log(Level.WARNING, "Exception " + e.getMessage());
            }
        }
    }


    @Override
    public String getAssertionTagName() {
        return SET_VARIABLE;
    }
}
