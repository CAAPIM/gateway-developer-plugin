package com.ca.apim.gateway.cagatewayconfig.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Set;

/**
 * Metadata file for Policy entities
 */

@JsonInclude
public class PolicyMetadata {
    @JsonIgnore
    private String name;
    @JsonIgnore
    private String path;
    private String type;
    private String tag;
    private String subtag;
    private Set<Dependency> usedEntities;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<Dependency> getUsedEntities() {
        return usedEntities;
    }

    public void setUsedEntities(Set<Dependency> usedEntities) {
        this.usedEntities = usedEntities;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSubtag() {
        return subtag;
    }

    public void setSubtag(String subtag) {
        this.subtag = subtag;
    }

    @JsonIgnore
    public String getNameWithPath() {
        return path == null || path.isEmpty() ? name : path + "/" + name;
    }

}
