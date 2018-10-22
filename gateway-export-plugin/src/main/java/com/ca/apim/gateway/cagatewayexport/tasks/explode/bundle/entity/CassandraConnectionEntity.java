/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;
import java.util.Map;
import java.util.Set;

@Named("CASSANDRA_CONFIGURATION")
public class CassandraConnectionEntity implements Entity {

    private final String name;
    private final String id;
    private final String keyspace;
    private final String contactPoint;
    private final Integer port;
    private final String username;
    private final String passwordId;
    private String passwordName;
    private final String compression;
    private final Boolean ssl;
    private final Set<String> tlsCiphers;
    private final Map<String, Object> properties;

    private CassandraConnectionEntity(Builder builder) {
        name = builder.name;
        id = builder.id;
        contactPoint = builder.contactPoint;
        port = builder.port;
        username = builder.username;
        passwordId = builder.passwordId;
        compression = builder.compression;
        ssl = builder.ssl;
        tlsCiphers = builder.tlsCiphers;
        properties = builder.properties;
        keyspace = builder.keyspace;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public String getContactPoint() {
        return contactPoint;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordId() {
        return passwordId;
    }

    public String getPasswordName() {
        return passwordName;
    }

    public void setPasswordName(String passwordName) {
        this.passwordName = passwordName;
    }

    public String getCompression() {
        return compression;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public Set<String> getTlsCiphers() {
        return tlsCiphers;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public static class Builder {

        private String name;
        private String id;
        private String keyspace;
        private String contactPoint;
        private Integer port;
        private String username;
        private String passwordId;
        private String compression;
        private Boolean ssl;
        private Set<String> tlsCiphers;
        private Map<String, Object> properties;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder contactPoint(String contactPoint) {
            this.contactPoint = contactPoint;
            return this;
        }

        public Builder port(Integer port) {
            this.port = port;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder passwordId(String passwordId) {
            this.passwordId = passwordId;
            return this;
        }

        public Builder compression(String compression) {
            this.compression = compression;
            return this;
        }

        public Builder ssl(Boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        public Builder tlsCiphers(Set<String> tlsCiphers) {
            this.tlsCiphers = tlsCiphers;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public Builder keyspace(String keyspace) {
            this.keyspace = keyspace;
            return this;
        }

        public CassandraConnectionEntity build() {
            return new CassandraConnectionEntity(this);
        }
    }
}
