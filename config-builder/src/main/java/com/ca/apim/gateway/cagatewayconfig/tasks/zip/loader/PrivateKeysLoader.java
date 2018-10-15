/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;

import javax.inject.Singleton;
import java.io.File;

import static java.nio.file.Files.newInputStream;
import static java.util.Arrays.stream;
import static org.apache.commons.io.FilenameUtils.getBaseName;

@Singleton
public class PrivateKeysLoader implements EntityLoader {

    @Override
    public void load(Bundle bundle, File rootDir) {
        File privateKeysDir = new File(rootDir, "config/privateKeys");
        if (directoryDoesNotExist(privateKeysDir)) {
            return;
        }
        loadFromDirectory(bundle, privateKeysDir);
    }

    @Override
    public void load(Bundle bundle, String name, String value) {
        File privateKeysDir = new File(value);
        if (directoryDoesNotExist(privateKeysDir)) {
            throw new BundleLoadException("Directory specified for private keys does not exist");
        }
        loadFromDirectory(bundle, privateKeysDir);
    }

    private static void loadFromDirectory(Bundle bundle, File privateKeysDirectory) {
        final File[] files = privateKeysDirectory.listFiles(file -> file.getName().endsWith(".p12"));
        stream(files).forEach(f -> bundle.getPrivateKeyFiles().put(getBaseName(f.getName()), () -> newInputStream(f.toPath())));
    }

    private static boolean directoryDoesNotExist(File directory) {
        return !directory.exists();
    }

    @Override
    public String getEntityType() {
        return "PRIVATE_KEYS_DIRECTORY";
    }
}
