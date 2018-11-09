/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.Nullable;

import javax.inject.Named;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.PROPERTIES;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Define stored passwords.
 */
@JsonInclude(NON_NULL)
@Named("SECURE_PASSWORD")
@ConfigurationFile(name = "stored-passwords", type = PROPERTIES)
@EnvironmentType("PASSWORD")
public class StoredPassword extends PropertiesEntity {

    private static final String PROPERTY_DESCRIPTION = "description";
    private static final String PROPERTY_TYPE = "type";
    private static final String PROPERTY_USAGE_FROM_VARIABLE = "usageFromVariable";

    private String password;
    private Map<String, Object> properties;

    public StoredPassword() {
    }

    private StoredPassword(Builder builder) {
        setId(builder.id);
        setName(builder.name);
        properties = builder.properties;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public void postLoad(String entityKey, Bundle bundle, @Nullable File rootFolder, IdGenerator idGenerator) {
        addDefaultProperties();
    }

    public void addDefaultProperties() {
        this.properties = fillDefaultProperties(this.getName(), this.properties);
    }

    public static Map<String, Object> fillDefaultProperties(String name, Map<String, Object> properties) {
        if (properties == null) {
            properties = new HashMap<>();
        }

        properties.putIfAbsent(PROPERTY_DESCRIPTION, name);
        properties.putIfAbsent(PROPERTY_TYPE, Type.PASSWORD.getName());
        properties.putIfAbsent(PROPERTY_USAGE_FROM_VARIABLE, true);

        return properties;
    }

    @Override
    public String getKey() {
        return getName();
    }

    @Override
    public void setKey(String key) {
        setName(key);
    }

    @Override
    public String getValue() {
        return EMPTY;
    }

    @Override
    public void setValue(String value) {
        setPassword(value);
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

    public boolean isType(Type type) {
        return properties != null && Objects.equals(properties.get(PROPERTY_TYPE), type.getName());
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

        public StoredPassword build() {
            return new StoredPassword(this);
        }
    }
}
