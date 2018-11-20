/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FolderTree {

    private Map<String, Folder> idFolderMap = new HashMap<>();
    private Map<String, Collection<Folder>> folderChildrenMap = new HashMap<>();
    private Folder rootFolder;

    public FolderTree(final Collection<Folder> folders) {
        folders.forEach(this::addFolder);

        if (rootFolder == null) {
            throw new FolderTreeException("Root folder could not be found in bundle");
        }
        Set<String> orphanedTrees = folderChildrenMap.keySet().stream().filter(id -> !idFolderMap.containsKey(id)).collect(Collectors.toSet());
        if (!orphanedTrees.isEmpty()) {
            throw new FolderTreeException("Orphaned folder Trees detected:" + orphanedTrees.stream().reduce("", (s1, s2) -> s1 + " " + s2));
        }
    }

    private synchronized void addFolder(final Folder folder) {
        idFolderMap.put(folder.getId(), folder);

        if (folder.getParentFolder() != null && !folder.getParentFolder().getId().isEmpty()) {
            folderChildrenMap.compute(folder.getParentFolder().getId(), (k, v) -> {
                if (v == null) {
                    return new HashSet<>(Collections.singleton(folder));
                } else {
                    v.add(folder);
                    return v;
                }
            });
        } else if (rootFolder == null) {
            rootFolder = folder;
        } else {
            throw new FolderTreeException("Detected multiple root folders: " + rootFolder.toString() + " AND " + folder.toString());
        }
    }

    public Stream<Folder> stream() {
        return Stream.of(rootFolder).flatMap(this::expand);
    }

    public Folder getRootFolder() {
        return rootFolder;
    }

    private Stream<Folder> expand(final Folder folder) {
        return Stream.of(folder).flatMap(f -> Stream.concat(Stream.of(f), folderChildrenMap.getOrDefault(f.getId(), Collections.emptySet()).stream().flatMap(this::expand)));
    }

    public String getFormattedPath(final Folder folder) {
        return PathUtils.unixPath(getPath(folder));
    }

    public Path getPath(final Folder folder) {
        Folder currentFolder = folder;
        Path path = currentFolder.getParentFolder() == null ? Paths.get("") : Paths.get(currentFolder.getName());
        while (currentFolder.getParentFolder() != null) {
            currentFolder = idFolderMap.get(currentFolder.getParentFolder().getId());
            path = Paths.get(currentFolder.getParentFolder() == null ? "" : currentFolder.getName()).resolve(path);
        }
        return path;
    }

    public Folder getFolderById(final String folderId) {
        return idFolderMap.get(folderId);
    }
}
