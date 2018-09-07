/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;

@Named("SSG_CONNECTOR")
public class ListenPortEntity implements Entity {

    public static final List<Integer> DEFAULT_PORTS = unmodifiableList(asList(8080, 8443, 9443, 2124));

    private final String id;
    private final String name;
    private final String protocol;
    private final int port;
    private final List<String> enabledFeatures;
    private final ListenPortEntityTlsSettings tlsSettings;
    private final Map<String, Object> properties;
    private String targetServiceReference;

    private ListenPortEntity(final Builder builder) {
        id = builder.id;
        name = builder.name;
        protocol = builder.protocol;
        port = builder.port;
        enabledFeatures = builder.enabledFeatures;
        tlsSettings = builder.tlsSettings;
        properties = builder.properties;
        targetServiceReference = builder.targetServiceReference;
    }

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

    public void setTargetServiceReference(String targetServiceReference) {
        this.targetServiceReference = targetServiceReference;
    }

    public static class Builder {

        private String id;
        private String name;
        private String protocol;
        private int port;
        private List<String> enabledFeatures;
        private ListenPortEntityTlsSettings tlsSettings;
        private Map<String, Object> properties;
        private String targetServiceReference;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder enabledFeatures(List<String> enabledFeatures) {
            this.enabledFeatures = enabledFeatures;
            return this;
        }

        public Builder tlsSettings(ListenPortEntityTlsSettings tlsSettings) {
            this.tlsSettings = tlsSettings;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public Builder targetServiceReference(String targetServiceReference) {
            this.targetServiceReference = targetServiceReference;
            return this;
        }

        public ListenPortEntity build () {
            return new ListenPortEntity(this);
        }
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
