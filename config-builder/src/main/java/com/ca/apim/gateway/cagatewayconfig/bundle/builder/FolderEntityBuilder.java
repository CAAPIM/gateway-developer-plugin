/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.string.EncodeDecodeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.FOLDER_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_EXISTING;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

@Singleton
public class FolderEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 100;
    private final IdGenerator idGenerator;

    @Inject
    FolderEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    private Entity buildFolderEntity(String name, String id, String parentFolderId, Document document) {
        Element folder = createElementWithAttribute(document, FOLDER, ATTRIBUTE_ID, id);

        if (parentFolderId != null) {
            folder.setAttribute(ATTRIBUTE_FOLDER_ID, parentFolderId);
        }
        String decodedName = EncodeDecodeUtils.decodePath(name);
        folder.appendChild(createElementWithTextContent(document, NAME, decodedName));
        final Entity entity;
        if (parentFolderId == null) {
            //No need to map root folder by name
            entity = new Entity(FOLDER_TYPE, decodedName, id, folder);
        } else {
            entity = EntityBuilderHelper.getEntityWithNameMapping(FOLDER_TYPE, decodedName, id, folder);
        }
        entity.setMappingAction(NEW_OR_EXISTING);
        return entity;
    }

    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        if (bundle.getFolders().isEmpty()) {
            return Collections.emptyList();
        }
        Map<Folder, Collection<Folder>> folderChildrenMap = new HashMap<>();

        bundle.getFolders().values().forEach(folder -> addFolder(folder, folderChildrenMap));

        Folder rootFolder = bundle.getFolders().get("");
        if (rootFolder == null) {
            throw new EntityBuilderException("Could not locate root folder.");
        }
        rootFolder.setId(Folder.ROOT_FOLDER_ID);
        rootFolder.setName(Folder.ROOT_FOLDER_NAME);
        Stream<Folder> folderStream = Stream.of(rootFolder).flatMap(f -> expand(f, folderChildrenMap));

        return folderStream.map(f -> {
            if (f.getId() == null) {
                f.setId(idGenerator.generate());
            }
            String parentFolderId = f.getParentFolder() != null ? f.getParentFolder().getId() : null;
            return buildFolderEntity(f.getName(), f.getId(), parentFolderId, document);
        })
                .collect(Collectors.toList());
    }

    @Override
    public Integer getOrder() {
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
