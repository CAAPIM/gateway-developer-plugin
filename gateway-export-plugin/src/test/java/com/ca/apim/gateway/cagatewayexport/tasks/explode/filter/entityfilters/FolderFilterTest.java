package com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.entityfilters;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.FolderTree;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.filter.FilterConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createFolder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FolderFilterTest {
    @Test
    void filterEmptyBundle() {
        FolderFilter folderFilter = new FolderFilter();

        Bundle filteredBundle = new Bundle();
        List<Folder> childFolders = folderFilter.filter("/my/folder/path", new FilterConfiguration(), FilterTestUtils.getBundle(), filteredBundle);

        assertTrue(childFolders.isEmpty());
    }

    @Test
    void filterBundle() {
        Bundle bundle = FilterTestUtils.getBundle();
        Folder folder1 = createFolder("my", "1", Folder.ROOT_FOLDER);
        Folder folder2 = createFolder("folder", "2", folder1);
        Folder folder3 = createFolder("path", "3", createFolder("folder", "2", folder1));
        bundle.addEntity(folder1);
        bundle.addEntity(folder2);
        bundle.addEntity(createFolder("fold", "5", folder1));
        bundle.addEntity(folder3);
        bundle.addEntity(createFolder("sub-folder", "4", folder3));
        bundle.addEntity(createFolder("another-path", "6", folder2));
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        FolderFilter folderFilter = new FolderFilter();

        Bundle filteredBundle = new Bundle();
        List<Folder> childFolders = folderFilter.filter("/my/folder", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(4, childFolders.size());
        assertTrue(childFolders.stream().anyMatch(f -> "folder".equals(f.getName())));
        assertTrue(childFolders.stream().anyMatch(f -> "path".equals(f.getName())));
        assertTrue(childFolders.stream().anyMatch(f -> "another-path".equals(f.getName())));
        assertTrue(childFolders.stream().anyMatch(f -> "sub-folder".equals(f.getName())));
    }

    @Test
    void filterParentFolderNoParents() {
        List<Folder> parentFolders = FolderFilter.parentFolders("my/folder/path", FilterTestUtils.getBundle());
        assertEquals(1, parentFolders.size());
        assertTrue(parentFolders.stream().anyMatch(f -> "Root Node".equals(f.getName())));
    }

    @Test
    void filterParentFolder() {
        Bundle bundle = FilterTestUtils.getBundle();
        Folder folder1 = createFolder("my", "1", Folder.ROOT_FOLDER);
        Folder folder2 = createFolder("folder", "2", folder1);
        Folder folder3 = createFolder("path", "3", folder2);
        bundle.addEntity(folder1);
        bundle.addEntity(folder2);
        bundle.addEntity(folder3);
        bundle.addEntity(createFolder("sub-folder", "4", folder3));
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
        Bundle bundle = FilterTestUtils.getBundle();
        Folder folder1 = createFolder("my", "1", Folder.ROOT_FOLDER);
        Folder folder2 = createFolder("folder", "2", folder1);
        Folder folder3 = createFolder("path", "3", folder2);
        bundle.addEntity(folder1);
        bundle.addEntity(folder2);
        bundle.addEntity(createFolder("fold", "5", folder1));
        bundle.addEntity(folder3);
        bundle.addEntity(createFolder("pa", "6", folder2));
        bundle.addEntity(createFolder("sub-folder", "4", folder3));
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        List<Folder> parentFolders = FolderFilter.parentFolders("/my/folder/path", bundle);
        assertEquals(4, parentFolders.size());
        assertTrue(parentFolders.stream().anyMatch(f -> "Root Node".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "my".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "folder".equals(f.getName())));
        assertTrue(parentFolders.stream().anyMatch(f -> "path".equals(f.getName())));
    }

    @Test
    void filterBundleWithMultipleFoldersStartingWithTheSameName() {
        Bundle bundle = FilterTestUtils.getBundle();
        Folder folder1 = createFolder("Folder Test", "1", Folder.ROOT_FOLDER);
        Folder folder2 = createFolder("Folder Test 2", "2", Folder.ROOT_FOLDER);
        bundle.addEntity(folder1);
        bundle.addEntity(folder2);
        bundle.setFolderTree(new FolderTree(bundle.getEntities(Folder.class).values()));

        FolderFilter folderFilter = new FolderFilter();

        Bundle filteredBundle = new Bundle();
        List<Folder> childFolders = folderFilter.filter("/Folder Test", new FilterConfiguration(), bundle, filteredBundle);

        assertEquals(1, childFolders.size());
        assertEquals("Folder Test", childFolders.iterator().next().getName());
    }
}