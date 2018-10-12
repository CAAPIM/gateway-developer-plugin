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
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

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

        // Properties with the keystore configuration
        // FIXME this is not being used yet
        Properties extraJavaArgs = new Properties();
        extraJavaArgs.put("com.l7tech.common.security.jceProviderEngine", "com.l7tech.security.prov.generic.GenericJceProviderEngine");
        extraJavaArgs.put("com.l7tech.keystore.type", keystoreHelper.getKeyStoreType());
        extraJavaArgs.put("com.l7tech.keystore.path", keyStoreFile.getAbsolutePath());
        extraJavaArgs.put("com.l7tech.keystore.savePath", "EMPTY");
        extraJavaArgs.put("com.l7tech.keystore.password", new String(keystoreHelper.getKeystorePassword()));

        File keyStorePropertiesFile = new File(keyStoreDirectory, "keystore.properties");
        stream = new StripFirstLineStream(fileUtils.getOutputStream(keyStorePropertiesFile));
        try {
            extraJavaArgs.store(stream, null);
        } catch (IOException e) {
            throw new KeyStoreCreationException("Unexpected error writing key store properties", e);
        } finally {
            closeQuietly(stream);
        }
    }

    /**
     * This is used in order to remove the first line when printing the properties to an output stream
     * that contains a date-timestamp. Inspired by https://stackoverflow.com/a/39043903/1108370
     */
    @SuppressWarnings("squid:S4349")
    private static class StripFirstLineStream extends FilterOutputStream {
        private boolean firstlineseen = false;

        StripFirstLineStream(final OutputStream out) {
            super(out);
        }

        @Override
        public void write(final int b) throws IOException {
            if (firstlineseen) {
                super.write(b);
            } else if (b == '\n') {
                firstlineseen = true;
            }
        }
    }
}
