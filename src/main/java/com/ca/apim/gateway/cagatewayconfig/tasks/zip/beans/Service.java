/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import java.util.Map;
import java.util.Set;

public class Service {
    private String url;
    private Set<String> httpMethods;
    private Map<String,String> properties;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<String> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(Set<String> httpMethods) {
        this.httpMethods = httpMethods;
    }

    public Map<String,String> getProperties(){ return properties;}

    public void setProperties(Map<String,String> properties){ this.properties = properties;}
}
