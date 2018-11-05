/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

public class EntityBuilderException extends RuntimeException {
    public EntityBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityBuilderException(String message) {
        super(message);
    }
}
