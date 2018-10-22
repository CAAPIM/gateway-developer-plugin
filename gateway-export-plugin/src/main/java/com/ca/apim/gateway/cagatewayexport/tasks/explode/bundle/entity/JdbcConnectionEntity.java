/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;
import java.util.Map;

/**
 * Mapping for JDBC Connections.
 */
@Named("JDBC_CONNECTION")
@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
public class JdbcConnectionEntity implements Entity {

    private final String id;
    private final String name;
    private final String driverClass;
    private final String jdbcUrl;
    private final String user;
    private final Integer minimumPoolSize;
    private final Integer maximumPoolSize;
    private String passwordRef;
    private final Map<String, Object> properties;

    private JdbcConnectionEntity(Builder builder) {
        id = builder.id;
        name = builder.name;
        driverClass = builder.driverClass;
        jdbcUrl = builder.jdbcUrl;
        properties = builder.properties;
        user = builder.user;
        minimumPoolSize = builder.minimumPoolSize;
        maximumPoolSize = builder.maximumPoolSize;
        passwordRef = builder.passwordRef;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPasswordRef() {
        return passwordRef;
    }

    public void setPasswordRef(String passwordRef) {
        this.passwordRef = passwordRef;
    }

    public Integer getMinimumPoolSize() {
        return minimumPoolSize;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public static class Builder {

        private String id;
        private String name;
        private String driverClass;
        private String jdbcUrl;
        private String user;
        private String passwordRef;
        private Integer minimumPoolSize;
        private Integer maximumPoolSize;
        private Map<String, Object> properties;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder driverClass(String driverClass) {
            this.driverClass = driverClass;
            return this;
        }

        public Builder jdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder passwordRef(String passwordRef) {
            this.passwordRef = passwordRef;
            return this;
        }

        public Builder minimumPoolSize(Integer minimumPoolSize) {
            this.minimumPoolSize = minimumPoolSize;
            return this;
        }

        public Builder maximumPoolSize(Integer maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
            return this;
        }

        public JdbcConnectionEntity build() {
            return new JdbcConnectionEntity(this);
        }
    }
}
