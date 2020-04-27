package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.Entity;

import java.util.Map;

public class AnnotatedEntity {
    private String entityName;
    private String entityType;
    private String bundleName;
    private String policyName;
    private String description;
    private boolean isBundleType;
    private boolean isReusableType;
    private boolean isRedeployableType;
    private boolean isExcludeType;

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
}
