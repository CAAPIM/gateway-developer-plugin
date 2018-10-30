/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import org.w3c.dom.Element;

import javax.inject.Named;

@Named("POLICY")
public class PolicyEntity implements Entity {
    private final String name;
    private final String id;
    private final String guid;
    private final String parentFolderId;
    private final String policy;
    private final String tag;
    private final PolicyType policyType;
    private Element policyXML;
    private String policyPath;

    public PolicyEntity(final Builder builder) {
        this.name = builder.name;
        this.id = builder.id;
        this.guid = builder.guid;
        this.parentFolderId = builder.parentFolderId == null || builder.parentFolderId.isEmpty() ? null : builder.parentFolderId;
        this.policyXML = builder.policyXML;
        this.policy = builder.policy;
        this.tag = builder.tag;
        this.policyType = builder.policyType;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getFolderId() {
        return parentFolderId;
    }

    public String getPolicy() {
        return policy;
    }

    public String getTag() {
        return tag;
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public Element getPolicyXML() {
        return policyXML;
    }

    public void setPolicyXML(Element policyXML) {
        this.policyXML = policyXML;
    }

    public String getPolicyPath() {
        return policyPath;
    }

    public void setPolicyPath(String policyPath) {
        this.policyPath = policyPath;
    }

    public static class Builder {

        private String name;
        private String id;
        private String guid;
        private String parentFolderId;
        private String policy;
        private String tag;
        private PolicyType policyType;
        private Element policyXML;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setGuid(String guid) {
            this.guid = guid;
            return this;
        }

        public Builder setParentFolderId(String parentFolderId) {
            this.parentFolderId = parentFolderId;
            return this;
        }

        public Builder setPolicy(String policy) {
            this.policy = policy;
            return this;
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setPolicyXML(Element policyXML) {
            this.policyXML = policyXML;
            return this;
        }

        public Builder setPolicyType(PolicyType policyType) {
            this.policyType = policyType;
            return this;
        }

        public PolicyEntity build() {
            return new PolicyEntity(this);
        }
    }
}
