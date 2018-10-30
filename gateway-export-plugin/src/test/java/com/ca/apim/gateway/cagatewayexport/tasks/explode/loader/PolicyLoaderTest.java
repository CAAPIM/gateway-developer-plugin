/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.UUID;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.PROPERTY_TAG;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class PolicyLoaderTest {

    private static final String TEST_GUID = UUID.randomUUID().toString();
    private static final String TEST_POLICY_ID = "PolicyID";
    private static final String TEST_POLICY_NAME = "Policy";
    private static final String TEST_FOLDER_1 = "Folder1";
    private static final String TEST_POLICY_XML = "&lt;wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\"&gt;\n" +
            "    &lt;wsp:All wsp:Usage=\"Required\"&gt;\n" +
            "        &lt;L7p:CommentAssertion&gt;\n" +
            "            &lt;L7p:Comment stringValue=\"comment\"/&gt;\n" +
            "        &lt;/L7p:CommentAssertion&gt;\n" +
            "    &lt;/wsp:All&gt;\n" +
            "&lt;/wsp:Policy&gt;";

    private PolicyLoader policyLoader = new PolicyLoader();
    private Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();

    @Test
    void loadIncludePolicy() {
        final PolicyEntity entity = policyLoader.load(createPolicyBundleXml(document, INCLUDE.getType(), null));
        assertPolicyEntity(entity, INCLUDE, null);
    }

    @Test
    void loadServicePolicy() {
        final PolicyEntity entity = policyLoader.load(createPolicyBundleXml(document, SERVICE_OPERATION.getType(), null));
        assertPolicyEntity(entity, SERVICE_OPERATION, null);
    }

    @Test
    void loadGlobalPolicy() {
        final Map<String, Object> properties = ImmutableMap.of(PROPERTY_TAG, "global");
        final PolicyEntity entity = policyLoader.load(createPolicyBundleXml(document, GLOBAL.getType(), properties));
        assertPolicyEntity(entity, GLOBAL, "global");
    }

    @Test
    void loadAuditPolicy() {
        final Map<String, Object> properties = ImmutableMap.of(PROPERTY_TAG, "audit-sink");
        final PolicyEntity entity = policyLoader.load(createPolicyBundleXml(document, INTERNAL.getType(), properties));
        assertPolicyEntity(entity, INTERNAL, "audit-sink");
    }

    @Test
    void loadUnsupportedInternalPolicy() {
        final Map<String, Object> properties = ImmutableMap.of(PROPERTY_TAG, "other-internal-policy");
        final PolicyEntity entity = policyLoader.load(createPolicyBundleXml(document, INTERNAL.getType(), properties));
        assertNull(entity);
    }

    @Test
    void loadUnsupportedPolicyType() {
        final PolicyEntity entity = policyLoader.load(createPolicyBundleXml(document, "unsupported", null));
        assertNull(entity);
    }

    private static void assertPolicyEntity(PolicyEntity entity, PolicyType policyType, String tag) {
        assertNotNull(entity);
        assertEquals(TEST_POLICY_NAME, entity.getName());
        assertEquals(TEST_FOLDER_1, entity.getFolderId());
        assertEquals(TEST_POLICY_ID, entity.getId());
        assertEquals(TEST_GUID, entity.getGuid());
        assertEquals(policyType, entity.getPolicyType());
        assertEquals(tag, entity.getTag());
        assertNotNull(entity.getPolicyXML());
    }

    private static Element createPolicyBundleXml(Document document, String policyType, Map<String, Object> properties) {
        Element element = createElementWithAttributesAndChildren(
                document,
                POLICY,
                ImmutableMap.of(ATTRIBUTE_ID, TEST_POLICY_ID, ATTRIBUTE_GUID, TEST_GUID)
        );
        Element policyDetail = createElementWithAttributesAndChildren(
                document,
                POLICY_DETAIL,
                ImmutableMap.of(ATTRIBUTE_FOLDER_ID, TEST_FOLDER_1, ATTRIBUTE_GUID, TEST_GUID, ATTRIBUTE_ID, TEST_POLICY_ID),
                createElementWithTextContent(document, NAME, TEST_POLICY_NAME),
                createElementWithTextContent(document, POLICY_TYPE, policyType)
        );
        element.appendChild(policyDetail);

        if (properties != null) {
            buildAndAppendPropertiesElement(properties, document, policyDetail);
        }
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