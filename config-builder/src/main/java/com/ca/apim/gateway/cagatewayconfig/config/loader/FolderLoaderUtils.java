/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.Folderable;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils.unixPath;
import static com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils.unixPathEndingWithSeparator;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class FolderLoaderUtils {

    /**
     * Creates all the folders along the path for each folderable bean
     * @param bundle The bundle
     * @param rootDir The project rootDir
     * @param folderableMap The map of folderable entities in which the key is the path relative to the `policy` folder
     * @param <E> The folderable bean
     */
    public static <E extends Folderable> void createFolders(Bundle bundle, File rootDir, Map<String, E> folderableMap) {
        final File policyRootDir = getPolicyRootDir(rootDir);
        if (policyRootDir == null) return;

        final Map<String, Folder> folderMap = bundle.getFolders();
        final Folder rootFolder = folderMap.computeIfAbsent(
                getPath(policyRootDir, policyRootDir),
                key -> createFolder(policyRootDir.getName(), key, null)
        );

        folderableMap.forEach((folderablePath, folderable) -> {
            final String pathExcludingService = FilenameUtils.getFullPath(folderablePath);
            if (isEmpty(pathExcludingService)) {
                //service is directly under the root dir
                folderable.setParentFolder(rootFolder);
            } else {
                //service is in a folder, create folders if they don't already exist
                createFoldersAlongPath(pathExcludingService, folderMap, rootFolder);
                folderable.setParentFolder(folderMap.get(pathExcludingService));
            }
        });
    }

    @Nullable
    static File getPolicyRootDir(File rootDir) {
        final File policyRootDir = new File(rootDir, "policy");

        if (!policyRootDir.exists()) {
            // no policies to bundle. Just return
            return null;
        } else if (!policyRootDir.isDirectory()) {
            throw new ConfigLoadException("Expected directory but was file: " + policyRootDir);
        }
        return policyRootDir;
    }

    /**
     * Creates all folders along a path if they do not already exist, and adds them to the folderMap.
     *
     * @param stringPath the folder path to the service.
     * @param folderMap The existing map of folders
     * @param rootFolder The root folder
     */
    static void createFoldersAlongPath(final String stringPath, Map<String, Folder> folderMap, Folder rootFolder) {
        final Path path = Paths.get(stringPath);
        List<Path> paths = new ArrayList<>();
        int i = 0;
        while (i < path.getNameCount()) {
            paths.add(path.subpath(0 , ++i));
        }
        for (final Path p : paths) {
            Folder parentFolder = p.getParent() == null ?
                    rootFolder :
                    folderMap.get(unixPathEndingWithSeparator(p.getParent()));
            folderMap.computeIfAbsent(
                    unixPathEndingWithSeparator(p),
                    key -> createFolder(p.getFileName().toString(), key, parentFolder)
            );
        }
    }

    static Folder createFolder(String folderName, String folderPath, Folder parentFolder) {
        Folder folder = new Folder();
        folder.setName(folderName);
        folder.setPath(folderPath);
        folder.setParentFolder(parentFolder);
        return folder;
    }

    static String getPath(final File policy, final File policyRootDir) {
        String path = policyRootDir.toURI().relativize(policy.toURI()).getPath();
        if (policy.isFile() || path.isEmpty()) {
            return PathUtils.unixPath(path);
        }
        return PathUtils.unixPathEndingWithSeparator(path);
    }

    private FolderLoaderUtils(){}
}
