/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.beans.FolderTree;
import com.ca.apim.gateway.cagatewayconfig.beans.ScheduledTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createFolder;
import static com.ca.apim.gateway.cagatewayexport.util.TestUtils.createPolicy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScheduledTaskLinkerTest {
    private Bundle bundle;
    private ScheduledTaskLinker scheduledTaskLinker;
    private ScheduledTask mySTask;

    @BeforeEach
    void setUp() {
        scheduledTaskLinker = new ScheduledTaskLinker();
        mySTask = new ScheduledTask.Builder()
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
        fullBundle.addEntity(createPolicy("myScheduledPolicy", "1", "1", "1", null, ""));
        fullBundle.addEntity(createFolder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        scheduledTaskLinker.link(bundle, fullBundle);
        assertEquals("myScheduledPolicy.xml", bundle.getEntities(ScheduledTask.class).get("1").getPolicy());
    }

    @Test
    void linkMissingPolicy() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(mySTask);
        fullBundle.addEntity(createFolder("folder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> scheduledTaskLinker.link(bundle, fullBundle));
    }

    @Test
    void linkFolderIsMissing() {
        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(mySTask);
        fullBundle.addEntity(createPolicy("myScheduledPolicy", "1", "1", "1", null, ""));
        fullBundle.addEntity(createFolder("myFolder", "2", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> scheduledTaskLinker.link(bundle, fullBundle));
    }
}
