/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            final PolicyMetadata policyMetadata = createPolicyMetadata(bundle, rawBundle, null, serviceEntity);
            policyMetadataMap.put(policyMetadata.getFullPath(), policyMetadata);
        });

        Stream.of(
                bundle.getEntities(Policy.class).values().stream(),
                bundle.getEntities(GlobalPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream(),
                bundle.getEntities(AuditPolicy.class).values().stream().map(Policy.class::cast).collect(toList()).stream()
        ).flatMap(s -> s)
                .forEach(policyEntity -> {
                    writePolicy(bundle, policyFolder, policyEntity, policyEntity.getPolicyDocument());
                    final PolicyMetadata policyMetadata = createPolicyMetadata(bundle, rawBundle, policyEntity, policyEntity);
                    policyMetadataMap.put(policyMetadata.getFullPath(), policyMetadata);
                });
        writePolicyMetadata(policyMetadataMap, rootFolder);
    }

    private PolicyMetadata createPolicyMetadata(final Bundle bundle, final Bundle rawBundle, final Policy policyEntity, final Folderable folderableEntity) {
        final PolicyMetadata policyMetadata = new PolicyMetadata();
        final Folder folder = bundle.getFolderTree().getFolderById(folderableEntity.getParentFolderId());
        final Path policyPath = bundle.getFolderTree().getPath(folder);
        final String policyName = folderableEntity.getName();
        policyMetadata.setPath(PathUtils.unixPath(policyPath));
        policyMetadata.setName(policyName);

        if (policyEntity != null) {
            final PolicyType policyType = policyEntity.getPolicyType();
            if (policyType != null) {
                policyMetadata.setType(policyType.getType());
            }
            policyMetadata.setTag(policyEntity.getTag());
            policyMetadata.setSubtag(policyEntity.getSubtag());
            policyMetadata.setHasRouting(policyEntity.isHasRouting());
        }
        Set<Dependency> filteredDependencies = getFilteredPolicyDependencies(policyName, getPolicyDependencies(folderableEntity.getId(), rawBundle), rawBundle.getEncasses());

        final Collection<EntitiesLinker> entityLinkers = entityLinkerRegistry.getEntityLinkers();
        entityLinkers.forEach(e -> {
            if (e != null) {
                e.link(filteredDependencies);
            }
        });
        policyMetadata.setUsedEntities(filteredDependencies);
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

    /**
     * This method filters out encasses that refers the same policy (recursive dependency)
     * @param policyName         String
     * @param policyDependencies Set
     * @param encassMap          Map
     * @return Set
     */
    private Set<Dependency> getFilteredPolicyDependencies(final String policyName, final Set<Dependency> policyDependencies, final Map<String, Encass> encassMap) {
        return policyDependencies.stream().filter(dependency -> {
            if (EntityTypes.ENCAPSULATED_ASSERTION_TYPE.equals(dependency.getType())) {
                Encass dependentEncass = encassMap.get(dependency.getName());
                if (dependentEncass != null && policyName.equals(dependentEncass.getPolicy())) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toSet());
    }

    /**
     * This method finds policy dependencies from the bundle dependency graphf for a given policy id
     * @param id String
     * @param rawBundle Bundle
     * @return Set
     */
    private Set<Dependency> getPolicyDependencies(final String id, final Bundle rawBundle) {
        Map<Dependency, List<Dependency>> dependencyListMap = rawBundle.getDependencyMap();
        if (dependencyListMap != null) {
            Set<Map.Entry<Dependency, List<Dependency>>> entrySet = dependencyListMap.entrySet();
            for (Map.Entry<Dependency, List<Dependency>> entry : entrySet) {
                Dependency parent = entry.getKey();
                if (parent.getId().equals(id)) { // Search for "id" to get its dependencies
                    return new HashSet<>(entry.getValue());
                }
            }
        }
        return Collections.emptySet();
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
