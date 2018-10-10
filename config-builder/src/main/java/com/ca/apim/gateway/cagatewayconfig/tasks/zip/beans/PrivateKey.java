/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.ca.apim.gateway.cagatewayconfig.util.file.SupplierWithIO;

import java.io.InputStream;

@SuppressWarnings("squid:S2068") // sonarcloud believes 'password' field names may have hardcoded passwords
public class PrivateKey {

    private String alias;
    private String keystore;
    private String algorithm;
    private String keyPassword;
    private KeyStoreType keyStoreType;
    private SupplierWithIO<InputStream> privateKeyFile;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

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

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public SupplierWithIO<InputStream> getPrivateKeyFile() {
        return privateKeyFile;
    }

    public void setPrivateKeyFile(SupplierWithIO privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }
}
