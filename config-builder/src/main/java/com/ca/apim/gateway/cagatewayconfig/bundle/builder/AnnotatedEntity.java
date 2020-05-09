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
    private boolean bundle;
    private boolean reusable;
    private boolean redeployable;
    private boolean exclude;
    private boolean reusableEntity;
    private String uniquePrefix;
    private String uniqueSuffix;
    private Collection<String> tags;
    private String projectName;
    private String projectVersion;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getUniquePrefix() {
        return uniquePrefix;
    }

    public void setUniquePrefix(String uniquePrefix) {
        this.uniquePrefix = uniquePrefix;
    }

    public String getUniqueSuffix() {
        return uniqueSuffix;
    }

    public void setUniqueSuffix(String uniqueSuffix) {
        this.uniqueSuffix = uniqueSuffix;
    }

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

    public boolean isBundle() {
        return bundle;
    }

    public void setBundle(boolean isBundleType) {
        this.bundle = isBundleType;
    }

    public boolean isReusable() {
        return reusable;
    }

    public void setReusable(boolean isReusableType) {
        this.reusable = isReusableType;
    }

    public boolean isRedeployable() {
        return redeployable;
    }

    public void setRedeployable(boolean isRedeployableType) {
        this.redeployable = isRedeployableType;
    }

    public boolean exclude() {
        return exclude;
    }

    public void setExclude(boolean isExcludeType) {
        this.exclude = isExcludeType;
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
