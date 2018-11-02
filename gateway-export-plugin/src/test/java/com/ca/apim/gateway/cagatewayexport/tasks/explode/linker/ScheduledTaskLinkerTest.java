/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ScheduledTaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScheduledTaskLinkerTest {
    private Bundle bundle;
    private ScheduledTaskLinker scheduledTaskLinker;
    private ScheduledTaskEntity mySTask;

    @BeforeEach
    void setUp() {
        scheduledTaskLinker = new ScheduledTaskLinker();
        mySTask = new ScheduledTaskEntity.Builder()
                .name("task")
                .id("1")
                .jobType("Recurring")
                .jobStatus("Scheduled")
                .oneNode(true)
                .policyId("1")
                .shouldExecuteOnCreate(false)
                .build();

        bundle = new Bundle();
        bundle.addEntity(mySTask);
    }

    @Test
    void link() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(mySTask);
        fullBundle.addEntity(new PolicyEntity.Builder().setName("myScheduledPolicy").setId("1").setGuid("1").setParentFolderId("1").setPolicy("").build());
        fullBundle.addEntity(new Folder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        scheduledTaskLinker.link(bundle, fullBundle);
        assertEquals("myScheduledPolicy.xml", bundle.getEntities(ScheduledTaskEntity.class).get("1").getPolicyPath());
    }

    @Test
    void linkMissingPolicy() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(mySTask);
        fullBundle.addEntity(new Folder("folder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> scheduledTaskLinker.link(bundle, fullBundle));
    }

    @Test
    void linkFolderIsMissing() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(mySTask);
        fullBundle.addEntity(new PolicyEntity.Builder().setName("myScheduledPolicy").setId("1").setGuid("1").setParentFolderId("1").setPolicy("").build());
        fullBundle.addEntity(new Folder("myFolder", "2", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> scheduledTaskLinker.link(bundle, fullBundle));
    }
}
