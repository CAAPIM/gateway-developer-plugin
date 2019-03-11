/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.injection;

/**
 * Error during configuration of the injection bindings.
 */
public class InjectionConfigurationException extends RuntimeException {

    public InjectionConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
