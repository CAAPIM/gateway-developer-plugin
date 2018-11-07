/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.spec;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the configuration file name and format for an entity.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface ConfigurationFile {

    String name();

    FileType type();

    enum FileType {

        PROPERTIES,
        JSON_YAML

    }
}
