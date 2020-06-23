package com.ca.apim.gateway.cagatewayconfig.beans;

import java.util.List;
import java.util.Map;

public class EnvironmentBundleData {
    private String type;
    private String name;
    private String version;
    private List<Map<String, String>> referencedEntities;

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

    public List<Map<String, String>> getReferencedEntities() {
        return referencedEntities;
    }

    public void setReferencedEntities(List<Map<String, String>> referencedEntities) {
        this.referencedEntities = referencedEntities;
    }
}
