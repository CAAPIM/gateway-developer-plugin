package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.ProjectInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.DependentBundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AnnotatedBundle extends Bundle {
    private Bundle fullBundle;
    private AnnotatedEntity<? extends GatewayEntity> annotatedEntity;
    private List<DependentBundle> dependentBundles = new ArrayList<>();

    public AnnotatedBundle(Bundle fullBundle, AnnotatedEntity<? extends GatewayEntity> annotatedEntity,
                           ProjectInfo projectInfo) {
        super(projectInfo);
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

    public String getBundleName() {
        String name = getAnnotatedBundleName();
        return StringUtils.isBlank(getProjectInfo().getVersion()) ? name : name + "-" + getProjectInfo().getVersion();
    }

    private String getAnnotatedBundleName() {
        if (StringUtils.isBlank(annotatedEntity.getBundleName())) {
            return getProjectInfo().getName() + "-" + annotatedEntity.getEntityName();
        } else {
            return annotatedEntity.getBundleName();
        }
    }

    public void addDependentBundle(DependentBundle dependentBundle){
        dependentBundles.add(dependentBundle);
    }

    public List<DependentBundle> getDependentBundles() {
        return dependentBundles;
    }

    @Override
    protected String getNamespace(final EntityBuilder.BundleType bundleType, boolean isShared) {
        if (isShared || EntityBuilder.BundleType.ENVIRONMENT == bundleType) {
            return super.getNamespace(bundleType, isShared);
        }
        return getProjectInfo().getGroupName() + '.' + getAnnotatedBundleName();
    }

}
