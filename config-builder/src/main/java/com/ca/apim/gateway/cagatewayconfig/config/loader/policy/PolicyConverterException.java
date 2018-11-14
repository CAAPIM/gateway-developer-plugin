/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader.policy;

public class PolicyConverterException extends RuntimeException {
    public PolicyConverterException(String message) {
        super(message);
    }

    public PolicyConverterException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
