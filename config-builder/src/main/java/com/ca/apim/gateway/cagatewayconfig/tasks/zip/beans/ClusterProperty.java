/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import javax.inject.Named;

@Named("CLUSTER_PROPERTY")
public class ClusterProperty extends GatewayEntity {

    public static final String CLUSTER_HOSTNAME_PROPERTY_NAME = "cluster.hostname";

    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getId() + ":" + getName();
    }
}
