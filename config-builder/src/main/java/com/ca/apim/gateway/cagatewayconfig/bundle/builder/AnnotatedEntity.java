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
    private Collection<String> tags;
    private String id;
    private String guid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public AnnotatedEntity(T entity) {
        this.entity = entity;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityType() {
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

        return entity instanceof AnnotableEntity && ((AnnotableEntity) entity).isBundle();
    }

    public boolean isReusable() {
        return entity instanceof AnnotableEntity && ((AnnotableEntity) entity).isReusable();
    }

    public boolean isRedeployable() {
        return entity instanceof AnnotableEntity && ((AnnotableEntity) entity).isRedeployable();
    }

    public boolean isExcluded() {
        return entity instanceof AnnotableEntity && ((AnnotableEntity) entity).isExcluded();
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
