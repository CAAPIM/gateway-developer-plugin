/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;

import javax.inject.Named;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.PROPERTIES;

@Named("CLUSTER_PROPERTY")
@ConfigurationFile(name = "static", type = PROPERTIES)
public class ClusterProperty extends GatewayEntity implements PropertiesEntity {

    public static final String CLUSTER_HOSTNAME_PROPERTY_NAME = "cluster.hostname";

    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getKey() {
        return this.getName();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getId() + ":" + getName();
    }
}
