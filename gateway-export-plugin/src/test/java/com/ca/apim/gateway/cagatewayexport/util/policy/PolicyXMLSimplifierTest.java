package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PolicyXMLSimplifierTest {

    @Test
    void simplifySetVariable() throws DocumentParseException {
        Element setVariableAssertion = createSetVariableAssertionElement("my-var", "dGVzdGluZyBzaW1wbGlmeSBzZXQgdmFyaWFibGUgYXNzZXJ0aW9u");

        Bundle resultantBundle = new Bundle();
        PolicyXMLSimplifier.simplifySetVariable(setVariableAssertion, resultantBundle);

        Element expressionElement = getSingleElement(setVariableAssertion, EXPRESSION);
        assertEquals("testing simplify set variable assertion", expressionElement.getFirstChild().getTextContent());

        NodeList base64ElementNodes = setVariableAssertion.getElementsByTagName(BASE_64_EXPRESSION);
        assertEquals(0, base64ElementNodes.getLength());
    }

    @Test
    void simplifySetVariableWithBadEndingUnit() throws DocumentParseException {
        //The default Base64 decoder in java cannot decode this, but the commons-codec decoder can.
        Element setVariableAssertion = createSetVariableAssertionElement("my-var2", "ew0KICAgICAgICAgICAgICAgICJlcnJvciI6ImludmFsaWRfbWV0aG9kIiwNCiAgICAgICAgICAgICAgICAiZXJyb3JfZGVzY3JpcHRpb24iOiIke3JlcXVlc3QuaHR0cC5tZXRob2R9IG5vdCBwZXJtaXR0ZWQiDQogICAgICAgICAgICAgICAgfQ=");

        Bundle resultantBundle = new Bundle();
        PolicyXMLSimplifier.simplifySetVariable(setVariableAssertion, resultantBundle);

        Element expressionElement = getSingleElement(setVariableAssertion, EXPRESSION);
        assertEquals("{\r\n" +
                "                \"error\":\"invalid_method\",\r\n" +
                "                \"error_description\":\"${request.http.method} not permitted\"\r\n" +
                "                }", expressionElement.getFirstChild().getTextContent());

        NodeList base64ElementNodes = setVariableAssertion.getElementsByTagName(BASE_64_EXPRESSION);
        assertEquals(0, base64ElementNodes.getLength());
    }

    @NotNull
    private Element createSetVariableAssertionElement(String variableName, String base64Data) {
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();

        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);

        Element base64Element = document.createElement(BASE_64_EXPRESSION);
        setVariableAssertion.appendChild(base64Element);

        base64Element.setAttribute(STRING_VALUE, base64Data);

        Element variableToSetElement = document.createElement(VARIABLE_TO_SET);
        setVariableAssertion.appendChild(variableToSetElement);
        variableToSetElement.setAttribute(STRING_VALUE, variableName);
        return setVariableAssertion;
    }
}