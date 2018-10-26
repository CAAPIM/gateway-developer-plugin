/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;

@Named("CLUSTER_PROPERTY")
public class ClusterProperty implements Entity {
    public static final String CLUSTER_HOSTNAME_PROPERTY_NAME = "cluster.hostname";

    private final String name;
    private final String id;
    private final String value;

    public ClusterProperty(final String name, String value, final String id) {
        this.name = name;
        this.value = value;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return id + ":" + name;
    }
}
