/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;

public class ListenPortEntity implements Entity {

    public static final List<Integer> DEFAULT_PORTS = Arrays.asList(8080, 8443, 9443, 2124);

    private String id;
    private String name;
    private String protocol;
    private int port;
    private List<String> enabledFeatures;
    private ListenPortEntityTlsSettings tlsSettings;
    private Map<String, Object> properties;
    private String targetServiceReference;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getPort() {
        return port;
    }

    public List<String> getEnabledFeatures() {
        return enabledFeatures;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public ListenPortEntityTlsSettings getTlsSettings() {
        return tlsSettings;
    }

    public String getTargetServiceReference() {
        return targetServiceReference;
    }

    public ListenPortEntity setId(String id) {
        this.id = id;
        return this;
    }

    public ListenPortEntity setName(String name) {
        this.name = name;
        return this;
    }

    public ListenPortEntity setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public ListenPortEntity setPort(int port) {
        this.port = port;
        return this;
    }

    public ListenPortEntity setEnabledFeatures(List<String> enabledFeatures) {
        this.enabledFeatures = enabledFeatures;
        return this;
    }

    public ListenPortEntity setTlsSettings(ListenPortEntityTlsSettings tlsSettings) {
        this.tlsSettings = tlsSettings;
        return this;
    }

    public ListenPortEntity setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public ListenPortEntity setTargetServiceReference(String targetServiceReference) {
        this.targetServiceReference = targetServiceReference;
        return this;
    }

    public static class ListenPortEntityTlsSettings {

        private final ClientAuthentication clientAuthentication;
        private final List<String> enabledVersions;
        private final List<String> enabledCipherSuites;
        private final Map<String, Object> properties;

        public ListenPortEntityTlsSettings(ClientAuthentication clientAuthentication, List<String> enabledVersions, List<String> enabledCipherSuites, Map<String, Object> properties) {
            this.clientAuthentication = clientAuthentication;
            this.enabledVersions = enabledVersions;
            this.enabledCipherSuites = enabledCipherSuites;
            this.properties = properties;
        }

        public ClientAuthentication getClientAuthentication() {
            return clientAuthentication;
        }

        public List<String> getEnabledVersions() {
            return enabledVersions;
        }

        public List<String> getEnabledCipherSuites() {
            return enabledCipherSuites;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }
    }

    public enum ClientAuthentication {

        NONE("None"), OPTIONAL("Optional"), REQUIRED("Required");

        private String type;

        private ClientAuthentication(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public static ClientAuthentication fromType(String type) {
            return stream(values()).filter(c -> c.type.equals(type)).findFirst().orElse(null);
        }
    }

}
