/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.config.loader.FolderLoaderUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class FolderLoaderUtilsTest {

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void getPolicyAsStringTest(TemporaryFolder temporaryFolder) throws IOException {

        File root = new File(temporaryFolder.getRoot(), "a");
        File a = new File(root, "a");
        File b = new File(a, "b");
        File c = new File(b, "c");
        c.mkdirs();
        File policy = new File(c, "policy.xml");
        Files.touch(policy);

        String path = getPath(policy, root);

        assertEquals("a/b/c/policy.xml", path);
    }

    @Test
    void createFolderTest() {
        Folder root = createFolder("root", "", null);
        Folder child = createFolder("child","/", root);

        assertEquals("child", child.getName());
        assertEquals("/", child.getPath());
        assertEquals(root, child.getParentFolder());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void getPolicyRootNoPolicyDirTest(TemporaryFolder temporaryFolder) {
        assertNull(getPolicyRootDir(temporaryFolder.getRoot()));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void getPolicyRootDirPolicyFileErrorTest(TemporaryFolder temporaryFolder) throws IOException {
        temporaryFolder.createFile("policy");
        assertThrows(ConfigLoadException.class, () -> getPolicyRootDir(temporaryFolder.getRoot()));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void getPolicyRootDirTest(TemporaryFolder temporaryFolder) {
        temporaryFolder.createDirectory("policy");
        assertNotNull(getPolicyRootDir(temporaryFolder.getRoot()));
    }

    @Test
    void createFoldersTest() {
        //Set up
        String path = "a/b/c/";
        Folder root = createFolder("root", "", null);
        Map<String, Folder> folderMap = new HashedMap<>();
        //Test
        createFoldersAlongPath(path, folderMap, root);

        assertEquals(3, folderMap.size());
        verifyFolder("a/", root, folderMap);
        verifyFolder("a/b/", folderMap.get("a/"), folderMap);
        verifyFolder("a/b/c/", folderMap.get("a/b/"), folderMap);
    }

    private void verifyFolder(String folderPath, Folder parent, Map<String, Folder> folderMap) {
        Folder folderToVerify = folderMap.get(folderPath);
        assertEquals(folderPath, folderToVerify.getPath());
        assertEquals(parent, folderToVerify.getParentFolder());
        assertEquals(Paths.get(folderPath).getFileName().toString(), folderToVerify.getName());
    }

}