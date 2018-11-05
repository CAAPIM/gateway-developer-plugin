/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

public class EnvironmentProperty extends GatewayEntity {

    private final String value;
    private final Type type;

    public EnvironmentProperty(final String name, final String value, final Type type) {
        this.setName(name);
        this.value = value;
        this.type = type;
        this.setId(type + ":" + name);
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return getId();
    }

    public enum Type {
        LOCAL, GLOBAL, SERVICE
    }
}
