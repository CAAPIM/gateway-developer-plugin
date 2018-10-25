/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Set;
import java.util.List;


import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ListenPortTlsSettings.*;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableList;

/**
 * Listen port representation for yaml/json files.
 */
@JsonInclude(NON_NULL)
public class ListenPort {

    public static final String PROTOCOL_HTTP = "HTTP";
    public static final String PROTOCOL_HTTPS = "HTTPS";
    public static final Integer HTTP_DEFAULT_PORT = 8080;
    public static final Integer HTTPS_DEFAULT_PORT = 8443;
    public static final String DEFAULT_HTTP_8080 = "Default HTTP (8080)";
    public static final String DEFAULT_HTTPS_8443 = "Default HTTPS (8443)";
    public static final List<String> DEFAULT_RECOMMENDED_CIPHERS = unmodifiableList(asList(
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
            "TLS_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
            "TLS_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA"));
    public static final List<String> TLS_VERSIONS = unmodifiableList(asList(TLSV1, TLSV11, TLSV12));

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

    public static class ListenPortTlsSettings {

        public static final String TLSV1 = "TLSv1";
        public static final String TLSV11 = "TLSv1.1";
        public static final String TLSV12 = "TLSv1.2";

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

        ClientAuthentication(String type) {
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
