/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotatedEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotatedEntityCreator;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.Metadata;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotationDeserializer;
import com.ca.apim.gateway.cagatewayconfig.config.spec.BundleGeneration;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.annotations.VisibleForTesting;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants.*;

import javax.inject.Named;
import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.stream.Collectors.toCollection;

@JsonInclude(NON_NULL)
@Named("ENCAPSULATED_ASSERTION")
@ConfigurationFile(name = "encass", type = JSON_YAML)
@EnvironmentType("ENCAPSULATED_ASSERTION")
@BundleGeneration
public class Encass extends GatewayEntity {

    private String policy;
    private Set<EncassArgument> arguments;
    private Set<EncassResult> results;
    private Map<String, Object> properties;
    @JsonDeserialize(using = AnnotationDeserializer.class)
    private Set<Annotation> annotations;
    @JsonIgnore
    private String guid;
    @JsonIgnore
    private String policyId;
    @JsonIgnore
    private String path;

    public Set<EncassArgument> getArguments() {
        return arguments;
    }

    public void setArguments(Set<EncassArgument> arguments) {
        this.arguments = arguments;
    }

    public Set<EncassResult> getResults() {
        return results;
    }

    public void setResults(Set<EncassResult> results) {
        this.results = results;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public boolean hasAnnotation() {
        return annotations != null && !annotations.isEmpty();
    }

    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Set<Annotation> annotations) {
        this.annotations = annotations;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void postLoad(String entityKey, Bundle bundle, File rootFolder, IdGenerator idGenerator) {
        setGuid(idGenerator.generateGuid());
        setId(idGenerator.generate());
        setName(entityKey);
    }

    @Override
    public void preWrite(File configFolder, DocumentFileUtils documentFileUtils) {
        sortArgumentsAndResults();
    }

    @JsonIgnore
    @Override
    public Metadata getMetadata() {
        return new Metadata() {
            @Override
            public String getType() {
                return EntityTypes.ENCAPSULATED_ASSERTION_TYPE;
            }

            @Override
            public String getName() {
                return Encass.this.getName();
            }

            public Set<EncassArgument> getArguments() {
                return Encass.this.getArguments();
            }

            public Set<EncassResult> getResults() {
                return Encass.this.getResults();
            }
        };
    }

    @VisibleForTesting
    public void sortArgumentsAndResults() {
        setArguments(getArguments().stream().collect(toCollection(() -> new TreeSet<>(Comparator.comparing(EncassArgument::getName)))));
        setResults(getResults().stream().collect(toCollection(() -> new TreeSet<>(Comparator.comparing(EncassResult::getName)))));
    }

    @Override
    public boolean hasBundleAnnotation(){
        if (hasAnnotation()){
            return annotations.contains(new Annotation(ANNOTATION_TYPE_BUNDLE));
        }
        return false;
    }

    @Override
    public AnnotatedEntity<GatewayEntity> getAnnotatedEntity(final String projectName,
                                                      final String projectVersion) {
        return AnnotatedEntityCreator.createAnnotatedEntity(this, projectName, projectVersion);
    }
}
