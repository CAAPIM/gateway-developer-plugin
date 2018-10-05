/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.io.File;

public class PrivateKey {

    private String keystore;
    private String algorithm;
    private KeyStoreType keyStoreType;
    private File privateKeyDirectory;

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public KeyStoreType getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(KeyStoreType keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public File getPrivateKeyDirectory() {
        return privateKeyDirectory;
    }

    public void setPrivateKeyDirectory(File privateKeyDirectory) {
        this.privateKeyDirectory = privateKeyDirectory;
    }
}
