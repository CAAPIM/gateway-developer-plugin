package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.beans.EnvironmentProperty.Type;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.LinkerException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER;
import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER_ID;
import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_ID;
import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.jupiter.api.Assertions.*;

class PolicyXMLSimplifierTest {
    private PolicyXMLSimplifier policyXMLSimplifier = PolicyXMLSimplifier.INSTANCE;

    @Test
    void simplifySetVariable() throws DocumentParseException {
        Element setVariableAssertion = createSetVariableAssertionElement("my-var", "dGVzdGluZyBzaW1wbGlmeSBzZXQgdmFyaWFibGUgYXNzZXJ0aW9u");

        Bundle resultantBundle = new Bundle();
        policyXMLSimplifier.simplifySetVariable(setVariableAssertion, resultantBundle);

        Element expressionElement = getSingleElement(setVariableAssertion, EXPRESSION);
        assertEquals("testing simplify set variable assertion", expressionElement.getFirstChild().getTextContent());

        NodeList base64ElementNodes = setVariableAssertion.getElementsByTagName(BASE_64_EXPRESSION);
        assertEquals(0, base64ElementNodes.getLength());
    }

    @Test
    void simplifySetVariableInvalid() {
        Element setVariableAssertion = createSetVariableAssertionElement("ENV.gateway.test", Base64.encodeBase64String("test".getBytes()));

        Bundle resultantBundle = new Bundle();
        assertThrows(LinkerException.class, () -> policyXMLSimplifier.simplifySetVariable(setVariableAssertion, resultantBundle));
    }

    @Test
    void simplifySetVariableExisting() {
        Element setVariableAssertion = createSetVariableAssertionElement("ENV.test", Base64.encodeBase64String("test".getBytes()));

        Bundle resultantBundle = new Bundle();
        EnvironmentProperty property = new EnvironmentProperty("test", "test", Type.LOCAL);
        resultantBundle.getEnvironmentProperties().put(property.getId(), property);
        assertThrows(LinkerException.class, () -> policyXMLSimplifier.simplifySetVariable(setVariableAssertion, resultantBundle));
    }

    @Test
    void simplifySetVariableWithBadEndingUnit() throws DocumentParseException {
        //The default Base64 decoder in java cannot decode this, but the commons-codec decoder can.
        Element setVariableAssertion = createSetVariableAssertionElement("my-var2", "ew0KICAgICAgICAgICAgICAgICJlcnJvciI6ImludmFsaWRfbWV0aG9kIiwNCiAgICAgICAgICAgICAgICAiZXJyb3JfZGVzY3JpcHRpb24iOiIke3JlcXVlc3QuaHR0cC5tZXRob2R9IG5vdCBwZXJtaXR0ZWQiDQogICAgICAgICAgICAgICAgfQ=");

        Bundle resultantBundle = new Bundle();
        policyXMLSimplifier.simplifySetVariable(setVariableAssertion, resultantBundle);

        Element expressionElement = getSingleElement(setVariableAssertion, EXPRESSION);
        assertEquals("{\r\n" +
                "                \"error\":\"invalid_method\",\r\n" +
                "                \"error_description\":\"${request.http.method} not permitted\"\r\n" +
                "                }", expressionElement.getFirstChild().getTextContent());

        NodeList base64ElementNodes = setVariableAssertion.getElementsByTagName(BASE_64_EXPRESSION);
        assertEquals(0, base64ElementNodes.getLength());
    }

    @Test
    void simplifyAuthenticationAssertion() throws DocumentParseException {
        String id = new IdGenerator().generate();
        String testName = "test";
        IdentityProvider identityProvider = new IdentityProvider.Builder()
                .id(id)
                .name(testName)
                .build();
        Bundle bundle = new Bundle();
        bundle.addEntity(identityProvider);
        Element authenticationAssertion = createAuthenticationAssertionElement(id);
        policyXMLSimplifier.simplifyAuthenticationAssertion(bundle, authenticationAssertion);

        assertEquals(testName, getSingleChildElementAttribute(authenticationAssertion, ID_PROV_NAME, STRING_VALUE));
        assertNull(getSingleChildElement(authenticationAssertion, ID_PROV_OID, true));
    }

