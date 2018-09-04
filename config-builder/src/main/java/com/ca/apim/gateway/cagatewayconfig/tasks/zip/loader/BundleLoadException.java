/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

public class BundleLoadException extends RuntimeException {
    public BundleLoadException(String message) {
        super(message);
    }

    public BundleLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
