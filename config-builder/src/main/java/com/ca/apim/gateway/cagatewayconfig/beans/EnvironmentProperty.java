/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.EntityConfigException;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;

import javax.inject.Named;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.PROPERTIES;

@Named("ENVIRONMENT_PROPERTY")
@ConfigurationFile(name = "env", type = PROPERTIES)
@EnvironmentType("PROPERTY")
public class EnvironmentProperty extends PropertiesEntity {

    private String value;
    private Type type;

    public EnvironmentProperty() { }

    public EnvironmentProperty(final String name, final String value, final Type type) {
        this.setName(name);
        this.value = value;
        this.type = type;
        this.setId(type + ":" + name);
    }

    @Override
    public String getKey() {
        switch (type) {
            case LOCAL:
                return getName();
            case GLOBAL:
                return "gateway." + getName();
            case SERVICE:
                return "service.property." + getName();
            default:
                throw new EntityConfigException("Unknown Environment Property Type: " + getType());
        }
    }

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
