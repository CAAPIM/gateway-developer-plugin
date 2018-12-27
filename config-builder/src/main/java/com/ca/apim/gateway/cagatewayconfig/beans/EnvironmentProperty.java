/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

public abstract class EnvironmentProperty extends PropertiesEntity {

    private String value;

    public EnvironmentProperty() { }

    public EnvironmentProperty(final String name, final String value) {
        this.setName(name);
        this.setId(name);
        this.value = value;
    }

    @Override
    public abstract String getKey();

    @Override
    public void setKey(String key) {
        setName(key);
    }

    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getId();
    }
}
