/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

/**
 * An entity that can be represented by a key-value pair in a properties file
 */
public interface PropertiesEntity {

    String getKey();

    String getValue();
}
