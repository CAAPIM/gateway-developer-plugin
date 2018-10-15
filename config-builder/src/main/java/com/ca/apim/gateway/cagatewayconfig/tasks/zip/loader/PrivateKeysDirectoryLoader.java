/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;

import javax.inject.Singleton;
import java.io.File;

@Singleton
public class PrivateKeysDirectoryLoader implements EntityLoader {

    @Override
    public void load(Bundle bundle, File rootDir) {
        File privateKeysDir = new File(rootDir, "config/privateKeys");
        if (directoryDoesNotExist(privateKeysDir)) {
            return;
        }
        setDirectory(bundle, privateKeysDir.getPath());
    }

    @Override
    public void load(Bundle bundle, String name, String value) {
        if (directoryDoesNotExist(new File(value))) {
            throw new BundleLoadException("Directory specified for private keys does not exist");
        }
        setDirectory(bundle, value);
    }

    private static void setDirectory(Bundle bundle, String value) {
        if (bundle.getPrivateKeysDirectory() != null) {
            throw new BundleLoadException("Private Keys directory path is already set to " + bundle.getPrivateKeysDirectory());
        }
        bundle.setPrivateKeysDirectory(value);
    }

    private static boolean directoryDoesNotExist(File directory) {
        return !directory.exists();
    }

    @Override
    public String getEntityType() {
        return "PRIVATE_KEYS_DIRECTORY";
    }
}
