/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;
import java.util.Map;
import java.util.Objects;

/**
 * Stored passwords.
 */
@Named("SECURE_PASSWORD")
public class StoredPasswordEntity implements Entity {

    private static final String PROPERTY_TYPE = "type";

    private final String id;
    private final String name;
    private final Map<String, Object> properties;

    private StoredPasswordEntity(Builder builder) {
        id = builder.id;
        name = builder.name;
        properties = builder.properties;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public boolean isType(Type type) {
        return properties != null && Objects.equals(properties.get(PROPERTY_TYPE), type.getName());
    }

    public enum Type {

        PASSWORD("Password"),
        PEM_PRIVATE_KEY("PEM Private Key");

        private String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class Builder {

        private String id;
        private String name;
        private Map<String, Object> properties;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public StoredPasswordEntity build() {
            return new StoredPasswordEntity(this);
        }
    }
}
