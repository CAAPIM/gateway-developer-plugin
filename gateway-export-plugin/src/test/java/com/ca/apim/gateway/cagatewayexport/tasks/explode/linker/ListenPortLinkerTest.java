/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayconfig.beans.*;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;

import static org.junit.jupiter.api.Assertions.*;

class ListenPortLinkerTest {

    @Test
    void linkNoService() {
        ListenPortLinker linker = new ListenPortLinker();
        ListenPort port = new ListenPort();
        port.setName("Port");
        Bundle bundle = new Bundle();

        linker.link(port, bundle, bundle);

        assertNull(port.getTargetServiceReference());
    }

    @Test
    void linkWithService() {
        ListenPortLinker linker = new ListenPortLinker();
        ListenPort port = new ListenPort();
        port.setName("Port");
        port.setTargetServiceReference("Service");
        Service service = new Service();
        service.setId("Service");
        service.setName("Service");
        Folder folder = new Folder();
        folder.setName("folder");
        folder.setId("folder");
        folder.setParentFolder(Folder.ROOT_FOLDER);
        service.setParentFolder(folder);
        FolderTree tree = new FolderTree(ImmutableSet.of(folder, folder.getParentFolder()));

        Bundle bundle = new Bundle();
        bundle.setFolderTree(tree);
        bundle.getServices().put(service.getId(), service);
        bundle.getFolders().put(folder.getId(), folder);

        linker.link(port, bundle, bundle);

        assertNotNull(port.getTargetServiceReference());
        assertEquals("folder/Service", port.getTargetServiceReference());
    }

    @Test
    void linkWithServiceMissing() {
        ListenPortLinker linker = new ListenPortLinker();
        ListenPort port = new ListenPort();
        port.setName("Port");
        port.setTargetServiceReference("Service");

        Bundle bundle = new Bundle();
        assertThrows(LinkerException.class, () -> linker.link(port, bundle, bundle));
    }
}