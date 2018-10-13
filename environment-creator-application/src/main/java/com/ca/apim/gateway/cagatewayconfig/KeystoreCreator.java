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

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.closeQuietly;
import static org.apache.commons.lang.StringUtils.isEmpty;

class KeystoreCreator {

    private static FileUtils fileUtils = FileUtils.INSTANCE;

    private KeystoreCreator() {
    }

    static void createKeyStoreIfNecessary(Map<String, String> environmentProperties, String keyStoreDirPath) {
        // write keystore if directory is specified and exists
        String privateKeyFilesDirectory = environmentProperties.get(KeystoreHelper.ENV_VAR_KEYSTORE_PATH);
        if (isEmpty(privateKeyFilesDirectory) || !Paths.get(privateKeyFilesDirectory).toFile().exists()) {
            return;
        }

        KeystoreHelper keystoreHelper = ConfigBuilderModule.getInjector().getInstance(KeystoreHelper.class);
        final byte[] keyStore = keystoreHelper.createKeyStoreFromEnvironment(privateKeyFilesDirectory, environmentProperties);
        // no keys
        if (keyStore.length == 0) {
            return;
        }

        final File keyStoreDirectory = new File(keyStoreDirPath);
        if (!keyStoreDirectory.exists() && !keyStoreDirectory.mkdirs()) {
            throw new KeyStoreCreationException("Could not create directory '" + keyStoreDirPath + "' for KeyStore");
        }

        File keyStoreFile = new File(keyStoreDirectory, "keystore.gwks");
        OutputStream stream = fileUtils.getOutputStream(keyStoreFile);
        try {
            IOUtils.write(keyStore, stream);
        } catch (IOException e) {
            throw new KeyStoreCreationException("Unexpected error writing key store", e);
        } finally {
            closeQuietly(stream);
        }

        updateSystemPropertiesFile(keystoreHelper, keyStoreFile);
    }

    private static void updateSystemPropertiesFile(KeystoreHelper keystoreHelper, File keyStoreFile) {
        File systemPropertiesFile = new File("/opt/SecureSpan/Gateway/node/default/etc/conf/system.properties");

        try (FileWriter fileWriter = new FileWriter(systemPropertiesFile, true); BufferedWriter br = new BufferedWriter(fileWriter);) {
            br.newLine();
            br.write("# Properties to configure a file based Keystore");
            br.write("com.l7tech.common.security.jceProviderEngineName=generic");
            br.newLine();
            br.write("com.l7tech.keystore.type=" + keystoreHelper.getKeyStoreType());
            br.newLine();
            br.write("com.l7tech.keystore.path=" + keyStoreFile.getAbsolutePath());
            br.newLine();
            br.write("com.l7tech.keystore.savePath=EMPTY");
            br.newLine();
            br.write("com.l7tech.keystore.password=" + new String(keystoreHelper.getKeystorePassword()));
        } catch (IOException e) {
            throw new KeyStoreCreationException("Unexpected error adding to system properties", e);
        }
    }
}
