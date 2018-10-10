/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.injection.ConfigBuilderModule;
import com.ca.apim.gateway.cagatewayconfig.util.keystore.KeyStoreCreationException;
import com.ca.apim.gateway.cagatewayconfig.util.keystore.KeystoreHelper;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

class KeystoreCreator {

    private static FileUtils fileUtils = FileUtils.INSTANCE;

    private KeystoreCreator() {}

    static void createKeyStoreIfNecessary(Map<String, String> environmentProperties, String keyStoreDirPath) {
        // write keystore if directory is specified
        String privateKeyFilesDirectory = environmentProperties.get(KeystoreHelper.ENV_VAR_KEYSTORE_PATH);
        if (isNotEmpty(privateKeyFilesDirectory) && Paths.get(privateKeyFilesDirectory).toFile().exists()) {
            KeystoreHelper keystoreHelper = ConfigBuilderModule.getInjector().getInstance(KeystoreHelper.class);
            final byte[] keyStore = keystoreHelper.createKeyStoreFromEnvironment(privateKeyFilesDirectory, environmentProperties);

            final File keyStoreDirectory = new File(keyStoreDirPath);
            final boolean createdDirs = keyStoreDirectory.mkdirs();
            if (!createdDirs) {
                throw new KeyStoreCreationException("Could not create directory '" + keyStoreDirPath + "' for KeyStore");
            }

            OutputStream stream = fileUtils.getOutputStream(new File(keyStoreDirectory, "keystore.gwks"));
            try {
                IOUtils.write(keyStore, stream);
            } catch (IOException e) {
                throw new KeyStoreCreationException("Unexpected error writing key store", e);
            } finally {
                closeQuietly(stream);
            }
        }
    }
}
