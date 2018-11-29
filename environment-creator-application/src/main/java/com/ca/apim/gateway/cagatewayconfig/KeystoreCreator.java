/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.injection.ConfigBuilderModule;
import com.ca.apim.gateway.cagatewayconfig.util.keystore.KeyStoreCreationException;
import com.ca.apim.gateway.cagatewayconfig.util.keystore.KeystoreHelper;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Collection;

import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.closeQuietly;

class KeystoreCreator {

    static final String KEYSTORE_FILE_NAME = "keystore.gwks";

    private KeystoreCreator() {
    }

    static void createKeyStoreIfNecessary(String keyStoreDirPath, String privateKeyFolderPath, Collection<PrivateKey> privateKeys, FileUtils fileUtils, String systemPropertiesPath) {
        PrivateKey.loadFromDirectory(privateKeys, new File(privateKeyFolderPath), true);

        KeystoreHelper keystoreHelper = ConfigBuilderModule.getInjector().getInstance(KeystoreHelper.class);
        final byte[] keyStore = keystoreHelper.createKeyStore(privateKeys);
        // no keys
        if (keyStore.length == 0) {
            return;
        }

        final File keyStoreDirectory = new File(keyStoreDirPath);
        if (!keyStoreDirectory.exists() && !keyStoreDirectory.mkdirs()) {
            throw new KeyStoreCreationException("Could not create directory '" + keyStoreDirPath + "' for KeyStore");
        }

        File keyStoreFile = new File(keyStoreDirectory, KEYSTORE_FILE_NAME);
        OutputStream stream = fileUtils.getOutputStream(keyStoreFile);
        try {
            IOUtils.write(keyStore, stream);
        } catch (IOException e) {
            throw new KeyStoreCreationException("Unexpected error writing key store", e);
        } finally {
            closeQuietly(stream);
        }

        updateSystemPropertiesFile(keystoreHelper, keyStoreFile, systemPropertiesPath);
    }

    static void updateSystemPropertiesFile(KeystoreHelper keystoreHelper, File keyStoreFile, String systemPropertiesPath) {
        File systemPropertiesFile = new File(systemPropertiesPath);

        try (FileWriter fileWriter = new FileWriter(systemPropertiesFile, true);
             BufferedWriter br = new BufferedWriter(fileWriter)
        ) {
            br.newLine();
            br.write("# Properties to configure a file based Keystore");
            br.newLine();
            br.write("com.l7tech.common.security.jceProviderEngineName=generic");
            br.newLine();
            br.write("com.l7tech.keystore.type=" + keystoreHelper.getKeyStoreType());
            br.newLine();
            br.write("com.l7tech.keystore.path=" + PathUtils.unixPath(keyStoreFile.getAbsolutePath()));
            br.newLine();
            br.write("com.l7tech.keystore.savePath=EMPTY");
            br.newLine();
            br.write("com.l7tech.keystore.password=" + new String(keystoreHelper.getKeystorePassword()));
            br.newLine();
            br.write("com.l7tech.common.security.jceProviderEngine=com.l7tech.security.prov.generic.GenericJceProviderEngine");
        } catch (IOException e) {
            throw new KeyStoreCreationException("Unexpected error adding to system properties", e);
        }
    }
}
