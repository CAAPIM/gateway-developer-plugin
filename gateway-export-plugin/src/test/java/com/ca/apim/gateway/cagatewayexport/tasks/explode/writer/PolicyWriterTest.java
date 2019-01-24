/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.FolderTree;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.AssertionJSPolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.XMLPolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableSet;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TemporaryFolderExtension.class)
class PolicyWriterTest {
    private PolicyConverterRegistry policyConverterRegistry = new PolicyConverterRegistry(ImmutableSet.of(new AssertionJSPolicyConverter(), new XMLPolicyConverter(DocumentTools.INSTANCE)));

    @Test
    void testNoPolicies(final TemporaryFolder temporaryFolder) {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE);

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        writer.write(bundle, temporaryFolder.getRoot());

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        assertEquals(0, policyFolder.listFiles().length);
    }

    @Test
    void testWriteAssertionJS(final TemporaryFolder temporaryFolder) throws DocumentParseException {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE);

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Policy policy = new Policy();
        policy.setGuid("123");
        policy.setPath("assertionPolicy");
        policy.setParentFolder(ROOT_FOLDER);
        policy.setName("assertionPolicy");
        policy.setId("asd");
        policy.setPolicyXML("<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\"><L7p:JavaScript>\n" +
                "            <L7p:ExecutionTimeout stringValue=\"\"/>\n" +
                "            <L7p:Name stringValue=\"assertionPolicy\"/>\n" +
                "            <L7p:Script stringValueReference=\"inline\"><![CDATA[var js = {};]]></L7p:Script>\n" +
                "        </L7p:JavaScript></wsp:All>\n" +
                "</wsp:Policy>");
        policy.setPolicyDocument(DocumentTools.INSTANCE.parse(policy.getPolicyXML()).getDocumentElement());
        bundle.getPolicies().put("assertionPolicy", policy);

        writer.write(bundle, temporaryFolder.getRoot());

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File policyFile = new File(policyFolder, "assertionPolicy.assertion.js");
        assertTrue(policyFile.exists());

    }
}