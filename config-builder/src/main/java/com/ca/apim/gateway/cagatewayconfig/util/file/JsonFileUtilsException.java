/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.file;

public class JsonFileUtilsException extends RuntimeException {
    public JsonFileUtilsException(String message) {
        super(message);
    }

    public JsonFileUtilsException(String message, Throwable cause) {
        super(message, cause);
    }
}
