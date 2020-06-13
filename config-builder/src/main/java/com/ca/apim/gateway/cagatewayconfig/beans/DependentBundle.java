package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.Metadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.File;
@JsonPropertyOrder({"type", "name", "groupName", "version"})
public class DependentBundle implements Metadata {
    private String type;
    private String name;
    private String groupName;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String version;
    @JsonIgnore
    private File dependencyFile;

    public DependentBundle(File dependencyFile) {
        this.dependencyFile = dependencyFile;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    @Override
    public String getId() {
        return null;
    }

    @JsonIgnore
    @Override
    public String getGuid() {
        return null;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public File getDependencyFile() {
        return dependencyFile;
    }

    public void setDependencyFile(File dependencyFile) {
        this.dependencyFile = dependencyFile;
    }
}
