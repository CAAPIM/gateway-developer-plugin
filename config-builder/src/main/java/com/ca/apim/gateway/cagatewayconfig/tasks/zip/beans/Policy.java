/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@JsonInclude(NON_EMPTY)
public class Policy extends Folderable {
    private String path;
    private String policyXML;
    private String name;
    private String guid;
    private Element policyDocument;
    private final Set<Policy> dependencies = new HashSet<>();
    private String id;
    private String tag;
    private PolicyType policyType;

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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    public Policy merge(Policy otherPolicy) {
        this.policyXML = firstNonNull(otherPolicy.policyXML, this.policyXML);
        this.name = firstNonNull(otherPolicy.name, this.name);
        this.parentFolder = firstNonNull(otherPolicy.parentFolder, this.parentFolder);
        this.guid = firstNonNull(otherPolicy.guid, this.guid);
        this.policyDocument = firstNonNull(otherPolicy.policyDocument, this.policyDocument);
        this.dependencies.addAll(otherPolicy.dependencies);
        this.id = firstNonNull(otherPolicy.id, this.id);
        this.tag = firstNonNull(otherPolicy.tag, this.tag);
        this.policyType = firstNonNull(otherPolicy.policyType, this.policyType);

        return this;
    }
}
