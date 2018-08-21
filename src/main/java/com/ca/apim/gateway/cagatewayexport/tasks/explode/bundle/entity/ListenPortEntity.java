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

    private final String id;
    private final String name;
    private final String protocol;
    private final int port;
    private final List<String> enabledFeatures;
    private final ListenPortEntityTlsSettings tlsSettings;
    private final Map<String, Object> properties;

    public ListenPortEntity(String id, String name, String protocol, int port, List<String> enabledFeatures, ListenPortEntityTlsSettings tlsSettings, Map<String, Object> properties) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.port = port;
        this.enabledFeatures = enabledFeatures;
        this.tlsSettings = tlsSettings;
        this.properties = properties;
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
            return stream(values()).filter(c -> c.type.equals(type)).findFirst().get();
        }
    }

}
