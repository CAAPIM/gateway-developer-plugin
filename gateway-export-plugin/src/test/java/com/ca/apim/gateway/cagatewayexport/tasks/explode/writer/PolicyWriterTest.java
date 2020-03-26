/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.AssertionJSPolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.XMLPolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableSet;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        writer.write(bundle, temporaryFolder.getRoot(), new Bundle());

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

        Bundle rawBundle = new Bundle();
        Map<Dependency, List<Dependency>> map = new HashMap<>();
        Dependency dep = new Dependency("serviceid", Service.class, "serviceName", EntityTypes.SERVICE_TYPE);
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency("identity", IdentityProvider.class, "identityName", EntityTypes.ID_PROVIDER_CONFIG_TYPE));
        dependencies.add(new Dependency("jdbc", JdbcConnection.class, "jdbcName", EntityTypes.JDBC_CONNECTION));
        map.put(dep, dependencies);
        List<Dependency> jdbcDependencies = new ArrayList<>();
        jdbcDependencies.add(new Dependency("password", StoredPassword.class, "passwordName", EntityTypes.STORED_PASSWORD_TYPE));
        map.put(new Dependency("jdbc", JdbcConnection.class, "jdbName", EntityTypes.JDBC_CONNECTION), jdbcDependencies);
        rawBundle.setDependencyMap(map);
        Map<String, Service> services  = bundle.getEntities(Service.class);
        Service service = new Service();
        service.setName("Test Service");
        service.setUrl("/Test");
        service.setId("serviceid");
        service.setPolicy(policy.getName());
        service.setPolicyXML(policy.getPolicyDocument());
        service.setParentFolder(ROOT_FOLDER);

        services.put("testService", service);

        writer.write(bundle, temporaryFolder.getRoot(), rawBundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File policyFile = new File(policyFolder, "assertionPolicy.assertion.js");
        assertTrue(policyFile.exists());

    }
}