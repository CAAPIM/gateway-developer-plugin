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
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntityLinkerRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.collect.ImmutableSet;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.w3c.dom.Document;

import static com.ca.apim.gateway.cagatewayconfig.beans.Folder.ROOT_FOLDER;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TemporaryFolderExtension.class)
class PolicyWriterTest {
    private PolicyConverterRegistry policyConverterRegistry = new PolicyConverterRegistry(ImmutableSet.of(new AssertionJSPolicyConverter(), new XMLPolicyConverter(DocumentTools.INSTANCE)));

    @Test
    void testNoPolicies(final TemporaryFolder temporaryFolder) {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        assertEquals(0, policyFolder.listFiles().length);
    }

    @Test
    void testWriteAssertionJS(final TemporaryFolder temporaryFolder) throws DocumentParseException {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

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

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File policyFile = new File(policyFolder, "assertionPolicy.assertion.js");
        assertTrue(policyFile.exists());

        File configFolder = new File(temporaryFolder.getRoot(), "config");
        assertTrue(configFolder.exists());
        File policyMetadataFile = new File(configFolder, "policies.yml");
        assertTrue(policyMetadataFile.exists());
    }

    @Test
    void testWritePolicyWithSubfolder(final TemporaryFolder temporaryFolder) throws DocumentParseException {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        Folder folder = new Folder("0000000000000000ffffffffffff54", "Test");
        folder.setParentFolder(ROOT_FOLDER);
        bundle.addEntity(folder);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Policy policy = new Policy();
        policy.setGuid("123");
        policy.setPath("assertionPolicy");
        policy.setParentFolder(folder);
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

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File testFolder = new File(policyFolder, "Test");
        assertTrue(testFolder.exists());

        File policyFile = new File(testFolder, "assertionPolicy.assertion.js");
        assertTrue(policyFile.exists());

        File configFolder = new File(temporaryFolder.getRoot(), "config");
        assertTrue(configFolder.exists());

        File policyMetadataFile = new File(configFolder, "policies.yml");
        assertTrue(policyMetadataFile.exists());
    }

    @Test
    void testWriteServicePolicy(final TemporaryFolder temporaryFolder) throws DocumentParseException {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Service service = new Service();
        service.setPath("assertionPolicy");
        service.setParentFolder(ROOT_FOLDER);
        service.setName("assertionPolicy");
        service.setId("asd");
        DocumentTools documentTools = DocumentTools.INSTANCE;
        Document document = documentTools.parse("<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\">\n" +
                "    <wsp:All wsp:Usage=\"Required\"><L7p:JavaScript>\n" +
                "            <L7p:ExecutionTimeout stringValue=\"\"/>\n" +
                "            <L7p:Name stringValue=\"assertionPolicy\"/>\n" +
                "            <L7p:Script stringValueReference=\"inline\"><![CDATA[var js = {};]]></L7p:Script>\n" +
                "        </L7p:JavaScript></wsp:All>\n" +
                "</wsp:Policy>");
        service.setPolicyXML(document.getDocumentElement());
        bundle.getServices().put("assertionPolicy", service);

        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File policyFile = new File(policyFolder, "assertionPolicy.assertion.js");
        assertTrue(policyFile.exists());

        File configFolder = new File(temporaryFolder.getRoot(), "config");
        assertTrue(configFolder.exists());

        File policyMetadataFile = new File(configFolder, "policies.yml");
        assertTrue(policyMetadataFile.exists());
    }

    @Test
    void testWritePolicyWithDependencies(final TemporaryFolder temporaryFolder) throws DocumentParseException {
        PolicyWriter writer = new PolicyWriter(policyConverterRegistry, DocumentFileUtils.INSTANCE, JsonFileUtils.INSTANCE, new EntityLinkerRegistry(new HashSet<>()));

        Bundle bundle = new Bundle();
        bundle.addEntity(ROOT_FOLDER);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        Policy policy = new Policy();
        policy.setGuid("123");
        policy.setPath("assertionPolicy");
        policy.setParentFolder(ROOT_FOLDER);
        policy.setName("assertionPolicy");
        policy.setId("asd");
        policy.setPolicyXML("<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2002/12/policy\" xmlns:L7p=\"http://www.layer7tech.com/ws/policy\">" +
                "    <wsp:All wsp:Usage=\"Required\"><L7p:JavaScript>" +
                "            <L7p:ExecutionTimeout stringValue=\"\"/>" +
                "            <L7p:Name stringValue=\"assertionPolicy\"/>" +
                "            <L7p:Script stringValueReference=\"inline\"><![CDATA[var js = {};]]></L7p:Script>" +
                "        </L7p:JavaScript>" +
                "        <L7p:Encapsulated encassName=\"encassDep\"/>" +
                "</wsp:All>" +
                "</wsp:Policy>");
        policy.setPolicyDocument(DocumentTools.INSTANCE.parse(policy.getPolicyXML()).getDocumentElement());
        bundle.getPolicies().put("assertionPolicy", policy);

        JdbcConnection.Builder builder = new JdbcConnection.Builder();
        builder.id("jdbcid");
        builder.name("testjdbc");
        builder.driverClass("testDriver");
        builder.jdbcUrl("jdbc:localhost:3306");
        JdbcConnection jdbcConnection = builder.build();
        bundle.addEntity(jdbcConnection);
        Map<Dependency, List<Dependency>> dependencyListMap = new HashMap<>();
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency("jdbcid", JdbcConnection.class, "testjdbc", EntityTypes.JDBC_CONNECTION));
        Dependency encassDependency = new Dependency(null, null, "encassName", EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
        dependencies.add(encassDependency);
        Dependency encassDep = new Dependency(null, null, "encassDep", EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
        dependencies.add(encassDep);
        dependencyListMap.put(new Dependency("asd", Policy.class, "assertionPolicy", EntityTypes.POLICY_TYPE), dependencies);
        bundle.setDependencyMap(dependencyListMap);
        writer.write(bundle, temporaryFolder.getRoot(), bundle);

        File policyFolder = new File(temporaryFolder.getRoot(), "policy");
        assertTrue(policyFolder.exists());

        File configFolder = new File(temporaryFolder.getRoot(), "config");
        assertTrue(configFolder.exists());

        File policyMetadataFile = new File(configFolder, "policies.yml");
        assertTrue(policyMetadataFile.exists());
        Map<String, PolicyMetadata> policyMetadataMap = getPolicyMetadata(policyMetadataFile);
        PolicyMetadata policyMetadata = policyMetadataMap.get("assertionPolicy");
        Set<Dependency> usedEntities = policyMetadata.getUsedEntities();
        assertFalse(usedEntities.contains(encassDependency));
        assertTrue(usedEntities.contains(encassDep));
    }

    private Map<String, PolicyMetadata> getPolicyMetadata(File policyMetadataFile) {
        Map<String, PolicyMetadata> policyMetadataMap = null;
        JsonTools jsonTools = JsonTools.INSTANCE;
        final ObjectMapper objectMapper = jsonTools.getObjectMapper();
        final MapType type = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, PolicyMetadata.class);
        try {
            policyMetadataMap = objectMapper.readValue(policyMetadataFile, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return policyMetadataMap;
    }
}