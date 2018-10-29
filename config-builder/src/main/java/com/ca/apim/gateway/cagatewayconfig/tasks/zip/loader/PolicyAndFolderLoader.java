/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader.FolderLoaderUtils.*;

@Singleton
public class PolicyAndFolderLoader implements EntityLoader {

    private final FileUtils fileUtils;
    private final IdGenerator idGenerator;

    @Inject
    PolicyAndFolderLoader(FileUtils fileUtils, IdGenerator idGenerator) {
        this.fileUtils = fileUtils;
        this.idGenerator = idGenerator;
    }

    @Override
    public void load(final Bundle bundle, final File rootDir) {
        final File policyRootDir = getPolicyRootDir(rootDir);
        if (policyRootDir == null) return;

        final Map<String, Policy> policies = new HashMap<>();
        loadPolicies(policyRootDir, policyRootDir, null, policies, bundle.getFolders());
        bundle.putAllPolicies(policies);
    }

    @Override
    public void load(Bundle bundle, String name, String value) {
        throw new BundleLoadException("Cannot load an individual policy");
    }

    private void loadPolicies(final File currentDir, final File rootDir, Folder parentFolder, final Map<String, Policy> policies, Map<String, Folder> folders) {
        Folder folder = folders.computeIfAbsent(getPath(currentDir, rootDir), key -> createFolder(currentDir.getName(), key, parentFolder));
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

    private Policy loadPolicy(final File policyFile, final File rootDir, Folder parentFolder) {
        Policy policy = new Policy();
        policy.setPath(getPath(policyFile, rootDir));
        policy.setPolicyXML(fileUtils.getFileAsString(policyFile));
        policy.setName(getPolicyName(policyFile));
        policy.setParentFolder(parentFolder);
        policy.setGuid(idGenerator.generateGuid());
        policy.setId(idGenerator.generate());
        return policy;
    }

    @NotNull
    String getPolicyName(File policyFile) {
        String fileName = policyFile.getName();
        int indexOfPeriod = fileName.lastIndexOf('.');
        if (indexOfPeriod > 0) {
            return fileName.substring(0, indexOfPeriod);
        } else {
            return fileName;
        }
    }

    @Override
    public String getEntityType() {
        return "POLICY";
    }
}