    @Test
    void simplifyAuthenticationAssertionToInternalIDP() throws DocumentParseException {
        Element authenticationAssertion = createAuthenticationAssertionElement(INTERNAL_IDP_ID);
        policyXMLSimplifier.simplifyAuthenticationAssertion(new Bundle(), authenticationAssertion);
        assertEquals(INTERNAL_IDP_NAME, getSingleChildElementAttribute(authenticationAssertion, ID_PROV_NAME, STRING_VALUE));
        assertNull(getSingleChildElement(authenticationAssertion, ID_PROV_OID, true));
    }

    @Test
    void simplifyAuthenticationAssertionToMissingIDP() throws DocumentParseException {
        String id = new IdGenerator().generate();
        Element authenticationAssertion = createAuthenticationAssertionElement(id);
        policyXMLSimplifier.simplifyAuthenticationAssertion(new Bundle(), authenticationAssertion);
        assertEquals(id, getSingleChildElementAttribute(authenticationAssertion, ID_PROV_OID, GOID_VALUE));
        assertNull(getSingleChildElement(authenticationAssertion, ID_PROV_NAME, true));
    }

    @Test
    void simplifyIncludeAssertion() throws DocumentParseException {
        String id = new IdGenerator().generate();
        String testName = "test";
        Policy policy = new Policy.Builder()
                .setGuid(id)
                .setName(testName)
                .setParentFolderId(ROOT_FOLDER_ID)
                .build();
        Bundle bundle = new Bundle();
        bundle.addEntity(policy);
        bundle.addEntity(ROOT_FOLDER);
        FolderTree folderTree = new FolderTree(bundle.getEntities(Folder.class).values());
        bundle.setFolderTree(folderTree);

        Element includeAssertion = createIncludeAssertionElement(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), id);
        policyXMLSimplifier.simplifyIncludeAssertion(bundle, includeAssertion);

