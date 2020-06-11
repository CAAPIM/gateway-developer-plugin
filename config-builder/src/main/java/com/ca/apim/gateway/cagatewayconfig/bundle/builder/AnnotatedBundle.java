package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import org.apache.commons.lang3.StringUtils;

public class AnnotatedBundle extends Bundle {
    private Bundle fullBundle;
    private AnnotatedEntity<? extends GatewayEntity> annotatedEntity;
    private ProjectInfo projectInfo;
    private String uniqueNameSeparator = "#";   // This can be different for environment entities.

    public AnnotatedBundle(Bundle fullBundle, AnnotatedEntity<? extends GatewayEntity> annotatedEntity,
                           ProjectInfo projectInfo) {
        this.fullBundle = fullBundle;
        this.annotatedEntity = annotatedEntity;
        this.projectInfo = projectInfo;
    }

    public AnnotatedEntity<? extends GatewayEntity> getAnnotatedEntity() {
        return annotatedEntity;
    }

    public void setAnnotatedEntity(AnnotatedEntity<? extends GatewayEntity> annotatedEntity) {
        this.annotatedEntity = annotatedEntity;
    }

    public Bundle getFullBundle() {
        return fullBundle;
    }

    public void setFullBundle(Bundle fullBundle) {
        this.fullBundle = fullBundle;
    }

    public String getBundleName() {
        String name;
        if (StringUtils.isBlank(annotatedEntity.getBundleName())) {
            name = projectInfo.getName() + "-" + annotatedEntity.getEntityName();
        } else {
            name = annotatedEntity.getBundleName();
        }
        return StringUtils.isBlank(projectInfo.getVersion()) ? name : name + "-" + projectInfo.getVersion();
    }

    public String getUniquePrefix() {
        return projectInfo.getName() + uniqueNameSeparator + PathUtils.extractName(annotatedEntity.getEntityName()) + uniqueNameSeparator;
    }

    public String getUniqueSuffix() {
        return "-" + projectInfo.getVersion();
    }

}
