/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.w3c.dom.Element;

import javax.inject.Named;
import java.io.File;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;

@JsonInclude(Include.NON_NULL)
@Named("SERVICE")
@ConfigurationFile(name = "services", type = JSON_YAML)
@EnvironmentType("SERVICE")
public class Service extends Folderable {

    private String url;
    private String policy;
    private Set<String> httpMethods;
    private Map<String,Object> properties;
    @JsonIgnore
    private Element serviceDetailsElement;
    @JsonIgnore
    private Element policyXML;
    private WSDL wsdl;

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

    public WSDL getWsdl() {
        return wsdl;
    }

    public void setWsdl(WSDL wsdl) {
        this.wsdl = wsdl;
    }
}
