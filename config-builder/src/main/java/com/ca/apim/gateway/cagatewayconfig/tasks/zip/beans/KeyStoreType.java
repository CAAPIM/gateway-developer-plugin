/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.util.stream.Stream;

/**
 * Possible types of key stores where a PrivateKey can be stored.
 */
public enum KeyStoreType {

    GENERIC("00000000000000000000000000000005", "Generic"),
    PKCS12_SOFTWARE("00000000000000000000000000000002", "Software DB"),
    PKCS11_HARDWARE("00000000000000000000000000000001", "HSM"),
    LUNA_HARDWARE("00000000000000000000000000000003", "SafeNet HSM"),
    NCIPHER_HARDWARE("00000000000000000000000000000004", "nCipher HSM");

    private String id;
    private String name;

    KeyStoreType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String generateKeyId(String keyAlias) {
        return this.id + ":" + keyAlias;
    }

    public static KeyStoreType fromName(String name) {
        return Stream.of(values()).filter(k -> k.name.equals(name)).findFirst().orElse(GENERIC);
    }
}