/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.w3c.dom.Element;

import javax.inject.Named;
import java.util.Map;
import java.util.Set;

@JsonInclude(Include.NON_NULL)
@Named("SERVICE")
public class Service extends Folderable {

    private String url;
    private String policy;
    private Set<String> httpMethods;
    private Map<String,String> properties;
    private Element serviceDetailsElement;
    private Element policyXML;
    private String path;

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

    public Map<String,String> getProperties(){ return properties;}

    public void setProperties(Map<String,String> properties){ this.properties = properties;}

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
