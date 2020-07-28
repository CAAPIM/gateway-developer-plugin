package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.injection.InjectionRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.LinkerException;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
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

    @Test
    void simplifySetVariable() throws DocumentParseException {
        Element setVariableAssertion = createSetVariableAssertionElement("my-var", "dGVzdGluZyBzaW1wbGlmeSBzZXQgdmFyaWFibGUgYXNzZXJ0aW9u");
        String policyName = "policyName";

        Bundle resultantBundle = new Bundle();
        new SetVariableAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                policyName,
                                null,
                                resultantBundle)
                                .withAssertionElement(setVariableAssertion)
                );

        Element expressionElement = getSingleElement(setVariableAssertion, EXPRESSION);
        assertEquals("testing simplify set variable assertion", expressionElement.getFirstChild().getTextContent());

        NodeList base64ElementNodes = setVariableAssertion.getElementsByTagName(BASE_64_EXPRESSION);
        assertEquals(0, base64ElementNodes.getLength());
    }

    @Test
    void simplifySetVariableInvalid() {
        Element setVariableAssertion = createSetVariableAssertionElement("ENV.gateway.test", Base64.encodeBase64String("test".getBytes()));
        String policyName = "policyName";

        Bundle resultantBundle = new Bundle();
        assertThrows(LinkerException.class, () -> new SetVariableAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                policyName,
                                null,
                                resultantBundle)
                                .withAssertionElement(setVariableAssertion)
                ));
    }

    @Test
    void simplifySetVariableExisting() {
        Element setVariableAssertion = createSetVariableAssertionElement("ENV.test", Base64.encodeBase64String("test".getBytes()));

        Bundle resultantBundle = new Bundle();
        String policyName = "policyName";
        ContextVariableEnvironmentProperty property = new ContextVariableEnvironmentProperty(policyName + ".test", "test");
        resultantBundle.getContextVariableEnvironmentProperties().put(property.getName(), property);

        assertThrows(LinkerException.class, () -> new SetVariableAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                policyName,
                                null,
                                resultantBundle)
                                .withAssertionElement(setVariableAssertion)
                ));
    }

    @Test
    void simplifySetVariableWithBadEndingUnit() throws DocumentParseException {
        //The default Base64 decoder in java cannot decode this, but the commons-codec decoder can.
        Element setVariableAssertion = createSetVariableAssertionElement("my-var2", "ew0KICAgICAgICAgICAgICAgICJlcnJvciI6ImludmFsaWRfbWV0aG9kIiwNCiAgICAgICAgICAgICAgICAiZXJyb3JfZGVzY3JpcHRpb24iOiIke3JlcXVlc3QuaHR0cC5tZXRob2R9IG5vdCBwZXJtaXR0ZWQiDQogICAgICAgICAgICAgICAgfQ=");
        String policyName = "policyName";

        Bundle resultantBundle = new Bundle();
        new SetVariableAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                policyName,
                                null,
                                resultantBundle)
                                .withAssertionElement(setVariableAssertion)
                );

        Element expressionElement = getSingleElement(setVariableAssertion, EXPRESSION);
    assertEquals(
        "{\r\n"
            + "                &quot;error&quot;:&quot;invalid_method&quot;,\r\n"
            + "                &quot;error_description&quot;:&quot;${request.http.method} not permitted&quot;\r\n"
            + "                }",
        expressionElement.getFirstChild().getTextContent());

        NodeList base64ElementNodes = setVariableAssertion.getElementsByTagName(BASE_64_EXPRESSION);
        assertEquals(0, base64ElementNodes.getLength());
    }

    @Test
    void simplifySetVariableWithXMLContent() throws DocumentParseException {
        //The default Base64 decoder in java cannot decode this, but the commons-codec decoder can.
        Element setVariableAssertion = createSetVariableAssertionElement("userSession", "PHVzZXJzZXNzaW9uPgogPHVzZXI+PCFbQ0RBVEFbJHtjdXJyZW50LnVzZXJuYW1lfV1dPjwvdXNlcj4KIDxyb2xlPjwhW0NEQVRBWyR7Y3VycmVudC51c2VyLnJvbGV9XV0+PC9yb2xlPgogPGxvb2t1cFVzZXI+PCFbQ0RBVEFbJHtsb29rdXBVc2VyfV1dPjwvbG9va3VwVXNlcj4KIDxzeW5jaFRva2VuPjwhW0NEQVRBWyR7eHBhdGhTeW5jaFRva2VuLnJlc3VsdH1dXT48L3N5bmNoVG9rZW4+CjwvdXNlcnNlc3Npb24+");
        String policyName = "policyName";

        Bundle resultantBundle = new Bundle();
        new SetVariableAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                policyName,
                                null,
                                resultantBundle)
                                .withAssertionElement(setVariableAssertion)
                );

        Element expressionElement = getSingleElement(setVariableAssertion, EXPRESSION);
    assertEquals(
        "&lt;usersession&gt;\n"
            + " &lt;user&gt;&lt;![CDATA[${current.username}]]&gt;&lt;/user&gt;\n"
            + " &lt;role&gt;&lt;![CDATA[${current.user.role}]]&gt;&lt;/role&gt;\n"
            + " &lt;lookupUser&gt;&lt;![CDATA[${lookupUser}]]&gt;&lt;/lookupUser&gt;\n"
            + " &lt;synchToken&gt;&lt;![CDATA[${xpathSynchToken.result}]]&gt;&lt;/synchToken&gt;\n"
            + "&lt;/usersession&gt;",
        expressionElement.getFirstChild().getTextContent());

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
        new AuthenticationAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                 "policy",
                                bundle,
                                null)
                                .withAssertionElement(authenticationAssertion)
                );

        assertEquals(testName, getSingleChildElementAttribute(authenticationAssertion, ID_PROV_NAME, STRING_VALUE));
        assertNull(getSingleChildElement(authenticationAssertion, ID_PROV_OID, true));
    }

    @Test
    void simplifyAuthenticationAssertionToInternalIDP() throws DocumentParseException {
        Element authenticationAssertion = createAuthenticationAssertionElement(INTERNAL_IDP_ID);
        new AuthenticationAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                new Bundle(),
                                null)
                                .withAssertionElement(authenticationAssertion)
                );
        assertEquals(INTERNAL_IDP_NAME, getSingleChildElementAttribute(authenticationAssertion, ID_PROV_NAME, STRING_VALUE));
        assertNull(getSingleChildElement(authenticationAssertion, ID_PROV_OID, true));
    }

    @Test
    void simplifyAuthenticationAssertionToMissingIDP() throws DocumentParseException {
        String id = new IdGenerator().generate();
        Element authenticationAssertion = createAuthenticationAssertionElement(id);
        new AuthenticationAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                new Bundle(),
                                null)
                                .withAssertionElement(authenticationAssertion)
                );
        assertEquals(id, getSingleChildElementAttribute(authenticationAssertion, ID_PROV_OID, GOID_VALUE));
        assertNull(getSingleChildElement(authenticationAssertion, ID_PROV_NAME, true));
    }

    @Test
    void simplifySpecificUserAssertion() throws DocumentParseException {
        String id = new IdGenerator().generate();
        String testName = "test";
        IdentityProvider identityProvider = new IdentityProvider.Builder()
                .id(id)
                .name(testName)
                .build();
        Bundle bundle = new Bundle();
        bundle.addEntity(identityProvider);
        Element authenticationAssertion = createSpecificUserAssertionElement(id);
        new SpecificUserAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                bundle,
                                null)
                                .withAssertionElement(authenticationAssertion)
                );

        assertEquals(testName, getSingleChildElementAttribute(authenticationAssertion, ID_PROV_NAME, STRING_VALUE));
        assertNull(getSingleChildElement(authenticationAssertion, ID_PROV_OID, true));
    }

    @Test
    void simplifySpecificUserAssertionToInternalIDP() throws DocumentParseException {
        Element authenticationAssertion = createSpecificUserAssertionElement(INTERNAL_IDP_ID);
        new AuthenticationAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                new Bundle(),
                                null)
                                .withAssertionElement(authenticationAssertion)
                );
        assertEquals(INTERNAL_IDP_NAME, getSingleChildElementAttribute(authenticationAssertion, ID_PROV_NAME, STRING_VALUE));
        assertNull(getSingleChildElement(authenticationAssertion, ID_PROV_OID, true));
    }

    @Test
    void simplifySpecificUserAssertionToMissingIDP() throws DocumentParseException {
        String id = new IdGenerator().generate();
        Element authenticationAssertion = createSpecificUserAssertionElement(id);
        new AuthenticationAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                new Bundle(),
                                null)
                                .withAssertionElement(authenticationAssertion)
                );
        assertEquals(id, getSingleChildElementAttribute(authenticationAssertion, ID_PROV_OID, GOID_VALUE));
        assertNull(getSingleChildElement(authenticationAssertion, ID_PROV_NAME, true));
    }

    @Test
    void simplifyJmsRoutingAssertion() throws DocumentParseException {
        String id = new IdGenerator().generate();
        String name = "jms-test";
        JmsDestination jmsDestination = new JmsDestination.Builder()
                .id(id)
                .name(name)
                .build();
        Bundle bundle = new Bundle();
        bundle.addEntity(jmsDestination);
        Element jmsRoutingAssertionEle = createJmsRoutingAssertion(id, name);
        new JmsRoutingAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                bundle,
                                null)
                                .withAssertionElement(jmsRoutingAssertionEle)
                );
        
        assertEquals(name, getSingleChildElementAttribute(jmsRoutingAssertionEle, JMS_ENDPOINT_NAME, STRING_VALUE));
        assertNull(getSingleChildElement(jmsRoutingAssertionEle, JMS_ENDPOINT_OID, true));
    }

    @Test
    void simplifyJmsRoutingAssertionMissingJmsDestination() throws DocumentParseException {
        String id = new IdGenerator().generate();
        String name = "jms-test";
        Element jmsRoutingAssertionEle = createJmsRoutingAssertion(id, name);

        new JmsRoutingAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                new Bundle(),
                                null)
                                .withAssertionElement(jmsRoutingAssertionEle)
                );
        assertEquals(name, getSingleChildElementAttribute(jmsRoutingAssertionEle, JMS_ENDPOINT_NAME, STRING_VALUE));
        assertNull(getSingleChildElement(jmsRoutingAssertionEle, JMS_ENDPOINT_OID, true));
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
        Bundle resultantBundle = new Bundle();
        resultantBundle.addEntity(policy);

        Element includeAssertion = createIncludeAssertionElement(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), id);
        new IncludeAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                testName,
                                bundle,
                                resultantBundle)
                                .withAssertionElement(includeAssertion)
                );

        assertEquals(testName, getSingleChildElementAttribute(includeAssertion, POLICY_GUID, "policyPath"));
        assertNull(getSingleChildElementAttribute(includeAssertion, POLICY_GUID, STRING_VALUE));
        assertTrue(resultantBundle.getMissingEntities().isEmpty());
    }

    @Test
    void simplifyIncludeAssertionMissingPolicy() throws DocumentParseException {
        String id = new IdGenerator().generate();
        String testName = "test";
        Bundle bundle = new Bundle();
        Bundle resultantBundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        FolderTree folderTree = new FolderTree(bundle.getEntities(Folder.class).values());
        bundle.setFolderTree(folderTree);

        Element includeAssertion = createIncludeAssertionElement(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), id);
        new IncludeAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                bundle,
                                resultantBundle)
                                .withAssertionElement(includeAssertion)
                );

        assertNotEquals(testName, getSingleChildElementAttribute(includeAssertion, POLICY_GUID, "policyPath"));
        assertNull(getSingleChildElementAttribute(includeAssertion, POLICY_GUID, STRING_VALUE));
        assertTrue(getSingleChildElementAttribute(includeAssertion, POLICY_GUID, "policyPath").startsWith("Policy#"));
        assertFalse(resultantBundle.getMissingEntities().isEmpty());
    }

    @Test
    void simplifyIncludeAssertionMissingPolicyInResultantBundleOnly() throws DocumentParseException {
        String id = new IdGenerator().generate();
        String testName = "test";
        Policy policy = new Policy.Builder()
                .setGuid(id)
                .setName(testName)
                .setParentFolderId(ROOT_FOLDER_ID)
                .build();
        Bundle bundle = new Bundle();
        Bundle resultantBundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        FolderTree folderTree = new FolderTree(bundle.getEntities(Folder.class).values());
        bundle.setFolderTree(folderTree);
        bundle.addEntity(policy);

        Element includeAssertion = createIncludeAssertionElement(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), id);
        new IncludeAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                bundle,
                                resultantBundle)
                                .withAssertionElement(includeAssertion)
                );

        assertEquals(testName, getSingleChildElementAttribute(includeAssertion, POLICY_GUID, "policyPath"));
        assertNull(getSingleChildElementAttribute(includeAssertion, POLICY_GUID, STRING_VALUE));
        assertFalse(resultantBundle.getMissingEntities().isEmpty());
        assertNotNull(resultantBundle.getMissingEntities().get(policy.getId()));
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

        InjectionRegistry.getInstance(PolicyXMLSimplifier.class).simplifyPolicyXML(policyXML, policy.getName(), bundle, bundle);
    }

    @Test
    void simplifyHardcodedResponse() {
        Element hardcodedResponse = createHardcodedResponse(true);
        new HardcodedResponseAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                null,
                                null)
                                .withAssertionElement(hardcodedResponse)
                );
        assertEquals("Test", getSingleChildElementTextContent(hardcodedResponse, RESPONSE_BODY));
    }

    @Test
    void simplifyHardcodedResponseNoBody() {
        Element hardcodedResponse = createHardcodedResponse(false);
        new HardcodedResponseAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                null,
                                null)
                                .withAssertionElement(hardcodedResponse)
                );
        assertNull(getSingleChildElementTextContent(hardcodedResponse, RESPONSE_BODY));
    }

    @Test
    void simplifyEncapsulatedAssertion() throws DocumentParseException {
        Element encapsulatedAssertion = createEncapsulatedAssertion();
        Bundle bundle = new Bundle();
        Bundle resultantBundle = new Bundle();
        Encass encass = new Encass();
        encass.setGuid("Test Guid");
        encass.setName("Test Name");
        encass.setPolicyId("Policy");
        Policy policy = new Policy();
        policy.setId("Policy");
        bundle.getEncasses().put(encass.getGuid(), encass);
        bundle.getPolicies().put(policy.getId(), policy);
        resultantBundle.getEncasses().put(encass.getGuid(), encass);
        resultantBundle.getPolicies().put(policy.getId(), policy);

        new EncapsulatedAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "Policy",
                                bundle,
                                resultantBundle)
                                .withAssertionElement(encapsulatedAssertion)
                );

        assertEquals("Test Name", encapsulatedAssertion.getAttribute("encassName"));
        assertNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_NAME, true));
        assertNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_GUID, true));
        assertTrue(resultantBundle.getMissingEntities().isEmpty());
    }

    @Test
    void simplifyEncapsulatedAssertionMissingPolicy() throws DocumentParseException {
        Element encapsulatedAssertion = createEncapsulatedAssertion();
        Bundle bundle = new Bundle();
        Bundle resultantBundle = new Bundle();
        Encass encass = new Encass();
        encass.setGuid("Test Guid");
        encass.setName("Test Name");
        encass.setPolicyId("Policy");
        bundle.getEncasses().put(encass.getGuid(), encass);
        resultantBundle.getEncasses().put(encass.getGuid(), encass);

        new EncapsulatedAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "Policy",
                                bundle,
                                resultantBundle)
                                .withAssertionElement(encapsulatedAssertion)
                );

        assertEquals("Test Name", encapsulatedAssertion.getAttribute("encassName"));
        assertNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_NAME, true));
        assertNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_GUID, true));
        assertNotNull(resultantBundle.getMissingEntities().get("Test Guid"));
    }

    @Test
    void simplifyEncapsulatedAssertionMissingEncass() throws DocumentParseException {
        Element encapsulatedAssertion = createEncapsulatedAssertion();
        Bundle bundle = new Bundle();
        Bundle resultantBundle = new Bundle();
        new EncapsulatedAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "Policy",
                                bundle,
                                resultantBundle)
                                .withAssertionElement(encapsulatedAssertion)
                );

        assertEquals("Test Name", encapsulatedAssertion.getAttribute("encassName"));
        assertNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_NAME, true));
        assertNull(getSingleChildElement(encapsulatedAssertion, ENCAPSULATED_ASSERTION_CONFIG_GUID, true));
        assertNotNull(resultantBundle.getMissingEntities().get("Test Guid"));
    }

    @Test
    void simplifyHttpRoutingAssertionForTrustedCert() throws DocumentParseException {
        final IdGenerator idGenerator = new IdGenerator();
        Element httpRoutingAssertionElement = createHttpRoutingAssertionWithCerts(idGenerator);
        new HttpRoutingAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                null,
                                null)
                                .withAssertionElement(httpRoutingAssertionElement)
                );

        final Element trustedCertNameElement = getSingleChildElement(httpRoutingAssertionElement, TLS_TRUSTED_CERT_NAMES, true);
        assertNotNull(trustedCertNameElement);
        assertEquals(2, trustedCertNameElement.getChildNodes().getLength());
        assertEquals("fake-cert-1", trustedCertNameElement.getChildNodes().item(0).getAttributes().getNamedItem(STRING_VALUE).getTextContent());
        assertEquals("fake-cert-2", trustedCertNameElement.getChildNodes().item(1).getAttributes().getNamedItem(STRING_VALUE).getTextContent());
        assertNull(getSingleChildElement(httpRoutingAssertionElement, TLS_TRUSTED_CERT_IDS, true));
    }

    @Test
    void simplifyHttp2RoutingAssertionForClientConfig() throws DocumentParseException {
        final IdGenerator idGenerator = new IdGenerator();
        Element http2RoutingAssertionElement = createHttp2RoutingAssertionWithClientConfig(idGenerator);
        new Http2AssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                null,
                                null)
                                .withAssertionElement(http2RoutingAssertionElement)
                );

        final Element http2ClientConfigNameElement = getSingleChildElement(http2RoutingAssertionElement,
                HTTP2_CLIENT_CONFIG_NAME, true);
        assertNotNull(http2ClientConfigNameElement);
        assertEquals("http2client",
                http2ClientConfigNameElement.getAttributes().getNamedItem(STRING_VALUE).getTextContent());
        assertNull(getSingleChildElement(http2RoutingAssertionElement, HTTP2_CLIENT_CONFIG_GOID, true));
    }

    @Test
    void simplifyMqRoutingAssertion() throws DocumentParseException {
        final IdGenerator idGenerator = new IdGenerator();
        Element mqRoutingAssertionElement = createMqRoutingAssertion(idGenerator);
        new MQRoutingAssertionSimplifier()
                .simplifyAssertionElement(
                        new PolicySimplifierContext(
                                "policy",
                                null,
                                null)
                                .withAssertionElement(mqRoutingAssertionElement)
                );

        assertNull(getSingleChildElement(mqRoutingAssertionElement, ACTIVE_CONNECTOR_GOID, true));
        assertNull(getSingleChildElement(mqRoutingAssertionElement, ACTIVE_CONNECTOR_ID, true));
    }

    @NotNull
    private Element createMqRoutingAssertion(final IdGenerator idGenerator) {
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Element activeConnectorGoidElement = createElementWithAttributes(
                document,
                ACTIVE_CONNECTOR_GOID,
                ImmutableMap.of(GOID_VALUE, idGenerator.generate())
        );

        Element activeConnectorIdElement = createElementWithAttributes(
                document,
                ACTIVE_CONNECTOR_ID,
                ImmutableMap.of(GOID_VALUE, idGenerator.generate())
        );

        return createElementWithChildren(
                document,
                MQ_ROUTING_ASSERTION,
                activeConnectorGoidElement,
                activeConnectorIdElement
        );
    }

    @NotNull
    private Element createHttpRoutingAssertionWithCerts(final IdGenerator idGenerator) {
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Element trustedCertGoidsElement = createElementWithAttributesAndChildren(
                document,
                TLS_TRUSTED_CERT_IDS,
                ImmutableMap.of(GOID_ARRAY_VALUE, "included"),
                createElementWithAttribute(document, PolicyXMLElements.ITEM, GOID_VALUE, idGenerator.generate()),
                createElementWithAttribute(document, PolicyXMLElements.ITEM, GOID_VALUE, idGenerator.generate())
        );

        Element trustedCertNamesElement = createElementWithAttributesAndChildren(
                document,
                TLS_TRUSTED_CERT_NAMES,
                ImmutableMap.of("stringArrayValue", "included"),
                createElementWithAttribute(document, PolicyXMLElements.ITEM, STRING_VALUE, "fake-cert-1"),
                createElementWithAttribute(document, PolicyXMLElements.ITEM, STRING_VALUE, "fake-cert-2")
        );

        return createElementWithChildren(
                document,
                HTTP_ROUTING_ASSERTION,
                trustedCertGoidsElement,
                trustedCertNamesElement
        );
    }

    @NotNull
    private Element createHttp2RoutingAssertionWithClientConfig(final IdGenerator idGenerator) {
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();

        return createElementWithChildren(
                document,
                HTTP2_ROUTING_ASSERTION,
                createElementWithAttribute(document, "L7p:ProtectedServiceUrl", STRING_VALUE, "http://apim-hugh-new.lvn.broadcom.net:90"),
                createElementWithAttribute(document, HTTP2_CLIENT_CONFIG_NAME, STRING_VALUE, "http2client"),
                createElementWithAttribute(document, HTTP2_CLIENT_CONFIG_GOID, STRING_VALUE, idGenerator.generate())
        );
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

    @NotNull
    private Element createSpecificUserAssertionElement(String id) {
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        return createElementWithChildren(
                document,
                AUTHENTICATION,
                createElementWithAttribute(document, ID_PROV_OID, GOID_VALUE, id),
                createElementWithAttribute(document, TARGET, "target", "RESPONSE")
        );
    }
    
    @NotNull
    private Element createJmsRoutingAssertion(String id, String name) {
        Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        return createElementWithChildren(
                document,
                JMS_ROUTING_ASSERTION,
                createElementWithAttribute(document, JMS_ENDPOINT_OID, GOID_VALUE, id),
                createElementWithAttribute(document, JMS_ENDPOINT_NAME, STRING_VALUE, name)
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