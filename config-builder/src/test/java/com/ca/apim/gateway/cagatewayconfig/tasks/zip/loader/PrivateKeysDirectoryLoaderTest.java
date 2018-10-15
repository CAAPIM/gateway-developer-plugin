/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@Extensions(@ExtendWith(TemporaryFolderExtension.class))
class PrivateKeysDirectoryLoaderTest {

    private TemporaryFolder temporaryFolder;
    private PrivateKeysDirectoryLoader loader = new PrivateKeysDirectoryLoader();

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        this.temporaryFolder = temporaryFolder;
    }

    @Test
    void loadBasedOnRoot() {
        final File directory = new File(temporaryFolder.getRoot(), "config/privateKeys");
        directory.mkdirs();

        Bundle bundle = new Bundle();
        loader.load(bundle, temporaryFolder.getRoot());

        assertNotNull(bundle.getPrivateKeysDirectory());
        assertEquals(directory.getPath(), bundle.getPrivateKeysDirectory());
    }

    @Test
    void loadBasedOnRoot_nonExistingFolder() {
        Bundle bundle = new Bundle();
        loader.load(bundle, temporaryFolder.getRoot());
        assertNull(bundle.getPrivateKeysDirectory());
    }

    @Test
    void loadBasedOnRoot_twice() {
        final File directory = new File(temporaryFolder.getRoot(), "config/privateKeys");
        directory.mkdirs();

        Bundle bundle = new Bundle();
        loader.load(bundle, temporaryFolder.getRoot());

        assertNotNull(bundle.getPrivateKeysDirectory());
        assertEquals(directory.getPath(), bundle.getPrivateKeysDirectory());

        assertThrows(BundleLoadException.class, () -> loader.load(bundle, temporaryFolder.getRoot()));
    }

    @Test
    void loadBasedOnEnv() {
        final File directory = new File(temporaryFolder.getRoot(), "privateKeys");
        directory.mkdirs();

        Bundle bundle = new Bundle();
        loader.load(bundle, "path", directory.getPath());

        assertNotNull(bundle.getPrivateKeysDirectory());
        assertEquals(directory.getPath(), bundle.getPrivateKeysDirectory());
    }

    @Test
    void loadBasedOnEnv_nonExistingFolder() {
        final File directory = new File(temporaryFolder.getRoot(), "privateKeys");
        assertThrows(BundleLoadException.class, () -> loader.load(new Bundle(), "path", directory.getPath()));
    }

    @Test
    void loadBasedOnEnv_twice() {
        final File directory = new File(temporaryFolder.getRoot(), "privateKeys");
        directory.mkdirs();

        Bundle bundle = new Bundle();
        loader.load(bundle, "path", directory.getPath());

        assertNotNull(bundle.getPrivateKeysDirectory());
        assertEquals(directory.getPath(), bundle.getPrivateKeysDirectory());

        assertThrows(BundleLoadException.class, () -> loader.load(bundle, "path", directory.getPath()));
    }
}