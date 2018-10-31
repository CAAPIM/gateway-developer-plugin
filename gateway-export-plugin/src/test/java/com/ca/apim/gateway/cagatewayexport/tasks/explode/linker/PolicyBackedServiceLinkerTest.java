/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.Folder;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.FolderTree;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyBackedServiceEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.PolicyEntity;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyBackedServiceLinkerTest {

    @Test
    void link() {
        PolicyBackedServiceLinker policyBackedServiceLinker = new PolicyBackedServiceLinker();

        Map<String, String> operations = new HashMap<>();
        operations.put("operation1", "1");
        operations.put("operation2", "2");
        PolicyBackedServiceEntity myPolicyBackedServiceEntity = new PolicyBackedServiceEntity("myEncass", "1", "PolicyBackedServiceEntity", operations);

        Bundle bundle = new Bundle();
        bundle.addEntity(myPolicyBackedServiceEntity);

        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myPolicyBackedServiceEntity);
        fullBundle.addEntity(new PolicyEntity.Builder().setName("operation1Policy").setId("1").setGuid("1").setParentFolderId("1").setPolicy("").build());
        fullBundle.addEntity(new PolicyEntity.Builder().setName("operation2Policy").setId("2").setGuid("2").setParentFolderId("1").setPolicy("").build());
        fullBundle.addEntity(new Folder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        policyBackedServiceLinker.link(bundle, fullBundle);

        assertEquals("operation1Policy.xml", bundle.getEntities(PolicyBackedServiceEntity.class).get("1").getOperations().get("operation1"));
        assertEquals("operation2Policy.xml", bundle.getEntities(PolicyBackedServiceEntity.class).get("1").getOperations().get("operation2"));
    }

    @Test
    void linkMissingPolicy() {
        PolicyBackedServiceLinker policyBackedServiceLinker = new PolicyBackedServiceLinker();

        Map<String, String> operations = new HashMap<>();
        operations.put("operation1", "1");
        operations.put("operation2", "2");
        PolicyBackedServiceEntity myPolicyBackedServiceEntity = new PolicyBackedServiceEntity("myEncass", "1", "PolicyBackedServiceEntity", operations);

        Bundle bundle = new Bundle();
        bundle.addEntity(myPolicyBackedServiceEntity);

        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myPolicyBackedServiceEntity);
        fullBundle.addEntity(new PolicyEntity.Builder().setName("operation1Policy").setId("1").setGuid("1").setParentFolderId("1").setPolicy("").build());
        fullBundle.addEntity(new PolicyEntity.Builder().setName("operation3Policy").setId("3").setGuid("3").setParentFolderId("1").setPolicy("").build());
        fullBundle.addEntity(new Folder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> policyBackedServiceLinker.link(bundle, fullBundle));
    }

    @Test
    void linkFolderIsMissing() {
        PolicyBackedServiceLinker policyBackedServiceLinker = new PolicyBackedServiceLinker();

        Map<String, String> operations = new HashMap<>();
        operations.put("operation1", "1");
        operations.put("operation2", "2");
        PolicyBackedServiceEntity myPolicyBackedServiceEntity = new PolicyBackedServiceEntity("myEncass", "1", "PolicyBackedServiceEntity", operations);

        Bundle bundle = new Bundle();
        bundle.addEntity(myPolicyBackedServiceEntity);

        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myPolicyBackedServiceEntity);
        fullBundle.addEntity(new PolicyEntity.Builder().setName("operation1Policy").setId("1").setGuid("1").setParentFolderId("1").setPolicy("").build());
        fullBundle.addEntity(new PolicyEntity.Builder().setName("operation2Policy").setId("2").setGuid("2").setParentFolderId("1").setPolicy("").build());
        fullBundle.addEntity(new Folder("myFolder", "2", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> policyBackedServiceLinker.link(bundle, fullBundle));
    }
}