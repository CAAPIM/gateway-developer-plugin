/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;

@Singleton
public class PolicyWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;

    @Inject
    PolicyWriter(DocumentFileUtils documentFileUtils) {
        this.documentFileUtils = documentFileUtils;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
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
        services.values().parallelStream().forEach(serviceEntity -> writePolicy(bundle, policyFolder, serviceEntity.getParentFolder().getId(), serviceEntity.getName(), serviceEntity.getPolicyXML()));

        Map<String, Policy> policies = bundle.getEntities(Policy.class);
        policies.values().parallelStream().forEach(policyEntity -> writePolicy(bundle, policyFolder, policyEntity.getParentFolder().getId(), policyEntity.getName(), policyEntity.getPolicyDocument()));

    }

    private void writePolicy(Bundle bundle, File policyFolder, String folderId, String name, Element policy) {
        Folder folder = bundle.getFolderTree().getFolderById(folderId);
        Path folderPath = policyFolder.toPath().resolve(bundle.getFolderTree().getPath(folder));
        documentFileUtils.createFolders(folderPath);

        Path policyPath = folderPath.resolve(name);
        documentFileUtils.createFile(policy, policyPath, false);
    }
}
