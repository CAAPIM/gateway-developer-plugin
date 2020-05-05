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
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
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
    private final JsonFileUtils jsonFileUtils;

    @Inject
    PolicyAndFolderLoader(PolicyConverterRegistry policyConverterRegistry, FileUtils fileUtils,
                          IdGenerator idGenerator, JsonFileUtils jsonFileUtils) {
        this.policyConverterRegistry = policyConverterRegistry;
        this.fileUtils = fileUtils;
        this.idGenerator = idGenerator;
        this.jsonFileUtils = jsonFileUtils;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final File policyRootDir = FolderLoaderUtils.getPolicyRootDir(rootDir);
        if (policyRootDir == null) return;

        final Map<String, Policy> policies = new HashMap<>();
        loadPolicies(policyRootDir, policyRootDir, null, policies, bundle);
        loadPoliciesMetadata(rootDir, policies, bundle);
        bundle.putAllPolicies(policies);
    }

    private void loadPoliciesMetadata(final File rootDir, final Map<String, Policy> policies, final Bundle bundle) {
        final Map<String, PolicyMetadata> policyMetadataMap = jsonFileUtils.readPoliciesConfigFile(rootDir, PolicyMetadata.class);
        final Map<Dependency, List<Dependency>> policyDependencyMap = new HashMap<>();

        if (policyMetadataMap != null) {
            policyMetadataMap.forEach((fullPath, policyMetadata) -> {
                policyMetadata.setFullPath(fullPath);

                final Set<Dependency> dependencies = Optional.ofNullable(policyMetadata.getUsedEntities())
                        .orElse(Collections.emptySet());
                policyDependencyMap.put(new Dependency(policyMetadata.getName(), EntityTypes.POLICY_TYPE),
                        new LinkedList<>(dependencies));

                Policy policy = policies.get(policyMetadata.getFullPath());
                policy.setAnnotations(policyMetadata.getAnnotations());
                policy.setPolicyMetadata(policyMetadata);
            });
        }
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
