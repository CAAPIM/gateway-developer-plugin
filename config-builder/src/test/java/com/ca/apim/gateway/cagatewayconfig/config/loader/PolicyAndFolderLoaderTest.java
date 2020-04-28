/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.AssertionJSPolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterException;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.XMLPolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableSet;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class PolicyAndFolderLoaderTest {

    @Mock
    private FileUtils fileUtils;
    @Mock
    private EntityTypeRegistry entityTypeRegistry;

    private PolicyConverterRegistry policyConverterRegistry = new PolicyConverterRegistry(ImmutableSet.of(new AssertionJSPolicyConverter(), new XMLPolicyConverter(DocumentTools.INSTANCE)));

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoad(TemporaryFolder temporaryFolder) throws IOException {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(policyConverterRegistry, fileUtils, new IdGenerator(), entityTypeRegistry, JsonTools.INSTANCE);
        Mockito.when(fileUtils.getFileAsString(Mockito.any(File.class))).thenReturn("policy-content");

        File policyFolder = temporaryFolder.createDirectory("policy");
        File a = new File(policyFolder, "a");
        Assert.assertTrue(a.mkdir());
        File b = new File(a, "b");
        Assert.assertTrue(b.mkdir());
        File c = new File(b, "c");
        Assert.assertTrue(c.mkdir());
        File policy = new File(c, "policy.xml");
        Files.touch(policy);
        File policyDep = new File(policyFolder, "policies.yml");
        Map<String, PolicyMetadata> policyMetadataMap = new HashMap<>();
        PolicyMetadata policyMetadata = new PolicyMetadata();
        policyMetadata.setTag("tag");
        policyMetadata.setType("inclulde");
        Set<Dependency> dependencySet = new HashSet<>();
        dependencySet.add(new Dependency("jdbc", EntityTypes.JDBC_CONNECTION));
        policyMetadata.setUsedEntities(dependencySet);
        policyMetadataMap.put("a/b/c/policy", policyMetadata);
        JsonTools.INSTANCE.writeObject(policyMetadataMap, policyDep);

        Bundle bundle = new Bundle();
        policyAndFolderLoader.load(bundle, temporaryFolder.getRoot());

        Assert.assertNotNull(bundle.getFolders().get(""));
        Assert.assertNotNull(bundle.getFolders().get("a/"));
        Assert.assertNotNull(bundle.getFolders().get("a/b/"));
        Folder parentFolder = bundle.getFolders().get("a/b/c/");
        Assert.assertNotNull(parentFolder);

        Policy loadedPolicy = bundle.getPolicies().get("a/b/c/policy");
        Assert.assertNotNull(loadedPolicy);
        Assert.assertEquals(parentFolder, loadedPolicy.getParentFolder());

        Map<Dependency, List<Dependency>> dependencyListMap = bundle.getDependencyMap();
        Assert.assertNotNull(dependencyListMap);
        List<Dependency> dependencyList = dependencyListMap.get(new Dependency("policy", EntityTypes.POLICY_TYPE));
        Assert.assertNotNull(dependencyList);
        Assert.assertEquals(dependencyList.size(), 1);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadSingle(TemporaryFolder temporaryFolder) {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(policyConverterRegistry, FileUtils.INSTANCE, new IdGenerator(), entityTypeRegistry, JsonTools.INSTANCE);
        assertThrows(ConfigLoadException.class, () -> policyAndFolderLoader.loadSingle("Policy", temporaryFolder.getRoot()));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadNoPolicyFolder(TemporaryFolder temporaryFolder) {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(policyConverterRegistry, FileUtils.INSTANCE, new IdGenerator(), entityTypeRegistry, JsonTools.INSTANCE);

        Bundle bundle = new Bundle();
        policyAndFolderLoader.load(bundle, temporaryFolder.getRoot());

        Assert.assertTrue(bundle.getFolders().isEmpty());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadPolicyFolderIsFile(TemporaryFolder temporaryFolder) throws IOException {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(policyConverterRegistry, FileUtils.INSTANCE, new IdGenerator(), entityTypeRegistry, JsonTools.INSTANCE);

        temporaryFolder.createFile("policy");

        Bundle bundle = new Bundle();

        assertThrows(ConfigLoadException.class, () -> policyAndFolderLoader.load(bundle, temporaryFolder.getRoot()));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadPolicyUnknownType(TemporaryFolder temporaryFolder) throws IOException {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(policyConverterRegistry, FileUtils.INSTANCE, new IdGenerator(), entityTypeRegistry, JsonTools.INSTANCE);

        File policyFolder = temporaryFolder.createDirectory("policy");
        File policy = new File(policyFolder, "policy.blah");
        Files.touch(policy);

        Bundle bundle = new Bundle();
        policyAndFolderLoader.load(bundle, temporaryFolder.getRoot());

        Policy loadedPolicy = bundle.getPolicies().get("policy");
        Assert.assertNull(loadedPolicy);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testLoadPolicySameNameDifferentType(TemporaryFolder temporaryFolder) throws IOException {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(policyConverterRegistry, FileUtils.INSTANCE, new IdGenerator(), entityTypeRegistry, JsonTools.INSTANCE);

        File policyFolder = temporaryFolder.createDirectory("policy");
        File policy = new File(policyFolder, "policy.xml");
        Files.touch(policy);
        policy = new File(policyFolder, "policy.assertion.js");
        Files.touch(policy);

        Bundle bundle = new Bundle();
        assertThrows(ConfigLoadException.class, () -> policyAndFolderLoader.load(bundle, temporaryFolder.getRoot()));
    }

    @Test
    void loadFromEnvironment() {
        PolicyAndFolderLoader policyAndFolderLoader = new PolicyAndFolderLoader(policyConverterRegistry, FileUtils.INSTANCE, new IdGenerator(), entityTypeRegistry, JsonTools.INSTANCE);
        assertThrows(ConfigLoadException.class, () -> policyAndFolderLoader.load(new Bundle(), "policy", "policy"));
    }
}