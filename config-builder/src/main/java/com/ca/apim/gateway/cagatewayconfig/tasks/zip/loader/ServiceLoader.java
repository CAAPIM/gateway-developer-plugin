/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.FolderLoaderUtils.*;

@Singleton
public class ServiceLoader extends EntityLoaderBase<Service> {

    private static final String FILE_NAME = "services";

    @Inject
    ServiceLoader(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    protected Class<Service> getBeanClass() {
        return Service.class;
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, Service> entitiesMap) {
        Map<String, Service> parsedServices = new HashMap<>(entitiesMap.size());
        entitiesMap.forEach((k, s) -> parsedServices.put(EncodeDecodeUtils.decodePath(k), s));
        bundle.putAllServices(parsedServices);
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        // load services
        super.load(bundle, rootDir);

        final File policyRootDir = getPolicyRootDir(rootDir);
        if (policyRootDir == null) return;

        final Map<String, Folder> folderMap = bundle.getFolders();
        final Folder rootFolder = folderMap.computeIfAbsent(
                getPath(policyRootDir, policyRootDir),
                key -> createFolder(policyRootDir.getName(), key, null)
        );

        final Map<String, Service> services = bundle.getServices();
        services.forEach((servicePath, service) -> {
            final String pathExcludingService = FilenameUtils.getFullPath(servicePath);
            if (StringUtils.isEmpty(pathExcludingService)) {
                //service is directly under the root dir
                service.setParentFolder(rootFolder);
            } else {
                //service is in a folder, create folders if they don't already exist
                createFolders(pathExcludingService, folderMap, rootFolder);
                service.setParentFolder(folderMap.get(pathExcludingService));
            }
        });
    }

    @Override
    public String getEntityType() {
        return "SERVICE";
    }
}
