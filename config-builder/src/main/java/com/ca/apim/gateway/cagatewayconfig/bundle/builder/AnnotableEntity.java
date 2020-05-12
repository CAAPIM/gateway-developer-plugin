package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;

import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants.*;

public interface AnnotableEntity {
    AnnotatedEntity<GatewayEntity> getAnnotatedEntity(final String projectName,
                                                      final String projectVersion);

    void populateBundleInfo(final AnnotatedEntity<GatewayEntity> annotatedEntity, final Annotation bundleAnnotation, final String projectName,
                            final String projectVersion);

    /**
     * Creates AnnotatedEntity object by scanning all the annotations and gathering all the information required to
     * generate the bundle and its metadata.
     *
     * @param projectName    Project name
     * @param projectVersion Project version
     * @return AnnotatedEntity
     */
    default AnnotatedEntity<GatewayEntity> createAnnotatedEntity(final String projectName,
                                                                 final String projectVersion) {
        final Set<Annotation> annotations = getAnnotations();
        if (annotations != null) {
            AnnotatedEntity<GatewayEntity> annotatedEntity = new AnnotatedEntity(this);
            annotations.forEach(annotation -> {
                switch (annotation.getType()) {
                    case ANNOTATION_TYPE_BUNDLE:
                        annotatedEntity.setTags(annotation.getTags());
                        annotatedEntity.setBundle(true);
                        annotatedEntity.setEntityType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
                        populateBundleInfo(annotatedEntity, annotation, projectName,
                                projectVersion);
                        break;
                    case ANNOTATION_TYPE_REUSABLE:
                    case ANNOTATION_TYPE_REUSABLE_BUNDLE:
                        annotatedEntity.setReusable(true);
                        break;
                    case ANNOTATION_TYPE_REUSABLE_ENTITY:
                        annotatedEntity.setReusableEntity(true);
                        break;
                    case ANNOTATION_TYPE_REDEPLOYABLE:
                        annotatedEntity.setRedeployable(true);
                        break;
                    case ANNOTATION_TYPE_EXCLUDE:
                        annotatedEntity.setExclude(true);
                        break;
                    default:
                        break;
                }
            });
            return annotatedEntity;
        }
        return null;
    }

    Set<Annotation> getAnnotations();

    default boolean isBundle() {
        return getAnnotations() != null && getAnnotations().contains(new Annotation(ANNOTATION_TYPE_BUNDLE));
    }

    default boolean isRedeployable() {
        return getAnnotations() != null && getAnnotations().contains(new Annotation(ANNOTATION_TYPE_REDEPLOYABLE));
    }

    default boolean isReusable() {
        return getAnnotations() != null && getAnnotations().contains(new Annotation(ANNOTATION_TYPE_REUSABLE));
    }
}
