/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Element;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import static java.util.stream.Collectors.toList;
@Singleton
public class PolicyWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;
    private PolicyConverterRegistry policyConverterRegistry;
    private final JsonTools jsonTools;

    @Inject
    PolicyWriter(PolicyConverterRegistry policyConverterRegistry, DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        this.policyConverterRegistry = policyConverterRegistry;
        this.documentFileUtils = documentFileUtils;
        this.jsonTools = jsonTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder, Bundle rawBundle) {
        File policyFolder = new File(rootFolder, "policy");
        documentFileUtils.createFolder(policyFolder.toPath());

        //create folders
        bundle.getFolderTree().stream().forEach(folder -> {
            if (folder.getParentFolder() != null) {
                Path folderFile = policyFolder.toPath().resolve(bundle.getFolderTree().getPath(folder));
                documentFileUtils.createFolder(folderFile);
            }
        });

        //create policies
        Map<String, PolicyMetadata> policyMetadataMap = new HashMap<>();
        Map<String, Service> services = bundle.getEntities(Service.class);
        services.values().parallelStream().forEach(serviceEntity -> {
            final String id = serviceEntity.getId();
            final String name = serviceEntity.getName();
            final PolicyMetadata policyMetadata = writePolicy(bundle, policyFolder, serviceEntity.getParentFolder().getId(), name, id, serviceEntity.getPolicyXML());
            final Set<Dependency> policyDependencies = getPolicyDependencies(id, rawBundle);
            policyMetadata.setUsedEntities(policyDependencies);
            policyMetadataMap.put(policyMetadata.getNameWithPath(), policyMetadata);
        });

        Stream.of(
                bundle.getEntities(Policy.class).values().stream(),
                bundle.getEntities(GlobalPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream(),
                bundle.getEntities(AuditPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream()
        ).flatMap(s -> s)
                .forEach(policyEntity -> {
                    final String id = policyEntity.getId();
                    final String name = policyEntity.getName();
                    final PolicyMetadata policyMetadata = writePolicy(bundle, policyFolder, policyEntity.getParentFolder().getId(), name, id, policyEntity.getPolicyDocument());
                    final Set<Dependency> policyDependencies = getPolicyDependencies(id, rawBundle);
                    policyMetadata.setUsedEntities(policyDependencies);
                    final PolicyType policyType = policyEntity.getPolicyType();
                    if(policyType != null){
                        policyMetadata.setType(policyType.getType());
                    }
                    policyMetadata.setTag(policyMetadata.getTag());
                    policyMetadataMap.put(policyMetadata.getNameWithPath(), policyMetadata);
                });
        writePolicyMetadata(policyMetadataMap, policyFolder);
    }

    /**
     * Writes policy metadata to policy.yml file
     * @param policyMetadataMap
     * @param policyFolder
     */
    private void writePolicyMetadata(final Map<String, PolicyMetadata> policyMetadataMap, final File policyFolder) {
        if (!policyMetadataMap.isEmpty()) {
            //build yml file with dependencies
            File policyMetadataFile = new File(policyFolder, "policies" + jsonTools.getFileExtension());
            try {
                jsonTools.writeObject(policyMetadataMap, policyMetadataFile);
            } catch (IOException e) {
                throw new WriteException("Error writing policy metadata file", e);
            }
        }
    }

    private Set<Dependency> getPolicyDependencies(final String id, final Bundle rawBundle){
        final Set<Dependency> dependencyList = new HashSet<>();
        Map<Dependency, List<Dependency>> dependencyListMap = rawBundle.getDependencyMap();
        if (dependencyListMap != null) {
            populateDependencies(dependencyListMap, id, id, dependencyList);
        }
        return dependencyList;
    }

    private void populateDependencies(Map<Dependency, List<Dependency>> dependencyListMap, String rootId, String id, Set<Dependency> dependencies) {
        Set<Map.Entry<Dependency, List<Dependency>>> entrySet = dependencyListMap.entrySet();
        for (Map.Entry<Dependency, List<Dependency>> entry : entrySet) {
            Dependency parent = entry.getKey();
            if (parent.getId().equals(id)) {
                List<Dependency> dependencyList = entry.getValue();
                for (Dependency dependency : dependencyList) {
                    if (!rootId.equals(dependency.getId()) && dependencies.add(dependency)) {
                        populateDependencies(dependencyListMap, rootId, dependency.getId(), dependencies);
                    }
                }
            }
        }
    }

    private PolicyMetadata writePolicy(Bundle bundle, File policyFolder, String folderId, String name, String id, Element policy) {
        final  PolicyMetadata policyMetadata = new PolicyMetadata();
        Folder folder = bundle.getFolderTree().getFolderById(folderId);
        Path policyFolderPath = bundle.getFolderTree().getPath(folder);
        Path folderPath = policyFolder.toPath().resolve(policyFolderPath);
        documentFileUtils.createFolders(folderPath);
        PolicyConverter policyConverter = policyConverterRegistry.getFromPolicyElement(name, policy);
        Path policyPath = folderPath.resolve(name + policyConverter.getPolicyTypeExtension());
        policyMetadata.setPath(PathUtils.unixPath(policyFolderPath));
        policyMetadata.setName(name);
        try (InputStream policyStream = policyConverter.convertFromPolicyElement(policy)) {
            FileUtils.copyInputStreamToFile(policyStream, policyPath.toFile());
        } catch (IOException e) {
            throw new WriteException("Unable to write assertion js policy", e);
        }
        return policyMetadata;
    }
}
