/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

public class MissingEnvironmentException extends RuntimeException {
    public MissingEnvironmentException(String message) {
        super(message);
    }

    public MissingEnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
