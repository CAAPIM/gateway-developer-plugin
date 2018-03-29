/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.json;

public class JsonToolsException extends RuntimeException {
    public JsonToolsException(String message) {
        super(message);
    }

    public JsonToolsException(String message, Throwable cause) {
        super(message, cause);
    }
}
