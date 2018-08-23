/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.gw7;

import java.io.IOException;

public class PackageBuildException extends RuntimeException {
    public PackageBuildException(String message, IOException throwable) {
        super(message, throwable);
    }
}
