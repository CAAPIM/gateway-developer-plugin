package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotableEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotatedEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotationDeserializer;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.inject.Named;

import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Generic entity for yaml and json files
 */
@JsonInclude(NON_NULL)
@Named("GENERIC")
@ConfigurationFile(name = "generic-entities", type = JSON_YAML)
@EnvironmentType("GENERIC")
public class GenericEntity extends GatewayEntity implements AnnotableEntity {

    private String description;
    private String entityClassName;
    private String valueXml;
    @JsonDeserialize(using = AnnotationDeserializer.class)
    private Set<Annotation> annotations;
    @JsonIgnore
    private AnnotatedEntity<GatewayEntity> annotatedEntity;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    public String getValueXml() {
        return valueXml;
    }

    public void setValueXml(String valueXml) {
        this.valueXml = valueXml;
    }

    @JsonIgnore
    @Override
    public AnnotatedEntity<GatewayEntity> getAnnotatedEntity() {
        if (annotatedEntity == null && annotations != null) {
            annotatedEntity = createAnnotatedEntity();
        }
        return annotatedEntity;
    }

    @Override
    public String getType() {
        return EntityTypes.GENERIC_TYPE;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Set<Annotation> annotations) {
        this.annotations = annotations;
    }
}
