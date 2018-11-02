/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalPolicyWriterTest {

    private GlobalPolicyWriter writer = new GlobalPolicyWriter(DocumentFileUtils.INSTANCE, JsonTools.INSTANCE);
    private static final String TEST_POLICY_ID = "PolicyID";
    private static final String TEST_POLICY_NAME = "Policy";
    private static final String TEST_FOLDER_1 = "Folder1";

    @Test
    void testWithGlobalPolicies() {
        Bundle bundle = new Bundle();
        Policy globalPolicy = new Policy.Builder()
                .setName(TEST_POLICY_NAME)
                .setId(TEST_POLICY_ID)
                .setParentFolderId(TEST_FOLDER_1)
                .setTag("global-policy")
                .setPolicyType(PolicyType.GLOBAL)
                .build();
        bundle.addEntity(globalPolicy);
        bundle.addEntity(new Policy.Builder()
                .setName(TEST_POLICY_NAME + "_1")
                .setId(TEST_POLICY_ID + "_1")
                .setParentFolderId(TEST_FOLDER_1 + "_1")
                .setPolicyType(PolicyType.INTERNAL)
                .build());

        final Map<String, Policy> globalPolicies = writer.filterPolicies(bundle);

        assertNotNull(globalPolicies);
        assertFalse(globalPolicies.isEmpty());
        assertEquals(1, globalPolicies.size());
        assertEquals(globalPolicy, globalPolicies.get(TEST_POLICY_NAME));
    }

    @Test
    void testNoGlobalPolicies() {
        Bundle bundle = new Bundle();
        bundle.addEntity(new Policy.Builder()
                .setName(TEST_POLICY_NAME + "_1")
                .setId(TEST_POLICY_ID + "_1")
                .setParentFolderId(TEST_FOLDER_1 + "_1")
                .setPolicyType(PolicyType.SERVICE_OPERATION)
                .build());

        final Map<String, Policy> globalPolicies = writer.filterPolicies(bundle);

        assertNotNull(globalPolicies);
        assertTrue(globalPolicies.isEmpty());
    }

    @Test
    void testGetGlobalPolicyBean() {
        String path = Paths.get(TEST_FOLDER_1, TEST_POLICY_NAME + ".xml").toString();
        Policy globalPolicy = new Policy.Builder()
                .setName(TEST_POLICY_NAME)
                .setId(TEST_POLICY_ID)
                .setParentFolderId(TEST_FOLDER_1)
                .setTag("global-policy")
                .setPolicyType(PolicyType.INTERNAL)
                .build();
        globalPolicy.setPath(path);
        final Policy policyBean = writer.getPolicyBean(globalPolicy);

        assertNotNull(policyBean);
        assertEquals("global-policy", policyBean.getTag());
        assertEquals(path, policyBean.getPath());
    }
}