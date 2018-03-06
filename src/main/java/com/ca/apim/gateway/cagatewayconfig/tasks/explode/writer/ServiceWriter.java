/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Service;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;

import java.nio.file.Path;

public class ServiceWriter implements EntityWriter<Service> {
    private final FolderTree folderTree;
    private final DocumentTools documentTools;
    private final DocumentFileUtils documentFileUtils;

    public ServiceWriter(FolderTree folderTree, DocumentTools documentTools, DocumentFileUtils documentFileUtils) {
        this.folderTree = folderTree;
        this.documentTools = documentTools;
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void write(Path path, Service entity) {
        Folder folder = folderTree.getFolderById(entity.getFolderId());
        Path folderPath = path.resolve(folderTree.getPath(folder));

        documentFileUtils.createFile(entity.getServiceDetailsElement(), folderPath.resolve(entity.getName() + ".xml"), true);
        try {
            documentFileUtils.createFile(WriterHelper.stringToXML(documentTools, entity.getPolicy()), folderPath.resolve(entity.getName() + ".policy.xml"), false);
        } catch (DocumentParseException e) {
            throw new WriteException("Exception writing entity: " + entity, e);
        }

    }
}
