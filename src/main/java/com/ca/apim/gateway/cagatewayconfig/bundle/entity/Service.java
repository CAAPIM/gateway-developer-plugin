/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.entity;

import com.ca.apim.gateway.cagatewayconfig.bundle.Entity;
import org.w3c.dom.Element;

import java.util.LinkedList;
import java.util.List;

public class Service implements Entity {
    private final String name;
    private final String id;
    private final String parentFolderId;
    private final boolean enabled;
    private final String url;
    private final List<String> httpMethods;
    private final String policy;
    private final Element serviceXML;

    public Service(final String name, final String id, final String parentFolderId, Element serviceXML, String policy, boolean enabled, String url, List<String> httpMethods) {
        this.name = name;
        this.id = id;
        this.parentFolderId = parentFolderId == null || parentFolderId.isEmpty() ? null : parentFolderId;
        this.serviceXML = serviceXML;
        this.enabled = enabled;
        this.url = url;
        this.httpMethods = httpMethods;
        this.policy = policy;
    }

    @Override
    public String getType() {
        return "SERVICE";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Element getXml() {
        return serviceXML;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getFolderId() {
        return parentFolderId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public String getPolicy() {
        return policy;
    }

}
