/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayconfig.bundle.entity.FolderTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FolderWriter implements EntityWriter<Folder> {

    private final FolderTree folderTree;

    public FolderWriter(FolderTree folderTree) {
        this.folderTree = folderTree;
    }

    @Override
    public void write(Path path, Folder folder) {
        if (folder.getParentFolderId() != null) {
            Path folderFile = path.resolve(folderTree.getPath(folder));
            if(!folderFile.toFile().exists()){
                try {
                    Files.createDirectory(folderFile);
                } catch (IOException e) {
                    throw new WriteException("Exception writing entity: " + folder);
                }
            } else if (!folderFile.toFile().isDirectory()){
                throw new WriteException("Could not create folder: " + folder);
            }
        }
    }

    @Override
    public void finalizeWrite(Path path) {
        //does nothing
    }
}
