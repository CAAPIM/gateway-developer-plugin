/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.w3c.dom.Element;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@JsonInclude(NON_EMPTY)
@Named("POLICY")
public class Policy extends Folderable {

    private String path;
    private String policyXML;
    private String guid;
    private Element policyDocument;
    private final Set<Policy> dependencies = new HashSet<>();
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

    Policy merge(Policy otherPolicy) {
        this.policyXML = firstNonNull(otherPolicy.policyXML, this.policyXML);
        this.setName(firstNonNull(otherPolicy.getName(), this.getName()));
        this.setParentFolder(firstNonNull(otherPolicy.getParentFolder(), this.getParentFolder()));
        this.guid = firstNonNull(otherPolicy.guid, this.guid);
        this.policyDocument = firstNonNull(otherPolicy.policyDocument, this.policyDocument);
        this.dependencies.addAll(otherPolicy.dependencies);
        this.setId(firstNonNull(otherPolicy.getId(), this.getId()));
        this.tag = firstNonNull(otherPolicy.tag, this.tag);
        this.policyType = firstNonNull(otherPolicy.policyType, this.policyType);

        return this;
    }
}
