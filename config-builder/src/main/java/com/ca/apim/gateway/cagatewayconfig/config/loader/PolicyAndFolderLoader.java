/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.util.JSONPObject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.createFolder;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.getPath;

@Singleton
public class PolicyAndFolderLoader implements EntityLoader {
    private static final Logger LOGGER = Logger.getLogger(PolicyAndFolderLoader.class.getName());
    private final PolicyConverterRegistry policyConverterRegistry;
    private final FileUtils fileUtils;
    private final IdGenerator idGenerator;
    private final EntityTypeRegistry entityTypeRegistry;
    @Inject
    PolicyAndFolderLoader(PolicyConverterRegistry policyConverterRegistry, FileUtils fileUtils, IdGenerator idGenerator, EntityTypeRegistry entityTypeRegistry) {
        this.policyConverterRegistry = policyConverterRegistry;
        this.fileUtils = fileUtils;
        this.idGenerator = idGenerator;
        this.entityTypeRegistry = entityTypeRegistry;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final File policyRootDir = FolderLoaderUtils.getPolicyRootDir(rootDir);
        if (policyRootDir == null) return;

        final Map<String, Policy> policies = new HashMap<>();
        final Map<Dependency, List<Dependency>> policyDependencyMap = new HashMap<>();
        loadPolicies(policyRootDir, policyRootDir, null, policies, bundle, policyDependencyMap);
        bundle.putAllPolicies(policies);
        bundle.setDependencyMap(policyDependencyMap);
    }

    @Override
    public void load(Bundle bundle, String name, String value) {
        throw new ConfigLoadException("Cannot load an individual policy");
    }

    @Override
    public Object loadSingle(String name, File entitiesFile) {
        throw new ConfigLoadException("Cannot load an individual policy");
    }

    private void loadPolicies(final File currentDir, final File rootDir, Folder parentFolder, final Map<String, Policy> policies, Bundle bundle, Map<Dependency, List<Dependency>> policyDependencyMap) {
        Folder folder = bundle.getFolders().computeIfAbsent(getPath(currentDir, rootDir), key -> createFolder(currentDir.getName(), key, parentFolder));
        final File[] children = currentDir.listFiles();
        if (children != null) {
            for (final File child : children) {
                if (child.isDirectory()) {
                    loadPolicies(child, rootDir, folder, policies, bundle, policyDependencyMap);
                } else if (policyConverterRegistry.isValidPolicyExtension(child.getName())) {
                    Policy policy = loadPolicy(child, rootDir, folder, bundle);
                    Policy existingPolicy = policies.put(policy.getPath(), policy);
                    if (existingPolicy != null) {
                        throw new ConfigLoadException("Found multiple policies with same path but different types. Policy Path: " + policy.getPath());
                    }

                } else if (child.getName().endsWith(".json")) {
                    loadPolicyDependencies(child, rootDir, folder, bundle, policyDependencyMap);

                }
            }
        }
    }

    private void loadPolicyDependencies(final File policyFile, final File rootDir, Folder parentFolder, Bundle bundle, Map<Dependency, List<Dependency>> policyDependencyMap) {
        final String policyDependencies = fileUtils.getFileAsString(policyFile);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Dependency> dependencies = new ArrayList<>();
        try {
            JsonNode policyNode = objectMapper.readTree(policyDependencies);
            TextNode policyId = (TextNode) policyNode.get("policyId");
            TextNode policyName = (TextNode) policyNode.get("policyName");
            ArrayNode jsonNodes = (ArrayNode) policyNode.get("uses");
            for(JsonNode jsonNode : jsonNodes){
                JsonNode id = jsonNode.get("id");
                JsonNode type = jsonNode.get("type");
                JsonNode name = jsonNode.get("name");
                dependencies.add(new Dependency(id.textValue(), entityTypeRegistry.getEntityClass(type.textValue()), name.textValue(), type.textValue()));
            }
            policyDependencyMap.put(new Dependency(policyId.textValue(), Policy.class, policyName.textValue(), EntityTypes.POLICY_TYPE), dependencies);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private Policy loadPolicy(final File policyFile, final File rootDir, Folder parentFolder, Bundle bundle) {
        PolicyConverter policyConverter = policyConverterRegistry.getConverterFromFileName(policyFile.getName());
        Policy policy = new Policy();
        policy.setPath(policyConverter.removeExtension(getPath(policyFile, rootDir)));
        policy.setName(policyConverter.removeExtension(policyFile.getName()));
        policy.setParentFolder(parentFolder);
        policy.setGuid(idGenerator.generateGuid());
        policy.setId(idGenerator.generate());

        policy.setPolicyXML(policyConverter.getPolicyXML(policy, fileUtils.getFileAsString(policyFile)));
        policy.postLoad(policy.getPath(), bundle, rootDir, this.idGenerator);
        return policy;
    }

    @Override
    public String getEntityType() {
        return "POLICY";
    }
}
