/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyBackedServiceLinkerTest {

    @Test
    void link() {
        PolicyBackedServiceLinker policyBackedServiceLinker = new PolicyBackedServiceLinker();

        Map<String, String> operations = new HashMap<>();
        operations.put("operation1", "1");
        operations.put("operation2", "2");
        PolicyBackedService myPolicyBackedService = createPolicyBackedService("myEncass", "1", "PolicyBackedService", operations);

        Bundle bundle = new Bundle();
        bundle.addEntity(myPolicyBackedService);

        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myPolicyBackedService);
        fullBundle.addEntity(createPolicy("operation1Policy", "1", "1", "1", null, ""));
        fullBundle.addEntity(createPolicy("operation2Policy", "2", "2", "1", null, ""));
        fullBundle.addEntity(createFolder("myFolder", "1", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        policyBackedServiceLinker.link(bundle, fullBundle);

        assertEquals("operation1Policy", bundle.getEntities(PolicyBackedService.class).get("1").getOperations().stream().collect(Collectors.toMap(PolicyBackedServiceOperation::getOperationName, PolicyBackedServiceOperation::getPolicy)).get("operation1"));
        assertEquals("operation2Policy", bundle.getEntities(PolicyBackedService.class).get("1").getOperations().stream().collect(Collectors.toMap(PolicyBackedServiceOperation::getOperationName, PolicyBackedServiceOperation::getPolicy)).get("operation2"));
    }

    @Test
    void linkMissingPolicy() {
        PolicyBackedServiceLinker policyBackedServiceLinker = new PolicyBackedServiceLinker();

        Map<String, String> operations = new HashMap<>();
        operations.put("operation1", "1");
        operations.put("operation2", "2");
        PolicyBackedService myPolicyBackedService = createPolicyBackedService("myEncass", "1", "PolicyBackedService", operations);

        Bundle bundle = new Bundle();
        bundle.addEntity(myPolicyBackedService);

        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myPolicyBackedService);
        fullBundle.addEntity(createPolicy("operation1Policy", "1", "1", "1", null, ""));
        fullBundle.addEntity(createPolicy("operation3Policy", "3", "3", "1", null, ""));
        fullBundle.addEntity(createFolder("myFolder", "1", null));

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
        PolicyBackedService myPolicyBackedService = createPolicyBackedService("myEncass", "1", "PolicyBackedService", operations);

        Bundle bundle = new Bundle();
        bundle.addEntity(myPolicyBackedService);

        Bundle fullBundle = new Bundle();
        fullBundle.addEntity(myPolicyBackedService);
        fullBundle.addEntity(createPolicy("operation1Policy", "1", "1", "1", null, ""));
        fullBundle.addEntity(createPolicy("operation2Policy", "2", "2", "1", null, ""));
        fullBundle.addEntity(createFolder("myFolder", "2", null));

        FolderTree folderTree = new FolderTree(fullBundle.getEntities(Folder.class).values());
        fullBundle.setFolderTree(folderTree);

        assertThrows(LinkerException.class, () -> policyBackedServiceLinker.link(bundle, fullBundle));
    }
}