/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

public class Policy {
    private String path;
    private String policyXML;
    private String name;
    private Folder parentFolder;
    private String guid;
    private Element policyDocument;
    private final Set<Policy> dependencies = new HashSet<>();
    private String id;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPolicyXML() {
        return policyXML;
    }

    public void setPolicyXML(String policyXML) {
        this.policyXML = policyXML;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Folder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getGuid() {
        return guid;
    }

    public void setPolicyDocument(Element policyDocument) {
        this.policyDocument = policyDocument;
    }

    public Element getPolicyDocument() {
        return policyDocument;
    }

    public Set<Policy> getDependencies() {
        return dependencies;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
