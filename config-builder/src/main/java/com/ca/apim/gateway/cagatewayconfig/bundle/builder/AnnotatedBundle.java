package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.DependentBundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AnnotatedBundle extends Bundle {
    private Bundle fullBundle;
    private AnnotatedEntity<? extends GatewayEntity> annotatedEntity;
    private ProjectInfo projectInfo;
    private List<DependentBundle> dependentBundles = new ArrayList<>();

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
        if (StringUtils.isBlank(annotatedEntity.getBundleName())) {
            return projectInfo.getName() + "-" + annotatedEntity.getEntityName() + "-" + projectInfo.getVersion();
        } else {
            return annotatedEntity.getBundleName() + "-" + projectInfo.getVersion();
        }
    }

    public void addDependentBundle(DependentBundle dependentBundle){
        dependentBundles.add(dependentBundle);
    }

    public List<DependentBundle> getDependentBundles() {
        return dependentBundles;
    }

    public String getUniquePrefix() {
        AnnotableEntity annotableEntity = (AnnotableEntity) annotatedEntity.getEntity();
        return projectInfo.getName() + "-" + annotableEntity.getShortenedType() + "-" + PathUtils.extractName(annotatedEntity.getEntityName()) + "-";
    }

    public String getUniqueSuffix() {
        return "-" + projectInfo.getVersion();
    }

}
