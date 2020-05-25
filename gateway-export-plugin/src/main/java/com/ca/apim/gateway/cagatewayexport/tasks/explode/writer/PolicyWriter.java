/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderException;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverter;
import com.ca.apim.gateway.cagatewayconfig.config.loader.policy.PolicyConverterRegistry;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntitiesLinker;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.linker.EntityLinkerRegistry;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.ENCAPSULATED;
import static java.util.stream.Collectors.toList;

@Singleton
public class PolicyWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;
    private final JsonFileUtils jsonFileUtils;
    private PolicyConverterRegistry policyConverterRegistry;
    private final EntityLinkerRegistry entityLinkerRegistry;
    static final String ENCASS_NAME = "encassName";

    @Inject
    PolicyWriter(PolicyConverterRegistry policyConverterRegistry, DocumentFileUtils documentFileUtils,
                 JsonFileUtils jsonFileUtils, EntityLinkerRegistry entityLinkerRegistry) {
        this.policyConverterRegistry = policyConverterRegistry;
        this.documentFileUtils = documentFileUtils;
        this.jsonFileUtils = jsonFileUtils;
        this.entityLinkerRegistry = entityLinkerRegistry;
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
            final PolicyMetadata policyMetadata = createPolicyMetadata(bundle, rawBundle, null, serviceEntity, serviceEntity.getPolicyXML());
            policyMetadataMap.put(policyMetadata.getFullPath(), policyMetadata);
        });

        Stream.of(
                bundle.getEntities(Policy.class).values().stream(),
                bundle.getEntities(GlobalPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream(),
                bundle.getEntities(AuditPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream()
        ).flatMap(s -> s)
                .forEach(policyEntity -> {
                    writePolicy(bundle, policyFolder, policyEntity, policyEntity.getPolicyDocument());
                    final PolicyMetadata policyMetadata = createPolicyMetadata(bundle, rawBundle, policyEntity, policyEntity, policyEntity.getPolicyDocument());
                    policyMetadataMap.put(policyMetadata.getFullPath(), policyMetadata);
                });
        writePolicyMetadata(policyMetadataMap, rootFolder);
    }

    private PolicyMetadata createPolicyMetadata(final Bundle bundle, final Bundle rawBundle, final Policy policyEntity, final Folderable folderableEntity, final Element policy) {
        final PolicyMetadata policyMetadata = new PolicyMetadata();
        final Folder folder = bundle.getFolderTree().getFolderById(folderableEntity.getParentFolderId());
        final Path policyPath = bundle.getFolderTree().getPath(folder);

        policyMetadata.setPath(PathUtils.unixPath(policyPath));
        policyMetadata.setName(folderableEntity.getName());

        if (policyEntity != null) {
            final PolicyType policyType = policyEntity.getPolicyType();
            if (policyType != null) {
                policyMetadata.setType(policyType.getType());
            }
            policyMetadata.setTag(policyEntity.getTag());
            policyMetadata.setSubtag(policyEntity.getSubtag());
        }
        Set<Dependency> dependencies = getPolicyDependencies(folderableEntity.getId(), rawBundle, policy);
        final Collection<EntitiesLinker> entityLinkers = entityLinkerRegistry.getEntityLinkers();
        entityLinkers.forEach(e -> {
            if (e != null) {
                e.link(dependencies);
            }
        });
        policyMetadata.setUsedEntities(dependencies);
        return policyMetadata;
    }

    /**
     * Writes policy metadata including their dependencies to policy.yml file
     * @param policyMetadataMap Policy metadata to write
     * @param rootDir Directory where to write
     */
    private void writePolicyMetadata(final Map<String, PolicyMetadata> policyMetadataMap, final File rootDir) {
        if (!policyMetadataMap.isEmpty()) {
            jsonFileUtils.writePoliciesConfigFile(policyMetadataMap, rootDir);
        }
    }

    private Set<Dependency> getPolicyDependencies(final String id, final Bundle rawBundle, final Element policy) {
        Map<Dependency, List<Dependency>> dependencyListMap = rawBundle.getDependencyMap();
        final Set<Dependency> dependencies = new HashSet<>();
        if (dependencyListMap != null) {
            Set<Map.Entry<Dependency, List<Dependency>>> entrySet = dependencyListMap.entrySet();
            for (Map.Entry<Dependency, List<Dependency>> entry : entrySet) {
                Dependency parent = entry.getKey();
                if (parent.getId().equals(id)) { // Search for "id" to get its dependencies
                    List<Dependency> policyDependencies = entry.getValue();
                    if (policyDependencies != null) {
                        Set<String> dependentEncassSet = getDependentEncapsulatedEntities(policy);
                        for (Dependency dependency : policyDependencies) {
                            if (!EntityTypes.ENCAPSULATED_ASSERTION_TYPE.equals(dependency.getType())) {
                                dependencies.add(dependency);
                            } else if (dependentEncassSet.contains(dependency.getName())) {
                                dependencies.add(dependency);
                            }
                        }
                    }
                }
            }
        }
        return dependencies;
    }

    private Set<String> getDependentEncapsulatedEntities(final Element policyElement) {
        Set<String> encassEntities = new HashSet<>();
        NodeList assertionReferences = policyElement.getElementsByTagName(ENCAPSULATED);

        for (int index = 0; index < assertionReferences.getLength(); index++) {
            Node assertionElement = assertionReferences.item(index);
            if (assertionElement instanceof Element) {
                Element encassElement = (Element) assertionElement;
                if (encassElement.hasAttribute(ENCASS_NAME)) {
                    encassEntities.add(encassElement.getAttribute(ENCASS_NAME));
                }
            }
        }
        return encassEntities;
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
