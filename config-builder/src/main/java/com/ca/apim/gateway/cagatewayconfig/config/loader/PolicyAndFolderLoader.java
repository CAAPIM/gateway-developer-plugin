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
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.*;

import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.createFolder;
import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.getPath;

@Singleton
public class PolicyAndFolderLoader implements EntityLoader {

    private final PolicyConverterRegistry policyConverterRegistry;
    private final FileUtils fileUtils;
    private final IdGenerator idGenerator;
    private final JsonTools jsonTools;

    @Inject
    PolicyAndFolderLoader(PolicyConverterRegistry policyConverterRegistry, FileUtils fileUtils, IdGenerator idGenerator, JsonTools jsonTools) {
        this.policyConverterRegistry = policyConverterRegistry;
        this.fileUtils = fileUtils;
        this.idGenerator = idGenerator;
        this.jsonTools = jsonTools;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final File policyRootDir = FolderLoaderUtils.getPolicyRootDir(rootDir);
        if (policyRootDir == null) return;

        final Map<String, Policy> policies = new HashMap<>();
        loadPolicies(policyRootDir, policyRootDir, null, policies, bundle);
        loadPoliciesMetadata(policyRootDir, policies, bundle);
        bundle.putAllPolicies(policies);
    }

    private void loadPoliciesMetadata(final File policyRootDir, final Map<String, Policy> policies, final Bundle bundle){
        final Map<String, PolicyMetadata> policyMetadataMap = readPoliciesMetadata(policyRootDir);
        final Map<Dependency, List<Dependency>> policyDependencyMap = new HashMap<>();
        if (policyMetadataMap != null) {
            for(Map.Entry<String, PolicyMetadata> metadataEntry : policyMetadataMap.entrySet()){
                final String policyNameWithPath = metadataEntry.getKey();
                final PolicyMetadata policyMetadata = metadataEntry.getValue();
                final String policyName = PathUtils.extractName(policyNameWithPath);
                Set<Dependency> dependencies = policyMetadata.getUsedEntities();
                List<Dependency> dependencyList = dependencies != null ? new LinkedList<>(dependencies) : new LinkedList<>();
                policyDependencyMap.put(new Dependency(policyName, EntityTypes.POLICY_TYPE), dependencyList);
                Policy policy = policies.get(policyNameWithPath);
                policy.setAnnotations(policyMetadata.getAnnotations());
            }
        }
        bundle.setDependencyMap(policyDependencyMap);
    }

    private Map<String, PolicyMetadata> readPoliciesMetadata(final File policyRootDir){
        File policyMetadataFile = new File(policyRootDir, "policies" + jsonTools.getFileExtension());
        final ObjectMapper objectMapper = jsonTools.getObjectMapper();
        final MapType type = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, PolicyMetadata.class);
        return jsonTools.readDocumentFile(policyMetadataFile, type);
    }

    @Override
    public void load(Bundle bundle, String name, String value) {
        throw new ConfigLoadException("Cannot load an individual policy");
    }

    @Override
    public Object loadSingle(String name, File entitiesFile) {
        throw new ConfigLoadException("Cannot load an individual policy");
    }

    private void loadPolicies(final File currentDir, final File rootDir, Folder parentFolder, final Map<String, Policy> policies, Bundle bundle) {
        Folder folder = bundle.getFolders().computeIfAbsent(getPath(currentDir, rootDir), key -> createFolder(currentDir.getName(), key, parentFolder));
        final File[] children = currentDir.listFiles();
        if (children != null) {
            for (final File child : children) {
                if (child.isDirectory()) {
                    loadPolicies(child, rootDir, folder, policies, bundle);
                } else if (policyConverterRegistry.isValidPolicyExtension(child.getName())) {
                    Policy policy = loadPolicy(child, rootDir, folder, bundle);
                    Policy existingPolicy = policies.put(policy.getPath(), policy);
                    if (existingPolicy != null) {
                        throw new ConfigLoadException("Found multiple policies with same path but different types. Policy Path: " + policy.getPath());
                    }
                }
            }
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
