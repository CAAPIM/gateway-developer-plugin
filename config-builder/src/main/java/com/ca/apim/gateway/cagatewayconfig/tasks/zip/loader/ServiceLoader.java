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
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.PolicyAndFolderLoader.getPath;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.PolicyAndFolderLoader.loadFolder;
import static java.util.Arrays.stream;

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
        bundle.putAllServices(entitiesMap);
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        // load services
        super.load(bundle, rootDir);

        final File policyRootDir = new File(rootDir, "policy");
        if (!policyRootDir.exists()) {
            // no policies to bundle. Just return
            return;
        } else if (!policyRootDir.isDirectory()) {
            throw new BundleLoadException("Expected directory but was file: " + policyRootDir);
        }

        File[] policyDirChildren = policyRootDir.listFiles();
        if (policyDirChildren == null) {
            // no policies
            throw new BundleLoadException("There should be a folder under the 'policy' folder which is the name of the project.");
        }
        List<File> files = stream(policyDirChildren).filter(File::isDirectory).collect(Collectors.toList());
        if (files.size() != 1) {
            //should only be one folder
            throw new BundleLoadException("There should be a folder under the 'policy' folder which is the name of the project.");
        }
        final Map<String, Folder> folderMap = bundle.getFolders();
        final Folder rootFolder = folderMap.computeIfAbsent(getPath(policyRootDir,policyRootDir), key -> loadFolder(policyRootDir, policyRootDir, null));
        final Folder projectFolder = folderMap.computeIfAbsent(getPath(files.get(0),policyRootDir), key -> loadFolder(files.get(0), policyRootDir, rootFolder));

        final Map<String, Service> services = bundle.getServices();
        services.forEach((servicePath, service) -> {
            int lastFileSeparator = servicePath.lastIndexOf(File.separatorChar);
            if (lastFileSeparator == -1) {
                //service is directly under the project dir
                service.setParentFolder(projectFolder);
            } else {
                //service is in a folder, create folders if they don't already exist
                String pathExcludingService = servicePath.substring(0, servicePath.lastIndexOf(File.separatorChar) + 1 );
                createFolders(pathExcludingService, folderMap, projectFolder);
                service.setParentFolder(folderMap.get(createPath(projectFolder, pathExcludingService)));
            }
        });
    }

    /**
     * Creates all folders along a path if they do not already exist, and adds them to the folderMap.
     *
     * @param stringPath the path containing the service. ie: /a/b/c/service
     * @param folderMap The existing map of folders
     * @param projectFolder The folder with the project name
     */
    private void createFolders(String stringPath, Map<String, Folder> folderMap, Folder projectFolder) {
        Path path = Paths.get(stringPath);
        List<Path> paths = new ArrayList<>();
        int i = 0;
        while (i < path.getNameCount()) {
            paths.add(path.subpath(0 , ++i));
        }
        for (final Path p : paths) {
            Folder parentFolder = p.getParent() == null ?
                    projectFolder :
                    folderMap.get(createPath(projectFolder, p.getParent().toString()));
            folderMap.computeIfAbsent(createPath(projectFolder, p.toString()), key -> {
                Folder f = new Folder();
                f.setName(p.getFileName().toString());
                f.setPath(createPath(projectFolder, p.toString()));
                f.setParentFolder(parentFolder);
                return f;
            });
        }
    }

    @NotNull
    private String createPath(Folder projectPath, String path) {
        return Paths.get(projectPath.getPath(), path).toString() + File.separator;
    }

    @Override
    public String getEntityType() {
        return "SERVICE";
    }
}
