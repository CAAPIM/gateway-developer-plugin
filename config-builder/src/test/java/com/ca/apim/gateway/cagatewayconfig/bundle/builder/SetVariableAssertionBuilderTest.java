package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PREFIX_ENV;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleElement;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.junit.jupiter.api.Assertions.*;

public class SetVariableAssertionBuilderTest {

    private Policy policy;
    private Bundle bundle;
    private Document document;
    private SetVariableAssertionBuilder setVariableAssertionBuilder = new SetVariableAssertionBuilder();
    private PolicyBuilderContext policyBuilderContext;

    @BeforeEach
    void beforeEach() {
        policy = new Policy();
        policy.setPath("test/policy/path.xml");
        bundle = new Bundle();
        bundle.setDependencies(new HashSet<>());
        document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        policyBuilderContext = new PolicyBuilderContext("path.xml", document, bundle, new IdGenerator());
    }

    @Test
    void testPrepareSetVariableAssertionNoENV() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "my-var", "base64Text");
        document.appendChild(setVariableAssertionElement);
        String prefix = "prefix";

        setVariableAssertionBuilder.buildAssertionElement(setVariableAssertionElement, policyBuilderContext);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("my-var", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        String b64 = expressionElement.getAttribute(PolicyEntityBuilder.STRING_VALUE);
        assertEquals(Base64.getEncoder().encodeToString("base64Text".getBytes(StandardCharsets.UTF_8)), b64);
    }

    @NotNull
    private Element createSetVariableAssertion(Document document, String variableName, String variableValue) {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);

        Element expression = document.createElement(EXPRESSION);
        if (variableValue != null) {
            expression.appendChild(document.createCDATASection(variableValue));
        }
        setVariableAssertion.appendChild(expression);
        Element variableToSet = document.createElement(VARIABLE_TO_SET);
        variableToSet.setAttribute(PolicyEntityBuilder.STRING_VALUE, variableName);
        setVariableAssertion.appendChild(variableToSet);
        return setVariableAssertion;
    }

    @Test
    void testPrepareSetVariableAssertionUnescaping() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "userSession", "&lt;usersession&gt;\n"
                + " &lt;user&gt;&lt;![CDATA[${current.username}]]&gt;&lt;/user&gt;\n"
                + " &lt;role&gt;&lt;![CDATA[${current.user.role}]]&gt;&lt;/role&gt;\n"
                + " &lt;lookupUser&gt;&lt;![CDATA[${lookupUser}]]&gt;&lt;/lookupUser&gt;\n"
                + " &lt;synchToken&gt;&lt;![CDATA[${xpathSynchToken.result}]]&gt;&lt;/synchToken&gt;\n"
                + "&lt;/usersession&gt;");
        document.appendChild(setVariableAssertionElement);
        String prefix = "prefix";

        setVariableAssertionBuilder.buildAssertionElement(setVariableAssertionElement, policyBuilderContext);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("userSession", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        String expectedB64 = "PHVzZXJzZXNzaW9uPgogPHVzZXI+PCFbQ0RBVEFbJHtjdXJyZW50LnVzZXJuYW1lfV1dPjwvdXNlcj4KIDxyb2xlPjwhW0NEQVRBWyR7Y3VycmVudC51c2VyLnJvbGV9XV0+PC9yb2xlPgogPGxvb2t1cFVzZXI+PCFbQ0RBVEFbJHtsb29rdXBVc2VyfV1dPjwvbG9va3VwVXNlcj4KIDxzeW5jaFRva2VuPjwhW0NEQVRBWyR7eHBhdGhTeW5jaFRva2VuLnJlc3VsdH1dXT48L3N5bmNoVG9rZW4+CjwvdXNlcnNlc3Npb24+";
        String b64 = expressionElement.getAttribute(PolicyEntityBuilder.STRING_VALUE);
        assertEquals(expectedB64, b64);

        String value = new String(decodeBase64(b64));
        assertEquals(
                "<usersession>\n"
                        + " <user><![CDATA[${current.username}]]></user>\n"
                        + " <role><![CDATA[${current.user.role}]]></role>\n"
                        + " <lookupUser><![CDATA[${lookupUser}]]></lookupUser>\n"
                        + " <synchToken><![CDATA[${xpathSynchToken.result}]]></synchToken>\n"
                        + "</usersession>", value);
    }

    @Test
    void testPrepareSetVariableAssertionENV() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "ENV.my-var", "base64Text");
        document.appendChild(setVariableAssertionElement);
        String prefix = "path.xml";

        setVariableAssertionBuilder.buildAssertionElement(setVariableAssertionElement, policyBuilderContext);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("ENV.my-var", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        assertEquals(PREFIX_ENV + prefix + ".my-var", expressionElement.getAttribute(SetVariableAssertionBuilder.ENV_PARAM_NAME));
        assertFalse(expressionElement.hasAttribute(PolicyEntityBuilder.STRING_VALUE));
    }

    @Test
    void testPrepareSetVariableAssertionMissingVariableToSet() {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element expression = document.createElement(EXPRESSION);
        expression.appendChild(document.createCDATASection("ashdkjsah"));
        setVariableAssertion.appendChild(expression);
        String prefix = "prefix";

        assertThrows(EntityBuilderException.class, () -> setVariableAssertionBuilder.buildAssertionElement(setVariableAssertion, policyBuilderContext));
    }

    @Test
    void testPrepareSetVariableAssertionMissingBase64Value() throws DocumentParseException {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element variableToSet = document.createElement(VARIABLE_TO_SET);
        variableToSet.setAttribute(PolicyEntityBuilder.STRING_VALUE, "my.var");
        setVariableAssertion.appendChild(variableToSet);
        String prefix = "prefix";

        setVariableAssertionBuilder.buildAssertionElement(setVariableAssertion, policyBuilderContext);

        Element nameElement = getSingleElement(setVariableAssertion, VARIABLE_TO_SET);
        assertEquals("my.var", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleChildElement(setVariableAssertion, BASE_64_EXPRESSION, true);
        assertNull(expressionElement);
    }

    @Test
    void testPrepareSetVariableAssertionNotENVEmptyValue() throws DocumentParseException {
        Element setVariableAssertionElement = createSetVariableAssertion(document, "my-var", null);
        document.appendChild(setVariableAssertionElement);
        String prefix = "prefix";

        setVariableAssertionBuilder.buildAssertionElement(setVariableAssertionElement, policyBuilderContext);

        Element nameElement = getSingleElement(setVariableAssertionElement, VARIABLE_TO_SET);
        assertEquals("my-var", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertionElement, BASE_64_EXPRESSION);
        String b64 = expressionElement.getAttribute(PolicyEntityBuilder.STRING_VALUE);
        assertTrue(b64.isEmpty());
    }

    @Test
    void testPrepareSetVariableAssertionNotENVTextNode() throws DocumentParseException {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element expression = document.createElement(EXPRESSION);
        expression.setTextContent("my \n Text \r\n Content");
        setVariableAssertion.appendChild(expression);
        Element variableToSet = document.createElement(VARIABLE_TO_SET);
        variableToSet.setAttribute(PolicyEntityBuilder.STRING_VALUE, "my-var");
        setVariableAssertion.appendChild(variableToSet);
        String prefix = "prefix";

        setVariableAssertionBuilder.buildAssertionElement(setVariableAssertion, policyBuilderContext);

        Element nameElement = getSingleElement(setVariableAssertion, VARIABLE_TO_SET);
        assertEquals("my-var", nameElement.getAttribute(PolicyEntityBuilder.STRING_VALUE));

        Element expressionElement = getSingleElement(setVariableAssertion, BASE_64_EXPRESSION);
        String b64 = expressionElement.getAttribute(PolicyEntityBuilder.STRING_VALUE);
        assertEquals(Base64.getEncoder().encodeToString("my \n Text \r\n Content".getBytes(StandardCharsets.UTF_8)), b64);
    }

    @Test
    void testPrepareSetVariableAssertionNotENVElementNode() {
        Element setVariableAssertion = document.createElement(SET_VARIABLE);
        document.appendChild(setVariableAssertion);
        Element expression = document.createElement(EXPRESSION);
        expression.appendChild(document.createElement("ashdkjsah"));
        setVariableAssertion.appendChild(expression);

        assertThrows(EntityBuilderException.class, () -> setVariableAssertionBuilder.buildAssertionElement(setVariableAssertion, policyBuilderContext));
    }
}
