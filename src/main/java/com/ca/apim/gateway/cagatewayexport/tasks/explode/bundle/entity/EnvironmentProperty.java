/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

public class EnvironmentProperty implements Entity {
    private final String name;
    private final String id;
    private final String value;
    private final Type type;

    public EnvironmentProperty(final String name, final String value, final Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.id = type + ":" + name;
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

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return id;
    }

    public enum Type {
        LOCAL, GLOBAL
    }
}
