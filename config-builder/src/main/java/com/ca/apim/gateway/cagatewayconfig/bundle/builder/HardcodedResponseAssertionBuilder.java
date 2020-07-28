package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import org.w3c.dom.Element;

import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;

public class HardcodedResponseAssertionBuilder implements PolicyAssertionBuilder {
    private static final Logger LOGGER = Logger.getLogger(HardcodedResponseAssertionBuilder.class.getName());

    @Override
    public void buildAssertionElement(Element assertionElement, PolicyBuilderContext policyBuilderContext) throws DocumentParseException {
        PolicyAssertionBuilder.prepareBase64Element(policyBuilderContext.getPolicyDocument(), assertionElement, RESPONSE_BODY, BASE_64_RESPONSE_BODY);
    }


    @Override
    public String getAssertionTagName() {
        return HARDCODED_RESPONSE;
    }
}
