/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.util.HashMap;
import java.util.Map;

/**
 * Define stored passwords.
 */
public class StoredPassword {

    private static final String PROPERTY_DESCRIPTION = "description";
    private static final String PROPERTY_TYPE = "type";
    private static final String PROPERTY_USAGE_FROM_VARIABLE = "usageFromVariable";

    private String id;
    private String name;
    private String password;
    private Map<String, Object> properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void addDefaultProperties() {
        this.properties = fillDefaultProperties(this.name, this.properties);
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
}
