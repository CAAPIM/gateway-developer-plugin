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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Singleton
public class PolicyWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;
    private PolicyConverterRegistry policyConverterRegistry;

    @Inject
    PolicyWriter(PolicyConverterRegistry policyConverterRegistry, DocumentFileUtils documentFileUtils) {
        this.policyConverterRegistry = policyConverterRegistry;
        this.documentFileUtils = documentFileUtils;
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
        Map<String, Service> services = bundle.getEntities(Service.class);
        services.values().parallelStream().forEach(serviceEntity -> writePolicy(bundle, policyFolder, serviceEntity.getParentFolder().getId(), serviceEntity.getName(), serviceEntity.getId(), serviceEntity.getPolicyXML(), rawBundle));

        Stream.of(
                bundle.getEntities(Policy.class).values().stream(),
                bundle.getEntities(GlobalPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream(),
                bundle.getEntities(AuditPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream()
        ).flatMap(s -> s)
                .forEach(policyEntity -> writePolicy(bundle, policyFolder, policyEntity.getParentFolder().getId(), policyEntity.getName(), policyEntity.getId(), policyEntity.getPolicyDocument(), rawBundle));
    }

    private void writePolicy(Bundle bundle, File policyFolder, String folderId, String name, String id, Element policy, Bundle rawBundle) {
        Folder folder = bundle.getFolderTree().getFolderById(folderId);
        Path folderPath = policyFolder.toPath().resolve(bundle.getFolderTree().getPath(folder));
        documentFileUtils.createFolders(folderPath);
        PolicyConverter policyConverter = policyConverterRegistry.getFromPolicyElement(name, policy);
        Path policyPath = folderPath.resolve(name + policyConverter.getPolicyTypeExtension());
        try (InputStream policyStream = policyConverter.convertFromPolicyElement(policy)) {
            FileUtils.copyInputStreamToFile(policyStream, policyPath.toFile());
        } catch (IOException e) {
            throw new WriteException("Unable to write assertion js policy", e);
        }

        List<Dependency> dependencyList = new ArrayList<>();
        Map<Dependency, List<Dependency>> dependencyListMap = rawBundle.getDependencyMap();
        if (dependencyListMap != null) {
            populateDependencies(dependencyListMap, id, dependencyList);
        }
        Path policyMetaPath = folderPath.resolve(name + ".json");
        if (!dependencyList.isEmpty()) {
            //build yml file with dependencies
            String data = createJsonData(id, name, dependencyList);
            try {
                FileUtils.writeStringToFile(policyMetaPath.toFile(), data, Charset.defaultCharset());
            } catch (IOException e) {
                throw new WriteException("Unable to write assertion js policy", e);
            }
        }

    }

    private String createJsonData(final String id, final String name, final List<Dependency> dependencyList) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        for (Dependency dependency : dependencyList) {
            ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("id", dependency.getId());
            objectNode.put("type", dependency.getEntityType());
            objectNode.put("name", dependency.getName());
            arrayNode.add(objectNode);
        }
        ObjectNode policyNode = mapper.createObjectNode();
        policyNode.set("policyId", TextNode.valueOf(id));
        policyNode.set("policyName", TextNode.valueOf(name));
        policyNode.set("uses", arrayNode);
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(policyNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void populateDependencies(Map<Dependency, List<Dependency>> dependencyListMap, String id, List<Dependency> dependencies) {
        Set<Map.Entry<Dependency, List<Dependency>>> entrySet = dependencyListMap.entrySet();

        for (Map.Entry<Dependency, List<Dependency>> e : entrySet) {
            Dependency entry = e.getKey();
            if (entry.getId().equals(id)) {
                List<Dependency> dependencyList = e.getValue();
                dependencies.addAll(dependencyList);
                for (Dependency dependency : dependencyList) {
                    populateDependencies(dependencyListMap, dependency.getId(), dependencies);
                }
            }
        }
    }
}
