/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;

public class ListenPort {

    public static final String TYPE = "SSG_CONNECTOR";
    public static final String PROTOCOL_HTTP = "HTTP";
    public static final String PROTOCOL_HTTPS = "HTTPS";
    public static final Integer HTTP_DEFAULT_PORT = 8080;
    public static final Integer HTTPS_DEFAULT_PORT = 8443;

    private String protocol;
    private int port;
    private List<String> enabledFeatures;
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

    public List<String> getEnabledFeatures() {
        return enabledFeatures;
    }

    public void setEnabledFeatures(List<String> enabledFeatures) {
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

    public static class ListenPortTlsSettings {

        private ClientAuthentication clientAuthentication;
        private List<String> enabledVersions;
        private List<String> enabledCipherSuites;
        private Map<String, Object> properties;

        public ClientAuthentication getClientAuthentication() {
            return clientAuthentication;
        }

        public void setClientAuthentication(ClientAuthentication clientAuthentication) {
            this.clientAuthentication = clientAuthentication;
        }

        public List<String> getEnabledVersions() {
            return enabledVersions;
        }

        public void setEnabledVersions(List<String> enabledVersions) {
            this.enabledVersions = enabledVersions;
        }

        public List<String> getEnabledCipherSuites() {
            return enabledCipherSuites;
        }

        public void setEnabledCipherSuites(List<String> enabledCipherSuites) {
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

        public static ClientAuthentication fromType(String type) {
            return stream(values()).filter(c -> c.type.equals(type)).findFirst().orElse(null);
        }
    }

    /**
     * Store features and its descriptions.
     */
    public enum Feature {

        MESSAGE_INPUT("Published service message input"),
        ADMIN_REMOTE_SSM("Policy Manager access"),
        ADMIN_REMOTE_ESM("Enterprise Manager access"),
        ADMIN_REMOTE("Administrative access"),
        ADMIN_APPLET("Browser-based administration"),
        POLICYDISCO("Policy download service"),
        PING("Ping service"),
        STS("WS-Trust security token service"),
        CSRHANDLER("Certificate signing service"),
        PASSWD("Password changing service"),
        WSDLPROXY("WSDL download service"),
        SNMPQUERY("SNMP Query service"),
        OTHER_SERVLETS("Built-in services"),
        PC_NODE_API("Node Control"),
        NODE_COMMUNICATION("Inter-Node Communication");

        private final String description;

        Feature(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

    }
}