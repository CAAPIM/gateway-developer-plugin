/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;

import javax.inject.Named;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.PROPERTIES;

@Named("CLUSTER_PROPERTY")
@ConfigurationFile(name = "static", type = PROPERTIES)
@EnvironmentType("STATIC_PROPERTY")
public class ClusterProperty extends PropertiesEntity {

    public static final String CLUSTER_HOSTNAME_PROPERTY_NAME = "cluster.hostname";

    public ClusterProperty() {
    }

    public ClusterProperty(String name, String value) {
        setName(name);
        this.value = value;
    }

    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getKey() {
        return this.getName();
    }

    @Override
    public void setKey(String key) {
        setName(key);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getId() + ":" + getName();
    }
}
