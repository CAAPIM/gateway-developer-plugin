/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.string.CharacterBlacklistUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.FOLDER_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_EXISTING;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

@Singleton
public class FolderEntityBuilder implements EntityBuilder {
    private static final Logger LOGGER = Logger.getLogger(FolderEntityBuilder.class.getName());
    private static final Integer ORDER = 100;
    private final IdGenerator idGenerator;

    @Inject
    FolderEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    private Entity buildFolderEntity(Folder folder, String id, String parentFolderId, Document document) {
        Element folderElement = createElementWithAttribute(document, FOLDER, ATTRIBUTE_ID, id);

        if (parentFolderId != null) {
            folderElement.setAttribute(ATTRIBUTE_FOLDER_ID, parentFolderId);
        }
        String folderName = folder.getName();
        folderName = CharacterBlacklistUtil.decodeName(folderName);
        folderElement.appendChild(createElementWithTextContent(document, NAME, folderName));
        final Entity entity;
        if (parentFolderId == null) {
            //No need to map root folder by name
            entity = new Entity(FOLDER_TYPE, folderName, id, folderElement, folder);
        } else {
            String filteredPathName = EntityBuilderHelper.getPath(folder.getParentFolder(), folder.getName());
            filteredPathName = CharacterBlacklistUtil.decodePath(filteredPathName);
            entity = EntityBuilderHelper.getEntityWithPathMapping(FOLDER_TYPE, filteredPathName, filteredPathName, id
                    , folderElement, false, folder);

        }
        entity.setMappingAction(NEW_OR_EXISTING);
        return entity;
    }

    private List<Entity> buildEntities(Map<String, Folder> entities, BundleType bundleType, Document document, String targetFolder) {
        // no folder has to be added to environment bundle
        if ((entities.isEmpty() && StringUtils.isBlank(targetFolder)) || bundleType == ENVIRONMENT) {
            return Collections.emptyList();
        }
        Map<Folder, Collection<Folder>> folderChildrenMap = new HashMap<>();
        Folder rootFolder = entities.remove("");
        if (rootFolder == null) {
            throw new EntityBuilderException("Could not locate root folder.");
        }
        String targetFolderPath = null;
        if (StringUtils.isNotBlank(targetFolder)) {
            Folder bundleTargetFolder = rootFolder;
            targetFolderPath =  targetFolder + "/";
            bundleTargetFolder.setId(idGenerator.generate());
            bundleTargetFolder.setName(targetFolder);
            bundleTargetFolder.setParentFolder(Folder.ROOT_FOLDER);
            bundleTargetFolder.setPath(targetFolderPath);
            entities.put(targetFolderPath , bundleTargetFolder);
            rootFolder = Folder.ROOT_FOLDER;
            entities.put("", rootFolder);

        } else {
            rootFolder.setId(Folder.ROOT_FOLDER_ID);
            rootFolder.setName(Folder.ROOT_FOLDER_NAME);
            entities.put("", rootFolder);
        }
        entities.values().forEach(folder -> addFolder(folder, folderChildrenMap));
        Stream<Folder> folderStream = Stream.of(rootFolder).flatMap(f -> expand(f, folderChildrenMap));

        return folderStream.map(f -> {
            if (f.getId() == null) {
                f.setId(idGenerator.generate());
            }
            String parentFolderId = f.getParentFolder() != null ? f.getParentFolder().getId() : null;
            return buildFolderEntity(f, f.getId(), parentFolderId, document);
        })
                .collect(Collectors.toList());
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        Map<String, Folder> folderMap = Optional.ofNullable(bundle.getFolders()).orElse(Collections.emptyMap());
        return buildEntities(folderMap, bundleType, document, bundle.getTargetFolder());
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }

    private Stream<Folder> expand(final Folder folder, Map<Folder, Collection<Folder>> folderChildrenMap) {
        return Stream.of(folder).flatMap(f -> Stream.concat(Stream.of(f), folderChildrenMap.getOrDefault(f, Collections.emptySet()).stream().flatMap(f2 -> expand(f2, folderChildrenMap))));
    }

    private void addFolder(Folder folder, Map<Folder, Collection<Folder>> folderChildrenMap) {
        if (folder.getParentFolder() != null) {
            folderChildrenMap.compute(folder.getParentFolder(), (k, v) -> {
                if (v == null) {
                    return new HashSet<>(Collections.singleton(folder));
                } else {
                    v.add(folder);
                    return v;
                }
            });
        }
    }
}
