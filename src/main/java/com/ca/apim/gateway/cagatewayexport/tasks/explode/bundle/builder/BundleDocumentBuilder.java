/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.builder;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

public class BundleDocumentBuilder {

    private final Set<Folder> folders = new HashSet<>();
    private final Set<Entity> entities = new HashSet<>();
    private final Document document;

    public BundleDocumentBuilder(final Document document) {
        this.document = document;
    }

    public BundleDocumentBuilder withFolder(final Folder folder) {
        folders.add(folder);
        return this;
    }

    public BundleDocumentBuilder withEntity(final Entity entity) {
        entities.add(entity);
        return this;
    }

    public Element build() {
        final Element bundle = document.createElement("l7:Bundle");
        final Element references = document.createElement("l7:References");
        final Element mappings = document.createElement("l7:Mappings");
        bundle.appendChild(references);
        bundle.appendChild(mappings);

        final FolderTree folderTree = new FolderTree(folders);
        folderTree.stream().forEach(f -> addEntity(references, mappings, f));

        entities.forEach(e -> addEntity(references, mappings, e));

        return bundle;
    }

    private void addEntity(final Element references, final Element mappings, final Entity entity) {
        final Element folderItem = buildEntityItem(entity, document);
        references.appendChild(folderItem);
        final Element folderMapping = buildEntityMapping(entity, document);
        mappings.appendChild(folderMapping);
    }

    private Element buildEntityMapping(final Entity entity, final Document document) {
        final Element mapping = document.createElement("l7:Mapping");
        mapping.setAttribute("action", "NewOrExisting");
        mapping.setAttribute("srcId", entity.getId());
        mapping.setAttribute("type", entity.getType());
        return mapping;
    }

    private Element buildEntityItem(final Entity entity, final Document document) {
        final Element item = document.createElement("l7:Item");
        final Element itemName = document.createElement("l7:Name");
        itemName.setTextContent(entity.getName());
        item.appendChild(itemName);

        final Element itemId = document.createElement("l7:Id");
        itemId.setTextContent(entity.getId());
        item.appendChild(itemId);

        final Element itemType = document.createElement("l7:Type");
        itemType.setTextContent(entity.getType());
        item.appendChild(itemType);

        final Element itemResource = document.createElement("l7:Resource");
        itemResource.appendChild(entity.getXml());
        item.appendChild(itemResource);

        return item;
    }
}
