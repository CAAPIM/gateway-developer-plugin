/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import java.util.Collection;

public class AnnotatedEntity<T> {
    private final T entity;
    private String entityName;
    private String entityType;
    private String bundleName;
    private String policyName;
    private String description;
    private boolean isBundleType;
    private boolean isReusableType;
    private boolean isRedeployableType;
    private boolean isExcludeType;
    private boolean reusableEntity;
    private Collection<String> tags;

    public AnnotatedEntity(T entity) {
        this.entity = entity;
    }

    public boolean isReusableEntity() {
        return reusableEntity;
    }

    public void setReusableEntity(boolean reusableEntity) {
        this.reusableEntity = reusableEntity;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityName(){
        return entityName;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityType(){
        return entityType;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isBundleTypeEnabled() {
        return isBundleType;
    }

    public void setBundleType(boolean isBundleType) {
        this.isBundleType = isBundleType;
    }

    public boolean isReusableTypeEnabled() {
        return isReusableType;
    }

    public void setReusableType(boolean isReusableType) {
        this.isReusableType = isReusableType;
    }

    public boolean isRedeployableTypeEnabled() {
        return isRedeployableType;
    }

    public void setRedeployableType(boolean isRedeployableType) {
        this.isRedeployableType = isRedeployableType;
    }

    public boolean isExcludeTypeEnabled() {
        return isExcludeType;
    }

    public void setExcludeType(boolean isExcludeType) {
        this.isExcludeType = isExcludeType;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(Collection<String> tags) {
        this.tags = tags;
    }

    T getEntity() {
        return entity;
    }
}
