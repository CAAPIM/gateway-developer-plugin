/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.Entity;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import static java.lang.String.format;
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
            final PolicyMetadata policyMetadata = writePolicy(bundle, policyFolder, serviceEntity.getParentFolder().getId(), name, serviceEntity.getId(), serviceEntity.getPolicyXML());
            final Set<Dependency> policyDependencies = getPolicyDependencies(id, rawBundle);
            policyMetadata.setDependencies(policyDependencies);
            policyMetadataMap.put(name, policyMetadata);
        });

        Stream.of(
                bundle.getEntities(Policy.class).values().stream(),
                bundle.getEntities(GlobalPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream(),
                bundle.getEntities(AuditPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream()
        ).flatMap(s -> s)
                .forEach(policyEntity -> {
                    final String id = policyEntity.getId();
                    final String name = policyEntity.getName();
                    final PolicyMetadata policyMetadata = writePolicy(bundle, policyFolder, policyEntity.getParentFolder().getId(), policyEntity.getName(), id, policyEntity.getPolicyDocument());
                    final Set<Dependency> policyDependencies = getPolicyDependencies(id, rawBundle);
                    policyMetadata.setDependencies(policyDependencies);
                    policyMetadataMap.put(name, policyMetadata);
                });
        writePolicyMetadata(policyMetadataMap, policyFolder);
    }

    private void writePolicyMetadata(final Map<String, PolicyMetadata> policyMetadataMap, final File policyFolder){
        if (!policyMetadataMap.isEmpty()) {
            //build yml file with dependencies
            ObjectWriter objectWriter = jsonTools.getObjectWriter();

            // check if a current file exists and merge contents
            File policyMetadataFile = new File(policyFolder, "policy" + jsonTools.getFileExtension());
            // last write the merged map of beans to the config file
            try (OutputStream fileStream = Files.newOutputStream(policyMetadataFile.toPath())) {
                objectWriter.writeValue(fileStream, policyMetadataMap);
            } catch (IOException e) {
                throw new WriteException("Error writing policy metadata", e);
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
        for (Map.Entry<Dependency, List<Dependency>> e : entrySet) {
            Dependency entry = e.getKey();
            if (entry.getId().equals(id)) {
                List<Dependency> dependencyList = e.getValue();
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
        policyMetadata.setPath(policyFolderPath.toString());
        try (InputStream policyStream = policyConverter.convertFromPolicyElement(policy)) {
            FileUtils.copyInputStreamToFile(policyStream, policyPath.toFile());
        } catch (IOException e) {
            throw new WriteException("Unable to write assertion js policy", e);
        }
        return policyMetadata;
    }
}
