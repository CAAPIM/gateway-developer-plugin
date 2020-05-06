package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Encass;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import org.apache.commons.lang3.StringUtils;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants.*;

public class AnnotatedEntityCreator {

    /**
     * Creates AnnotatedEntity object by scanning all the annotations and gathering all the information required to
     * generate the bundle and its metadata.
     *
     * @param encass         Encass
     * @param projectName    Project name
     * @param projectVersion Project version
     * @return AnnotatedEntity
     */
    public static AnnotatedEntity<GatewayEntity> createAnnotatedEntity(final Encass encass, final String projectName,
                                                                final String projectVersion) {
        AnnotatedEntity<GatewayEntity> annotatedEntity = new AnnotatedEntity<>(encass);
        encass.getAnnotations().forEach(annotation -> {
            switch (annotation.getType()) {
                case ANNOTATION_TYPE_BUNDLE:
                    String annotatedBundleName = annotation.getName();
                    if (StringUtils.isBlank(annotatedBundleName)) {
                        annotatedBundleName = projectName + "-" + encass.getName();
                    }

                    String description = annotation.getDescription();
                    if (StringUtils.isBlank(description)) {
                        description = encass.getProperties().getOrDefault("description", "").toString();
                    }
                    annotatedEntity.setTags(annotation.getTags());
                    annotatedEntity.setBundleType(true);
                    annotatedEntity.setEntityName(encass.getName());
                    annotatedEntity.setDescription(description);
                    annotatedEntity.setEntityType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
                    annotatedEntity.setBundleName(annotatedBundleName + "-" + projectVersion);
                    annotatedEntity.setPolicyName(encass.getPolicy());
                    annotatedEntity.setUniquePrefix(projectName + "-encass-" + PathUtils.extractName(encass.getName()) + "-");
                    annotatedEntity.setUniqueSuffix("-" + projectVersion);
                    break;
                case ANNOTATION_TYPE_REUSABLE:
                case ANNOTATION_TYPE_REUSABLE_BUNDLE:
                    annotatedEntity.setReusableType(true);
                    break;
                case ANNOTATION_TYPE_REUSABLE_ENTITY:
                    annotatedEntity.setReusableEntity(true);
                    break;
                case ANNOTATION_TYPE_REDEPLOYABLE:
                    annotatedEntity.setRedeployableType(true);
                    break;
                case ANNOTATION_TYPE_EXCLUDE:
                    annotatedEntity.setExcludeType(true);
                    break;
                default:
                    break;
            }
        });
        return annotatedEntity;
    }
}
