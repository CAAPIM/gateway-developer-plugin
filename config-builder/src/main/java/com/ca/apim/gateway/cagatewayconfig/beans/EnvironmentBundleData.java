package com.ca.apim.gateway.cagatewayconfig.beans;

import java.util.List;
import java.util.Map;

public class EnvironmentBundleData {
    private String type;
    private String name;
    private String version;
    private Boolean environmentIncluded;
    private List<Map<String, String>> environmentEntities;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isEnvironmentIncluded() {
        return environmentIncluded;
    }

    public void setEnvironmentIncluded(Boolean environmentIncluded) {
        this.environmentIncluded = environmentIncluded;
    }

    public List<Map<String, String>> getEnvironmentEntities() {
        return environmentEntities;
    }

    public void setEnvironmentEntities(List<Map<String, String>> environmentEntities) {
        this.environmentEntities = environmentEntities;
    }
}
