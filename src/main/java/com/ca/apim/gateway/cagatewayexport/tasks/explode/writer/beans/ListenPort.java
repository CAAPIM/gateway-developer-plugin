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

/**
 * Listen port representation for yaml/json files.
 */
@JsonInclude(NON_NULL)
public class ListenPort {

    private String protocol;
    private int port;
    private Set<String> enabledFeatures;
    private ListenPortTlsSettings tlsSettings;
    private Map<String, Object> properties;
    private String targetServiceReference;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Set<String> getEnabledFeatures() {
        return enabledFeatures;
    }

    public void setEnabledFeatures(Set<String> enabledFeatures) {
        this.enabledFeatures = enabledFeatures;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public ListenPortTlsSettings getTlsSettings() {
        return tlsSettings;
    }

    public void setTlsSettings(ListenPortTlsSettings tlsSettings) {
        this.tlsSettings = tlsSettings;
    }

    public String getTargetServiceReference() {
        return targetServiceReference;
    }

    public void setTargetServiceReference(String targetServiceReference) {
        this.targetServiceReference = targetServiceReference;
    }

    @JsonInclude(NON_NULL)
    public static class ListenPortTlsSettings {

        private ClientAuthentication clientAuthentication;
        private Set<String> enabledVersions;
        private Set<String> enabledCipherSuites;
        private Map<String, Object> properties;

        public ClientAuthentication getClientAuthentication() {
            return clientAuthentication;
        }

        public void setClientAuthentication(ClientAuthentication clientAuthentication) {
            this.clientAuthentication = clientAuthentication;
        }

        public Set<String> getEnabledVersions() {
            return enabledVersions;
        }

        public void setEnabledVersions(Set<String> enabledVersions) {
            this.enabledVersions = enabledVersions;
        }

        public Set<String> getEnabledCipherSuites() {
            return enabledCipherSuites;
        }

        public void setEnabledCipherSuites(Set<String> enabledCipherSuites) {
            this.enabledCipherSuites = enabledCipherSuites;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
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
    }

}
