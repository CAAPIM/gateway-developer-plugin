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
            writePolicy(bundle, policyFolder, serviceEntity, serviceEntity.getPolicyXML());
            final PolicyMetadata policyMetadata = createPolicyMetadata(bundle, rawBundle, null, serviceEntity);
            policyMetadataMap.put(policyMetadata.getPath(), policyMetadata);
        });

        Stream.of(
                bundle.getEntities(Policy.class).values().stream(),
                bundle.getEntities(GlobalPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream(),
                bundle.getEntities(AuditPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream()
        ).flatMap(s -> s)
                .forEach(policyEntity -> {
                    writePolicy(bundle, policyFolder, policyEntity, policyEntity.getPolicyDocument());
                    final PolicyMetadata policyMetadata = createPolicyMetadata(bundle, rawBundle, policyEntity, policyEntity);
                    policyMetadataMap.put(policyMetadata.getPath(), policyMetadata);
                });
        writePolicyMetadata(policyMetadataMap, policyFolder);
    }

    private PolicyMetadata createPolicyMetadata(final Bundle bundle, final Bundle rawBundle, final Policy policyEntity, final Folderable folderableEntity) {
        final PolicyMetadata policyMetadata = new PolicyMetadata();
        final Folder folder = bundle.getFolderTree().getFolderById(folderableEntity.getParentFolderId());
        final Path policyPath = bundle.getFolderTree().getPath(folder);
        final String path = PathUtils.unixPathEndingWithSeparator(policyPath);
        policyMetadata.setPath(path.length() > 1 ? path + folderableEntity.getName() : folderableEntity.getName());
        final Set<Dependency> policyDependencies = getPolicyDependencies(folderableEntity.getId(), rawBundle);
        policyMetadata.setUsedEntities(policyDependencies);
        if (policyEntity != null) {
            final PolicyType policyType = policyEntity.getPolicyType();
            if (policyType != null) {
                policyMetadata.setType(policyType.getType());
            }
            policyMetadata.setTag(policyEntity.getTag());
            policyMetadata.setSubtag(policyEntity.getSubtag());
        }
        return policyMetadata;
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

    private void writePolicy(Bundle bundle, File policyFolder, Folderable folderableEntity, Element policy) {
        final Folder folder = bundle.getFolderTree().getFolderById(folderableEntity.getParentFolderId());
        Path folderPath = policyFolder.toPath().resolve(bundle.getFolderTree().getPath(folder));
        documentFileUtils.createFolders(folderPath);
        PolicyConverter policyConverter = policyConverterRegistry.getFromPolicyElement(folderableEntity.getName(), policy);
        Path policyPath = folderPath.resolve(folderableEntity.getName() + policyConverter.getPolicyTypeExtension());
        try (InputStream policyStream = policyConverter.convertFromPolicyElement(policy)) {
            FileUtils.copyInputStreamToFile(policyStream, policyPath.toFile());
        } catch (IOException e) {
            throw new WriteException("Unable to write assertion js policy", e);
        }
    }
}
