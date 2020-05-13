package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import org.apache.commons.lang3.StringUtils;

public class AnnotatedBundle extends Bundle {
    private Bundle fullBundle;
    private AnnotatedEntity<? extends GatewayEntity> annotatedEntity;
    private String projectName;
    private String projectGroupName;
    private String projectVersion;

    public AnnotatedBundle(Bundle fullBundle, AnnotatedEntity<? extends GatewayEntity> annotatedEntity) {
        this.fullBundle = fullBundle;
        this.annotatedEntity = annotatedEntity;
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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectGroupName() {
        return projectGroupName;
    }

    public void setProjectGroupName(String projectGroupName) {
        this.projectGroupName = projectGroupName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public String getBundleName() {
        if (StringUtils.isBlank(annotatedEntity.getBundleName())) {
            return projectName + "-" + annotatedEntity.getEntityName() + "-" + projectVersion;
        } else {
            return annotatedEntity.getBundleName() + "-" + projectVersion;
        }
    }

    public String getUniquePrefix() {
        AnnotableEntity annotableEntity = (AnnotableEntity) annotatedEntity.getEntity();
        return projectName + "-" + annotableEntity.getType() + "-" + PathUtils.extractName(annotatedEntity.getEntityName()) + "-";
    }

    public String getUniqueSuffix() {
        return "-" + projectVersion;
    }

}
