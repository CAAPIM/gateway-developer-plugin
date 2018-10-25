/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

public class PropertyFileException extends RuntimeException {
    public PropertyFileException(String message) {
        super(message);
    }

    public PropertyFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
