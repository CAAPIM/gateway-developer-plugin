/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Paths;
import java.util.UUID;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType.SERVICE_OPERATION;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
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

    @Test
    void test() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        Folder f1 = new Folder();
        f1.setId(TEST_FOLDER_1);
        f1.setName(TEST_FOLDER_1);
        f1.setPath(TEST_FOLDER_1);
        bundle.getFolders().put(TEST_FOLDER_1, f1);

        loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID, TEST_POLICY_NAME, SERVICE_OPERATION.getType(), TEST_FOLDER_1));

        assertFalse(bundle.getPolicies().isEmpty());
        assertEquals(1, bundle.getPolicies().size());

        String path = Paths.get(TEST_FOLDER_1, TEST_POLICY_NAME + ".xml").toString();
        Policy policy = bundle.getPolicies().get(path);
        assertNotNull(policy);
        assertEquals(TEST_POLICY_ID, policy.getId());
        assertEquals(TEST_POLICY_NAME, policy.getName());
        assertEquals(path, policy.getPath());
        assertEquals(TEST_GUID, policy.getGuid());
        assertEquals(f1, policy.getParentFolder());
    }

    @Test
    void testUnsupportedPolicyType() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();

        loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID, TEST_POLICY_NAME, "Unsupported", TEST_FOLDER_1));

        assertTrue(bundle.getPolicies().isEmpty());
    }

    @Test
    void testMissingFolder() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();

        assertThrows(DependencyBundleLoadException.class, () -> loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID, TEST_POLICY_NAME, SERVICE_OPERATION.getType(), TEST_FOLDER_1)));
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

        assertThrows(DependencyBundleLoadException.class, () -> loader.load(bundle, createPolicyBundleXml(doc, TEST_POLICY_ID, TEST_POLICY_NAME, SERVICE_OPERATION.getType(), TEST_FOLDER_1)));
    }

    private static Element createPolicyBundleXml(Document document, String policyID, String policyName, String policyType, String folderID) {
        Element element = createElementWithAttributesAndChildren(
                document,
                POLICY,
                ImmutableMap.of(ATTRIBUTE_ID, policyID, ATTRIBUTE_GUID, TEST_GUID),
                createElementWithAttributesAndChildren(
                        document,
                        POLICY_DETAIL,
                        ImmutableMap.of(ATTRIBUTE_FOLDER_ID, folderID, ATTRIBUTE_GUID, TEST_GUID, ATTRIBUTE_ID, policyID),
                        createElementWithTextContent(document, NAME, policyName),
                        createElementWithTextContent(document, POLICY_TYPE, policyType)
                )
        );

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
                                        TEST_POLICY_XML
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