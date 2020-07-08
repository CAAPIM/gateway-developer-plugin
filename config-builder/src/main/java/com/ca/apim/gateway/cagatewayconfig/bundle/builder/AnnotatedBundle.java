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
    private static final String UNIQUE_NAME_SEPARATOR = "::";   // This can be different for environment entities.

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
        String name = getAnnotatedBundleName();
        return StringUtils.isBlank(projectInfo.getVersion()) ? name : name + "-" + projectInfo.getVersion();
    }

    private String getAnnotatedBundleName() {
        if (StringUtils.isBlank(annotatedEntity.getBundleName())) {
            return projectInfo.getName() + "-" + PathUtils.extractName(annotatedEntity.getEntityName());
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

    /**
     * Applies unique name space and version to the given entity name
     * ::groupName.bundleName::entityName::majorVersion.minorVersion
     *
     * @param entityName String
     * @return String
     */
    public String applyUniqueName(final String entityName) {
        StringBuilder uniqueName = new StringBuilder(UNIQUE_NAME_SEPARATOR);
        if (StringUtils.isNotBlank(projectInfo.getGroupName())) {
            uniqueName.append(projectInfo.getGroupName());
            uniqueName.append(".");
        }
        uniqueName.append(getAnnotatedBundleName());
        uniqueName.append(UNIQUE_NAME_SEPARATOR);
        uniqueName.append(entityName);

        String version = projectInfo.getVersion();
        if (StringUtils.isNotBlank(version)) {
            uniqueName.append(UNIQUE_NAME_SEPARATOR);
            String[] subVersions = version.split("\\.");
            uniqueName.append(subVersions.length > 0 ? subVersions[0] : version);
            uniqueName.append(".");
            uniqueName.append(subVersions.length > 1 ? subVersions[1] : "0");
        }

        return uniqueName.toString();
    }

}
