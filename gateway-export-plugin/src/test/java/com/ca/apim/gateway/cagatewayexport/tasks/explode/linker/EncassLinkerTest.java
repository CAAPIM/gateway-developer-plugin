/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.FolderTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EncassLinkerTest {
    private EncassLinker encassLinker;
    private Encass myEncass;
    private Bundle bundle;

    @BeforeEach
    void setUp() {
        encassLinker = new EncassLinker();
        myEncass = createEncass("myEncass", "1", "1", "1");
        bundle = new Bundle();
        bundle.addEntity(myEncass);
    }

    @Test
    void link() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(createPolicy("myEncassPolicy", "1", "1", "1", null, EMPTY));
        fullBundle.addEntity(createFolder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        encassLinker.link(bundle, fullBundle);

        assertEquals("myEncassPolicy", bundle.getEntities(Encass.class).get("1").getPath());
    }

    @Test
    void linkMissingPolicy() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(createFolder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> encassLinker.link(bundle, fullBundle));
    }

    @Test
    void linkFolderIsMissing() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myEncass);
        fullBundle.addEntity(createPolicy("myEncassPolicy", "1","1", "1", null, ""));
        fullBundle.addEntity(createFolder("myFolder", "2", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> encassLinker.link(bundle, fullBundle));
    }
}