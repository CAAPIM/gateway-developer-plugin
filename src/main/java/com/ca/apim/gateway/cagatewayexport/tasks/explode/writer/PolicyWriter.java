/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ServiceEntity;
import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import org.w3c.dom.Element;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

public class PolicyWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;

    PolicyWriter(DocumentFileUtils documentFileUtils) {
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        File policyFolder = new File(rootFolder, "policy");
        documentFileUtils.createFolder(policyFolder.toPath());

        //create folders
        bundle.getFolderTree().stream().forEach(folder -> {
            if (folder.getParentFolderId() != null) {
                Path folderFile = policyFolder.toPath().resolve(bundle.getFolderTree().getPath(folder));
                documentFileUtils.createFolder(folderFile);
            }
        });

        //create policies
        Map<String, ServiceEntity> services = bundle.getEntities(ServiceEntity.class);
        services.values().parallelStream().forEach(serviceEntity -> writePolicy(bundle, policyFolder, serviceEntity.getFolderId(), serviceEntity.getName(), serviceEntity.getPolicyXml()));

        Map<String, PolicyEntity> policies = bundle.getEntities(PolicyEntity.class);
        policies.values().parallelStream().forEach(policyEntity -> writePolicy(bundle, policyFolder, policyEntity.getFolderId(), policyEntity.getName(), policyEntity.getPolicyXML()));

    }

    private void writePolicy(Bundle bundle, File policyFolder, String folderId, String name, Element policy) {
        Folder folder = bundle.getFolderTree().getFolderById(folderId);
        Path folderPath = policyFolder.toPath().resolve(bundle.getFolderTree().getPath(folder));

        Path policyPath = folderPath.resolve(name + ".xml");
        documentFileUtils.createFile(policy, policyPath, false);
    }
}
