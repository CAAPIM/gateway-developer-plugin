/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils.unixPath;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class PolicyLoaderTest {

    private static final String TEST_GUID = UUID.randomUUID().toString();
    private static final String TEST_POLICY_ID = "PolicyID";
    private static final String TEST_POLICY_NAME = "Policy";
    private static final String TEST_FOLDER_1 = "Folder1";
    private PolicyLoader loader = new PolicyLoader();
    private static final String TEST_POLICY_XML = "&lt;wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\"&gt;\n" +
            "    &lt;wsp:All wsp:Usage=\"Required\"&gt;\n" +
            "        &lt;L7p:CommentAssertion&gt;\n" +
            "            &lt;L7p:Comment stringValue=\"comment\"/&gt;\n" +
            "        &lt;/L7p:CommentAssertion&gt;\n" +
            "    &lt;/wsp:All&gt;\n" +
            "&lt;/wsp:Policy&gt;";
    private static final String TEST_POLICY_XML_WITH_ROUTING = "&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;\n" +
            "&lt;wsp:Policy xmlns:L7p=&quot;http://www.layer7tech.com/ws/policy&quot; xmlns:wsp=&quot;http://schemas.xmlsoap.org/ws/2002/12/policy&quot;&gt;\n" +
            "    &lt;wsp:All wsp:Usage=&quot;Required&quot;&gt;\n" +
            "        &lt;L7p:Http2Routing&gt;\n" +
            "            &lt;L7p:Http2ClientConfigName stringValue=&quot;&amp;lt;Default Config&gt;&quot;/&gt;\n" +
            "            &lt;L7p:ProtectedServiceUrl stringValue=&quot;http://apim-hugh-new.lvn.broadcom.net:90&quot;/&gt;\n" +
            "        &lt;/L7p:Http2Routing&gt;\n" +
            "    &lt;/wsp:All&gt;\n" +
            "&lt;/wsp:Policy&gt;";

    @Test
    void test() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        Folder f1 = new Folder();
        f1.setId(TEST_FOLDER_1);
        f1.setName(TEST_FOLDER_1);
        f1.setPath(TEST_FOLDER_1);
        bundle.getFolders().put(TEST_FOLDER_1, f1);

        loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID, TEST_POLICY_NAME,
                PolicyType.SERVICE_OPERATION.getType(), TEST_FOLDER_1, TEST_POLICY_XML, null, false));

        assertFalse(bundle.getPolicies().isEmpty());
        assertEquals(1, bundle.getPolicies().size());

        String path = PathUtils.unixPath(TEST_FOLDER_1, TEST_POLICY_NAME);
        Policy policy = bundle.getPolicies().get(path);
        assertNotNull(policy);
        assertEquals(TEST_POLICY_ID, policy.getId());
        assertEquals(TEST_POLICY_NAME, policy.getName());
        assertEquals(path, policy.getPath());
        assertEquals(TEST_GUID, policy.getGuid());
        assertEquals(f1, policy.getParentFolder());

        assertFalse(policy.isHasRouting());
    }

    @Test
    void testUnsupportedPolicyType() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();

        loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID, TEST_POLICY_NAME, "Unsupported", TEST_FOLDER_1
                , TEST_POLICY_XML, null, false));

        assertTrue(bundle.getPolicies().isEmpty());
    }

    @Test
    void testMissingFolder() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();

        assertThrows(BundleLoadException.class, () -> loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID,
                TEST_POLICY_NAME, PolicyType.SERVICE_OPERATION.getType(), TEST_FOLDER_1, TEST_POLICY_XML, null, false)));
    }

    @Test
    void testRepeatedFolders() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        Folder f1 = new Folder();
        f1.setId(TEST_FOLDER_1);
        f1.setName(TEST_FOLDER_1);
        f1.setPath(TEST_FOLDER_1);
        bundle.getFolders().put(TEST_FOLDER_1, f1);
        bundle.getFolders().put(TEST_FOLDER_1 + "_1", f1);

        assertThrows(BundleLoadException.class, () -> loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID,
                TEST_POLICY_NAME, PolicyType.SERVICE_OPERATION.getType(), TEST_FOLDER_1, TEST_POLICY_XML, null, false)));
    }

    @Test
    void testGlobalPolicy() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        Folder f1 = new Folder();
        f1.setId(TEST_FOLDER_1);
        f1.setName(TEST_FOLDER_1);
        f1.setPath(TEST_FOLDER_1);
        bundle.getFolders().put(TEST_FOLDER_1, f1);

        loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID, TEST_POLICY_NAME, PolicyType.GLOBAL.getType(),
                TEST_FOLDER_1, TEST_POLICY_XML, null, false));

        assertFalse(bundle.getPolicies().isEmpty());
        assertEquals(1, bundle.getPolicies().size());

        String path = PathUtils.unixPath(TEST_FOLDER_1, TEST_POLICY_NAME);
        Policy policy = bundle.getPolicies().get(path);
        assertNotNull(policy);
        assertEquals(TEST_POLICY_ID, policy.getId());
        assertEquals(TEST_POLICY_NAME, policy.getName());
        assertEquals(path, policy.getPath());
        assertEquals(TEST_GUID, policy.getGuid());
        assertEquals(f1, policy.getParentFolder());
        assertTrue(policy instanceof GlobalPolicy);
        assertTrue(bundle.getEntities(GlobalPolicy.class).values().contains(policy));
    }


    @Test
    void testAuditPolicy() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        Folder f1 = new Folder();
        f1.setId(TEST_FOLDER_1);
        f1.setName(TEST_FOLDER_1);
        f1.setPath(TEST_FOLDER_1);
        bundle.getFolders().put(TEST_FOLDER_1, f1);

        loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID, TEST_POLICY_NAME,
                PolicyType.INTERNAL.getType(), TEST_FOLDER_1, TEST_POLICY_XML, "audit-lookup", false));

        assertFalse(bundle.getPolicies().isEmpty());
        assertEquals(1, bundle.getPolicies().size());

        String path = PathUtils.unixPath(TEST_FOLDER_1, TEST_POLICY_NAME);
        Policy policy = bundle.getPolicies().get(path);
        assertNotNull(policy);
        assertEquals(TEST_POLICY_ID, policy.getId());
        assertEquals(TEST_POLICY_NAME, policy.getName());
        assertEquals(path, policy.getPath());
        assertEquals(TEST_GUID, policy.getGuid());
        assertEquals(f1, policy.getParentFolder());
        assertTrue(policy instanceof AuditPolicy);
        assertTrue(bundle.getEntities(AuditPolicy.class).values().contains(policy));
    }

    @Test
    void testAuditPolicyWithInvalidTags() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        Folder f1 = new Folder();
        f1.setId(TEST_FOLDER_1);
        f1.setName(TEST_FOLDER_1);
        f1.setPath(TEST_FOLDER_1);
        bundle.getFolders().put(TEST_FOLDER_1, f1);

        loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID, TEST_POLICY_NAME,
                PolicyType.INTERNAL.getType(), TEST_FOLDER_1, TEST_POLICY_XML, "test", false));

        assertTrue(bundle.getPolicies().isEmpty());
        assertTrue(bundle.getEntities(AuditPolicy.class).isEmpty());
    }

    @Test
    void testHasRoutingWithRoutingAssertion() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        Folder f1 = new Folder();
        f1.setId(TEST_FOLDER_1);
        f1.setName(TEST_FOLDER_1);
        f1.setPath(TEST_FOLDER_1);
        bundle.getFolders().put(TEST_FOLDER_1, f1);

        loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID, TEST_POLICY_NAME,
                PolicyType.SERVICE_OPERATION.getType(), TEST_FOLDER_1, TEST_POLICY_XML_WITH_ROUTING, null, true));

        assertFalse(bundle.getPolicies().isEmpty());
        assertEquals(1, bundle.getPolicies().size());

        String path = PathUtils.unixPath(TEST_FOLDER_1, TEST_POLICY_NAME);
        Policy policy = bundle.getPolicies().get(path);
        assertNotNull(policy);
        assertEquals(TEST_POLICY_ID, policy.getId());
        assertEquals(TEST_POLICY_NAME, policy.getName());
        assertEquals(path, policy.getPath());
        assertEquals(TEST_GUID, policy.getGuid());
        assertEquals(f1, policy.getParentFolder());

        assertTrue(policy.isHasRouting());
    }

    private static Element createPolicyBundleXml(Document document, String policyID, String policyName,
                                                 String policyType, String folderID, String policyXml, String tag,
                                                 boolean hasRouting) {
        Element element = createElementWithAttributesAndChildren(
                document,
                POLICY,
                ImmutableMap.of(ATTRIBUTE_ID, policyID, ATTRIBUTE_GUID, TEST_GUID)
        );
        Element policyDetailElement = createElementWithAttributesAndChildren(
                document,
                POLICY_DETAIL,
                ImmutableMap.of(ATTRIBUTE_FOLDER_ID, folderID, ATTRIBUTE_GUID, TEST_GUID, ATTRIBUTE_ID, policyID),
                createElementWithTextContent(document, NAME, policyName),
                createElementWithTextContent(document, POLICY_TYPE, policyType)
        );
        Map<String, Object> properties = new HashMap<>();
        properties.put(PropertyConstants.PROPERTY_HAS_ROUTING, hasRouting);
        if (tag != null) {
            properties.put(PropertyConstants.PROPERTY_TAG, tag);
        }
        BuilderUtils.buildAndAppendPropertiesElement(properties, document, policyDetailElement);

        element.appendChild(policyDetailElement);
        element.appendChild(
                createElementWithChildren(
                        document,
                        RESOURCES,
                        createElementWithChildren(
                                document,
                                RESOURCE_SET,
                                createElementWithTextContent(
                                        document,
                                        RESOURCE,
                                        policyXml
                                )
                        )
                )
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, TEST_POLICY_ID),
                createElementWithTextContent(document, TYPE, EntityTypes.POLICY_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        element
                )
        );
    }
}