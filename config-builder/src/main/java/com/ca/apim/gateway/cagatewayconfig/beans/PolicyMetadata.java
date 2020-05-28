package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotationDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import org.apache.commons.lang3.StringUtils;

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
    @JsonDeserialize(using = AnnotationDeserializer.class)
    private Set<Annotation> annotations;


    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Set<Annotation> annotations) {
        this.annotations = annotations;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    @JsonIgnore
    public String getFullPath() {
        return StringUtils.isEmpty(getPath()) ? getName() : PathUtils.unixPath(getPath(), getName());
    }

    public void setFullPath(final String fullPath) {
        final int index = fullPath.lastIndexOf("/");
        if (index != -1) {
            setName(fullPath.substring(index + 1));
            setPath(fullPath.substring(0, index));
        } else {
            setName(fullPath);
        }
    }
}
