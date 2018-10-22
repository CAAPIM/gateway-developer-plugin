/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
public class CassandraConnection {

    private String keyspace;
    private String contactPoint;
    private Integer port;
    private String username;
    private String storedPasswordName;
    private String compression;
    private Boolean ssl;
    private Set<String> tlsCiphers;
    private Map<String, Object> properties;

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
}