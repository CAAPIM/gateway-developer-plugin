/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.inject.Named;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * JDBC Connection for yaml and json files
 */
@JsonInclude(NON_NULL)
@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
@Named("JDBC_CONNECTION")
@ConfigurationFile(name = "jdbc-connections", type = JSON_YAML)
public class JdbcConnection extends GatewayEntity {

    private String driverClass;
    private String jdbcUrl;
    private Map<String, Object> properties;
    private String user;
    private String passwordRef;
    private Integer minimumPoolSize;
    private Integer maximumPoolSize;
    private String password;

    public JdbcConnection() {
    }

    private JdbcConnection(Builder builder) {
        setId(builder.id);
        setName(builder.name);
        driverClass = builder.driverClass;
        jdbcUrl = builder.jdbcUrl;
        properties = builder.properties;
        user = builder.user;
        minimumPoolSize = builder.minimumPoolSize;
        maximumPoolSize = builder.maximumPoolSize;
        passwordRef = builder.passwordRef;
        password = builder.password;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPasswordRef() {
        return passwordRef;
    }

    public void setPasswordRef(String passwordRef) {
        this.passwordRef = passwordRef;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getMinimumPoolSize() {
        return minimumPoolSize;
    }

    public void setMinimumPoolSize(Integer minimumPoolSize) {
        this.minimumPoolSize = minimumPoolSize;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public static class Builder {

        private String id;
        private String name;
        private String driverClass;
        private String jdbcUrl;
        private String user;
        private String passwordRef;
        private String password;
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

        public Builder password(String password) {
            this.password = password;
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

        public JdbcConnection build() {
            return new JdbcConnection(this);
        }
    }
}
