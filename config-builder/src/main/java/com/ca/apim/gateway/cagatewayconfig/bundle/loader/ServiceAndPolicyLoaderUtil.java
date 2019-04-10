package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceAndPolicyLoaderUtil {

    /**
     * Get path with file name
     *
     * @param parentFolder
     * @param fileName name used to create a path with parent folder
     * @return a path ending with name
     */
    public static String getPath(Folder parentFolder, String fileName) {
        return PathUtils.unixPath(Paths.get(parentFolder.getPath()).resolve(fileName));
    }

    /**
     * Extract folder object from bundle
     *
     * @param bundle bundle contaning the folder
     * @param folderId id of folder to be extracted from bundle
     * @return folder object from bundle
     */
    public static Folder getFolder(Bundle bundle, String folderId) {
        List<Folder> folderList = bundle.getFolders().values().stream().filter(f -> folderId.equals(f.getId())).collect(Collectors.toList());
        if (folderList.isEmpty()) {
            throw new BundleLoadException("Invalid dependency bundle. Could not find folder with id: " + folderId);
        } else if (folderList.size() > 1) {
            throw new BundleLoadException("Invalid dependency bundle. Found multiple folders with id: " + folderId);
        }
        return folderList.get(0);
    }

    private ServiceAndPolicyLoaderUtil(){}
}
