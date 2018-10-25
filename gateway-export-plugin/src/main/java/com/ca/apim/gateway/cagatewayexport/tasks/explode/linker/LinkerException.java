/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

public class LinkerException extends RuntimeException {
    public LinkerException(String message) {
        super(message);
    }

    public LinkerException(String message, Exception e) {
        super(message, e);
    }

}
