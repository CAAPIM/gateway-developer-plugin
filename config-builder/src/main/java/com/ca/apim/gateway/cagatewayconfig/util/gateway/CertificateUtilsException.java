/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

public class CertificateUtilsException extends RuntimeException {
    public CertificateUtilsException(String message) {
        super(message);
    }

    public CertificateUtilsException(String message, Throwable cause) {
        super(message, cause);
    }
}
