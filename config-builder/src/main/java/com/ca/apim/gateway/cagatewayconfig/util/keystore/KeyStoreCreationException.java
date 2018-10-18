/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.keystore;

public class KeyStoreCreationException extends RuntimeException {

    public KeyStoreCreationException(String message) {
        super(message);
    }

    public KeyStoreCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
