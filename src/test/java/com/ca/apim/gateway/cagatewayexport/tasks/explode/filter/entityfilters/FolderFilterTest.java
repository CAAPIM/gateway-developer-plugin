package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FolderFilterTest {

    private static final String ROOT_FOLDER_ID = "0000000000000000ffffffffffffec76";

    @Test
    void filterEmptyBundle() {
        FolderFilter folderFilter = new FolderFilter();

        Bundle filteredBundle = new Bundle();
        List<Folder> childFolders = folderFilter.filter("/my/folder/path", getBundle(), filteredBundle);

        assertTrue(childFolders.isEmpty());
    }

    @Test
    void filterBundle() {
        Bundle bundle = getBundle();
        bundle.addEntity(new Folder("my", "1", ROOT_FOLDER_ID));
        bundle.addEntity(new Folder("folder", "2", "1"));
        bundle.addEntity(new Folder("fold", "5", "1"));
        bundle.addEntity(new Folder("path", "3", "2"));
        bundle.addEntity(new Folder("sub-folder", "4", "3"));
        bundle.addEntity(new Folder("another-path", "6", "2"));
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        FolderFilter folderFilter = new FolderFilter();

        Bundle filteredBundle = new Bundle();
        List<Folder> childFolders = folderFilter.filter("/my/folder", bundle, filteredBundle);

        assertEquals(4, childFolders.size());
        assertTrue(childFolders.stream().anyMatch(f -> "folder".equals(f.getName())));
        assertTrue(childFolders.stream().anyMatch(f -> "path".equals(f.getName())));
        assertTrue(childFolders.stream().anyMatch(f -> "another-path".equals(f.getName())));
        assertTrue(childFolders.stream().anyMatch(f -> "sub-folder".equals(f.getName())));
    }

    @Test
    void filterParentFolderNoParents() {
        List<Folder> parentFolders = FolderFilter.parentFolders("my/folder/path", getBundle());
        assertEquals(1, parentFolders.size());
        assertTrue(parentFolders.stream().anyMatch(f -> "Root Node".equals(f.getName())));
    }

    @Test
    void filterParentFolder() {
        Bundle bundle = getBundle();
        bundle.addEntity(new Folder("my", "1", ROOT_FOLDER_ID));
        bundle.addEntity(new Folder("folder", "2", "1"));
        bundle.addEntity(new Folder("path", "3", "2"));
        bundle.addEntity(new Folder("sub-folder", "4", "3"));
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        List<Folder> parentFolders = FolderFilter.parentFolders("/my/folder/path", bundle);
        assertEquals(4, parentFolders.size());
        assertTrue(parentFolders.stream().anyMatch(f -> "Root Node".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "my".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "folder".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "path".equals(f.getName())));
    }

    @Test
    void filterParentFolderFoldersWithPartialNames() {
        Bundle bundle = getBundle();
        bundle.addEntity(new Folder("my", "1", ROOT_FOLDER_ID));
        bundle.addEntity(new Folder("folder", "2", "1"));
        bundle.addEntity(new Folder("fold", "5", "1"));
        bundle.addEntity(new Folder("path", "3", "2"));
        bundle.addEntity(new Folder("pa", "6", "2"));
        bundle.addEntity(new Folder("sub-folder", "4", "3"));
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        List<Folder> parentFolders = FolderFilter.parentFolders("/my/folder/path", bundle);
        assertEquals(4, parentFolders.size());
        assertTrue(parentFolders.stream().anyMatch(f -> "Root Node".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "my".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "folder".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "path".equals(f.getName())));
    }

    @NotNull
    private Bundle getBundle() {
        Bundle bundle = new Bundle();
        bundle.addEntity(new Folder("Root Node", ROOT_FOLDER_ID, null));
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));
        return bundle;
    }
}