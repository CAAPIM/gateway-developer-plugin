/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.string.EncodeDecodeUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class FolderLoaderUtils {

    @Nullable
    static File getPolicyRootDir(File rootDir) {
        final File policyRootDir = new File(rootDir, "policy");

        if (!policyRootDir.exists()) {
            // no policies to bundle. Just return
            return null;
        } else if (!policyRootDir.isDirectory()) {
            throw new BundleLoadException("Expected directory but was file: " + policyRootDir);
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
    static void createFolders(final String stringPath, Map<String, Folder> folderMap, Folder rootFolder) {
        final Path path = Paths.get(stringPath);
        List<Path> paths = new ArrayList<>();
        int i = 0;
        while (i < path.getNameCount()) {
            paths.add(path.subpath(0 , ++i));
        }
        for (final Path p : paths) {
            Folder parentFolder = p.getParent() == null ?
                    rootFolder :
                    folderMap.get(p.getParent().toString() + "/");
            folderMap.computeIfAbsent(
                    p.toString() + "/",
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
        return EncodeDecodeUtils.decodePath(policyRootDir.toURI().relativize(policy.toURI()).getPath());
    }

    private FolderLoaderUtils(){}
}
