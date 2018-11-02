/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EncassEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EncassLinkerTest {
    private EncassLinker encassLinker;
    private EncassEntity myEncass;
    private Bundle bundle;

    @BeforeEach
    void setUp() {
        encassLinker = new EncassLinker();
        myEncass = new EncassEntity("myEncass", "1", "1", "1", null, null);
        bundle = new Bundle();
        bundle.addEntity(myEncass);
    }

    @Test
    void link() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(new PolicyEntity.Builder().setName("myEncassPolicy").setId("1").setGuid("1").setParentFolderId("1").setPolicy("").build());
        fullBundle.addEntity(new Folder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        encassLinker.link(bundle, fullBundle);

        assertEquals("myEncassPolicy.xml", bundle.getEntities(EncassEntity.class).get("1").getPath());
    }

    @Test
    void linkMissingPolicy() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(new Folder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> encassLinker.link(bundle, fullBundle));
    }

    @Test
    void linkFolderIsMissing() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(new PolicyEntity.Builder().setName("myEncassPolicy").setId("1").setGuid("1").setParentFolderId("1").setPolicy("").build());
        fullBundle.addEntity(new Folder("myFolder", "2", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> encassLinker.link(bundle, fullBundle));
    }
}