        assertEquals(testName, getSingleChildElementAttribute(includeAssertion, POLICY_GUID, "policyPath"));
        assertNull(getSingleChildElementAttribute(includeAssertion, POLICY_GUID, STRING_VALUE));
    }

    @Test
    void simplifyIncludeAssertionMissingPolicy() throws DocumentParseException {
        String id = new IdGenerator().generate();
        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        FolderTree folderTree = new FolderTree(bundle.getEntities(Folder.class).values());
        bundle.setFolderTree(folderTree);

        Element includeAssertion = createIncludeAssertionElement(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), id);
        policyXMLSimplifier.simplifyIncludeAssertion(bundle, includeAssertion);

        assertNull(getSingleChildElementAttribute(includeAssertion, POLICY_GUID, "policyPath"));
        assertEquals(id, getSingleChildElementAttribute(includeAssertion, POLICY_GUID, STRING_VALUE));
    }

    @Test
    void simplifyPolicy() {
        String policyID = new IdGenerator().generate();
        String testName = "test";
        Policy policy = new Policy.Builder()
                .setGuid(policyID)
                .setName(testName)
                .setParentFolderId(ROOT_FOLDER_ID)
                .build();
        Bundle bundle = new Bundle();
        bundle.addEntity(policy);
        bundle.addEntity(ROOT_FOLDER);
        FolderTree folderTree = new FolderTree(bundle.getEntities(Folder.class).values());
        bundle.setFolderTree(folderTree);

        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Element policyXML = createElementWithChildren(
                document,
                "wsp:Policy",
                createIncludeAssertionElement(document, policyID)
        );

        policyXMLSimplifier.simplifyPolicyXML(policyXML, bundle, bundle);
    }

    @Test
    void simplifyHardcodedResponse() {
        Element hardcodedResponse = createHardcodedResponse(true);
        policyXMLSimplifier.simplifyHardcodedResponse(hardcodedResponse);
        assertEquals("Test", getSingleChildElementTextContent(hardcodedResponse, RESPONSE_BODY));
    }

    @Test
    void simplifyHardcodedResponseNoBody() {
        Element hardcodedResponse = createHardcodedResponse(false);
        policyXMLSimplifier.simplifyHardcodedResponse(hardcodedResponse);
        assertNull(getSingleChildElementTextContent(hardcodedResponse, RESPONSE_BODY));
    }

    @Test
    void simplifyEncapsulatedAssertion() throws DocumentParseException {
        Element encapsulatedAssertion = createEncapsulatedAssertion();
        Bundle bundle = new Bundle();
        Encass encass = new Encass();
        encass.setGuid("Test Guid");
        encass.setName("Test Name");
        encass.setPolicyId("Policy");
        Policy policy = new Policy();
        policy.setId("Policy");
        bundle.getEncasses().put(encass.getGuid(), encass);
        bundle.getPolicies().put(policy.getId(), policy);

        policyXMLSimplifier.simplifyEncapsulatedAssertion(bundle, encapsulatedAssertion);

        assertEquals("Test Name", encapsulatedAssertion.getAttribute("encassName"));
        assertNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_NAME, true));
        assertNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_GUID, true));
    }

    @Test
    void simplifyEncapsulatedAssertionMissingPolicy() throws DocumentParseException {
        Element encapsulatedAssertion = createEncapsulatedAssertion();
        Bundle bundle = new Bundle();
        Encass encass = new Encass();
        encass.setGuid("Test Guid");
        encass.setName("Test Name");
        encass.setPolicyId("Policy");
        bundle.getEncasses().put(encass.getGuid(), encass);

        policyXMLSimplifier.simplifyEncapsulatedAssertion(bundle, encapsulatedAssertion);

        assertNull(StringUtils.trimToNull(encapsulatedAssertion.getAttribute("encassName")));
        assertNotNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_NAME));
        assertNotNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_GUID));
    }

    @Test
    void simplifyEncapsulatedAssertionMissingEncass() throws DocumentParseException {
        Element encapsulatedAssertion = createEncapsulatedAssertion();
        Bundle bundle = new Bundle();
        policyXMLSimplifier.simplifyEncapsulatedAssertion(bundle, encapsulatedAssertion);

        assertNull(StringUtils.trimToNull(encapsulatedAssertion.getAttribute("encassName")));
        assertNotNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_NAME));
        assertNotNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_GUID));
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

    @NotNull
    private Element createAuthenticationAssertionElement(String id) {
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        return createElementWithChildren(
                document,
                AUTHENTICATION,
                createElementWithAttribute(document, ID_PROV_OID, GOID_VALUE, id),
                createElementWithAttribute(document, TARGET, "target", "RESPONSE")
        );
    }

    private Element createIncludeAssertionElement(Document document, String id) {
        return createElementWithChildren(
                document,
                INCLUDE,
                createElementWithAttribute(document, POLICY_GUID, STRING_VALUE, id)
        );
    }

    private Element createHardcodedResponse(boolean body) {
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        String base64 = encodeBase64String("Test".getBytes());
        Element element = createElementWithChildren(
                document,
                HARDCODED_RESPONSE
        );
        if (body) {
            element.appendChild(createElementWithAttribute(document, BASE_64_RESPONSE_BODY, STRING_VALUE, base64));
        }
        return element;
    }

    private Element createEncapsulatedAssertion() {
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        return createElementWithChildren(
                document,
                ENCAPSULATED,
                createElementWithAttribute(document, ENCAPSULATED_ASSERTION_CONFIG_GUID, STRING_VALUE, "Test Guid"),
                createElementWithAttribute(document, ENCAPSULATED_ASSERTION_CONFIG_NAME, STRING_VALUE, "Test Name")
        );
    }
}