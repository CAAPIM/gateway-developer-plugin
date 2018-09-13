/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.FOLDER_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

public class FolderEntityBuilder implements EntityBuilder {
    private static final String ROOT_FOLDER_ID = "0000000000000000ffffffffffffec76";
    private static final String ROOT_FOLDER_NAME = "Root Node";
    private final IdGenerator idGenerator;
    private final Document document;

    FolderEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    private Entity buildFolderEntity(String name, String id, String parentFolderId) {
        Element folder = createElementWithAttribute(document, FOLDER, ATTRIBUTE_ID, id);

        if (parentFolderId != null) {
            folder.setAttribute(ATTRIBUTE_FOLDER_ID, parentFolderId);
        }
        folder.appendChild(createElementWithTextContent(document, NAME, name));
        return new Entity(FOLDER_TYPE, name, id, folder);
    }

    public List<Entity> build(Bundle bundle) {

        Map<Folder, Collection<Folder>> folderChildrenMap = new HashMap<>();

        bundle.getFolders().values().forEach(folder -> addFolder(folder, folderChildrenMap));

        Folder rootFolder = bundle.getFolders().get("");
        if(rootFolder == null) {
            throw new EntityBuilderException("Could not locate root folder.");
        }
        rootFolder.setId(ROOT_FOLDER_ID);
        rootFolder.setName(ROOT_FOLDER_NAME);
        Stream<Folder> folderStream = Stream.of(rootFolder).flatMap(f -> expand(f, folderChildrenMap));

        return folderStream.map(f -> {
            if (f.getId() == null) {
                f.setId(idGenerator.generate());
            }
            String parentFolderId = f.getParentFolder() != null ? f.getParentFolder().getId() : null;
            return buildFolderEntity(f.getName(), f.getId(), parentFolderId);
        })
                .collect(Collectors.toList());
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
