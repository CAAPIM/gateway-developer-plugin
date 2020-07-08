/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotableEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotatedEntity;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.AnnotationDeserializer;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.Metadata;
import com.ca.apim.gateway.cagatewayconfig.config.spec.BundleGeneration;
import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import javax.inject.Named;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;

@JsonInclude(Include.NON_NULL)
@Named("SERVICE")
@ConfigurationFile(name = "services", type = JSON_YAML)
@EnvironmentType("SERVICE")
@BundleGeneration
public class Service extends Folderable implements AnnotableEntity {

    private String guid;
    private String url;
    private String policy;
    private Set<String> httpMethods;
    private Map<String,Object> properties;
    @JsonDeserialize(using = AnnotationDeserializer.class)
    private Set<Annotation> annotations;
    @JsonIgnore
    private Element serviceDetailsElement;
    @JsonIgnore
    private Element policyXML;
    private Set<SoapResource> soapResources;
    private String soapVersion;
    private boolean wssProcessingEnabled;
    private String wsdlRootUrl;
    @JsonIgnore
    private AnnotatedEntity<? extends GatewayEntity> annotatedEntity;

    @Override
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public Set<String> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(Set<String> httpMethods) {
        this.httpMethods = httpMethods;
    }

    public Map<String,Object> getProperties(){ return properties;}

    public void setProperties(Map<String,Object> properties){ this.properties = properties;}

    public Element getServiceDetailsElement() {
        return serviceDetailsElement;
    }

    public void setServiceDetailsElement(Element serviceDetailsElement) {
        this.serviceDetailsElement = serviceDetailsElement;
    }

    public Element getPolicyXML() {
        return policyXML;
    }

    public void setPolicyXML(Element policyXML) {
        this.policyXML = policyXML;
    }

    @Override
    @JsonIgnore
    public String getPath() {
        return super.getPath();
    }

    @Override
    public String getMappingValue() {
        return getPath();
    }

    @Override
    public void preWrite(File configFolder, DocumentFileUtils documentFileUtils) {
        setPolicy(getPath());
    }

    public Set<SoapResource> getSoapResources() {
        return soapResources;
    }

    public void setSoapResources(Set<SoapResource> soapResources) {
        this.soapResources = soapResources;
    }

    public void addSoapResource(SoapResource wsdl) {
        if (this.soapResources == null) {
            this.soapResources = new LinkedHashSet<>();
        }
        this.soapResources.add(wsdl);
    }

    public String getSoapVersion() {
        return soapVersion;
    }

    public void setSoapVersion(String soapVersion) {
        this.soapVersion = soapVersion;
    }

    public boolean isWssProcessingEnabled() {
        return wssProcessingEnabled;
    }

    public void setWssProcessingEnabled(boolean wssProcessingEnabled) {
        this.wssProcessingEnabled = wssProcessingEnabled;
    }

    public String getWsdlRootUrl() {
        return wsdlRootUrl;
    }

    public void setWsdlRootUrl(String wsdlRootUrl) {
        this.wsdlRootUrl = wsdlRootUrl;
    }

    @Override
    public AnnotatedEntity getAnnotatedEntity() {
        if (annotatedEntity == null && annotations != null) {
            annotatedEntity = createAnnotatedEntity();
            if (StringUtils.isBlank(annotatedEntity.getDescription())) {
                Map<String, Object> props = getProperties();
                if (props != null) {
                    annotatedEntity.setDescription(props.getOrDefault("description", "").toString());
                }
            }
            annotatedEntity.setPolicyName(getPolicy());
            annotatedEntity.setEntityName(getName());
        }
        return annotatedEntity;
    }

    @JsonIgnore
    @Override
    public Metadata getMetadata() {
        return new Metadata() {
            public String getType() {
                return EntityTypes.SERVICE_TYPE;
            }

            @Override
            public String getName() {
                return Service.this.getName();
            }

            @Override
            public String getId() {
                return Service.this.getId();
            }

            @Override
            public String getGuid() {
                return Service.this.getGuid();
            }

            public String getUri() {
                return Service.this.getUrl();
            }

            public boolean isSoap() {
                return  Service.this.getSoapResources() != null && StringUtils.isNotBlank(Service.this.getWsdlRootUrl());
            }
        };
    }

    @Override
    public String getType(){
        return EntityTypes.SERVICE_TYPE;
    }

    @Override
    public void postLoad(String entityKey, Bundle bundle, File rootFolder, IdGenerator idGenerator) {
        setGuid(idGenerator.generateGuid());
        setId(idGenerator.generate());
        setName(entityKey);
    }
}
