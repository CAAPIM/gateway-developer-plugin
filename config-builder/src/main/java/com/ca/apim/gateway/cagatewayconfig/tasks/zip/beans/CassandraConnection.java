/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.inject.Named;
import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
@Named("CASSANDRA_CONFIGURATION")
public class CassandraConnection extends GatewayEntity {

    private String keyspace;
    private String contactPoint;
    private Integer port;
    private String username;
    private String passwordId;
    private String storedPasswordName;
    private String compression;
    private Boolean ssl;
    private Set<String> tlsCiphers;
    private Map<String, Object> properties;

    public CassandraConnection() {}

    private CassandraConnection(Builder builder) {
        setName(builder.name);
        setId(builder.id);
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

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getContactPoint() {
        return contactPoint;
    }

    public void setContactPoint(String contactPoint) {
        this.contactPoint = contactPoint;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStoredPasswordName() {
        return storedPasswordName;
    }

    public void setStoredPasswordName(String storedPasswordName) {
        this.storedPasswordName = storedPasswordName;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    public Set<String> getTlsCiphers() {
        return tlsCiphers;
    }

    public void setTlsCiphers(Set<String> tlsCiphers) {
        this.tlsCiphers = tlsCiphers;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getPasswordId() {
        return passwordId;
    }

    public void setPasswordId(String passwordId) {
        this.passwordId = passwordId;
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

        public CassandraConnection build() {
            return new CassandraConnection(this);
        }
    }
}
