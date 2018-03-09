/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Service;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class ServiceWriter implements EntityWriter<Service> {
    private final FolderTree folderTree;
    private final DocumentTools documentTools;
    private final DocumentFileUtils documentFileUtils;

    private final List<Service> services = new LinkedList<>();

    public ServiceWriter(FolderTree folderTree, DocumentTools documentTools, DocumentFileUtils documentFileUtils) {
        this.folderTree = folderTree;
        this.documentTools = documentTools;
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void write(Path path, Service entity) {
        Folder folder = folderTree.getFolderById(entity.getFolderId());
        Path folderPath = path.resolve(folderTree.getPath(folder));

        services.add(entity);
        try {
            documentFileUtils.createFile(WriterHelper.stringToXML(documentTools, entity.getPolicy()), folderPath.resolve(entity.getName() + ".xml"), false);
        } catch (DocumentParseException e) {
            throw new WriteException("Exception writing entity: " + entity, e);
        }
    }

    @Override
    public void finalizeWrite(Path path) {
        JSONObject servicesList = new JSONObject();
        services.forEach(s -> {
            folderTree.getFolderById(s.getFolderId());
            servicesList.append(folderTree.getPath(folderTree.getFolderById(s.getFolderId())).resolve(s.getName()).toString(), buildServiceObject(s));
        });
        Path configFolderPath = path.resolve("config");
        if(!configFolderPath.toFile().exists()){
            try {
                Files.createDirectory(configFolderPath);
            } catch (IOException e) {
                throw new WriteException("Exception writing entity: " + configFolderPath);
            }
        }

        try {
            Files.write(configFolderPath.resolve("services.json"), servicesList.toString(4).getBytes());
        } catch (IOException e) {
            throw new WriteException("Exception writing entity: " + configFolderPath.resolve("services.json"), e);
        }
    }

    private JSONObject buildServiceObject(Service s) {
        JSONObject service = new JSONObject();
        service.append("url", s.getUrl());
        s.getHttpMethods().forEach(m -> service.accumulate("http-methods", m));
        return service;
    }

    /**
     * "gateway-solution/my-api-service": {
     * "url": "/example",
     * "http-methods": ["GET", "PUT", "POST"]
     * }
     */
}
