/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PolicyAndFolderLoader implements EntityLoader {

    private final FileUtils fileUtils;

    public PolicyAndFolderLoader(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final File policyRootDir = new File(rootDir, "policy");

        if (!policyRootDir.exists()) {
            // no policies to bundle. Just return
            return;
        } else if (!policyRootDir.isDirectory()) {
            throw new BundleLoadException("Expected directory but was file: " + policyRootDir);
        }

        final Map<String, Policy> policies = new HashMap<>();
        final Map<String, Folder> folders = new HashMap<>();
        loadPolicies(policyRootDir, policyRootDir, null, policies, folders);
        bundle.putAllPolicies(policies);
        bundle.putAllFolders(folders);
    }

    private void loadPolicies(final File currentDir, final File rootDir, Folder parentFolder, final Map<String, Policy> policies, Map<String, Folder> folders) {
        Folder folder = loadFolder(currentDir, rootDir, parentFolder);
        folders.put(folder.getPath(), folder);
        final File[] children = currentDir.listFiles();
        if (children != null) {
            for (final File child : children) {
                if (child.isDirectory()) {
                    loadPolicies(child, rootDir, folder, policies, folders);
                } else {
                    Policy policy = loadPolicy(child, rootDir, folder);
                    policies.put(policy.getPath(), policy);
                }
            }
        }
    }

    private Folder loadFolder(File folderFile, File rootDir, Folder parentFolder) {
        Folder folder = new Folder();
        folder.setName(folderFile.getName());
        folder.setPath(getPath(folderFile, rootDir));
        folder.setParentFolder(parentFolder);
        return folder;
    }

    private Policy loadPolicy(final File policyFile, final File rootDir, Folder parentFolder) {
        Policy policy = new Policy();
        policy.setPath(getPath(policyFile, rootDir));
        policy.setPolicyXML(fileUtils.getFileAsString(policyFile));
        policy.setName(getPolicyName(policyFile));
        policy.setParentFolder(parentFolder);
        return policy;
    }

    @NotNull
    String getPolicyName(File policyFile) {
        String fileName = policyFile.getName();
        int indexOfPeriod = fileName.lastIndexOf('.');
        if(indexOfPeriod > 0) {
            return fileName.substring(0, indexOfPeriod);
        } else {
            return fileName;
        }
    }

    String getPath(final File policy, final File policyRootDir) {
        return policyRootDir.toURI().relativize(policy.toURI()).getPath();
    }
}
