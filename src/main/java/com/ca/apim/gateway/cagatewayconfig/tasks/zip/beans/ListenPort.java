package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.util.List;
import java.util.Map;

public class ListenPort {

    public static final String TYPE = "LISTEN_PORT";

    private String id;
    private String name;
    private boolean enabled;
    private String protocol;
    private String interfaceTag;
    private int port;
    private List<String> enabledFeatures;
    // TODO TLS Settings
    private Map<String, Object> properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getInterfaceTag() {
        return interfaceTag;
    }

    public void setInterfaceTag(String interfaceTag) {
        this.interfaceTag = interfaceTag;
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
}
