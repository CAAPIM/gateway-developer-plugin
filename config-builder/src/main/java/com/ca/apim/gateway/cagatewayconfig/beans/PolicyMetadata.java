package com.ca.apim.gateway.cagatewayconfig.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

/**
 * Metadata file for Policy entities
 */

@JsonInclude
public class PolicyMetadata {
    private String path;
    private Set<Dependency> dependencies;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
}
