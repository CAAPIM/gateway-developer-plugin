package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.paths.PathUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants.*;

public interface AnnotableEntity {
    /**
     * This method creates annotated entity from annotations defined and then returns AnnotatedEntity
     *
     * @return AnnotatedEntity
     */
    AnnotatedEntity<GatewayEntity> getAnnotatedEntity();

    /**
     * This method returns type of Entity
     *
     * @return String
     */
    String getType();

    /**
     * Creates AnnotatedEntity object by scanning all the annotations and gathering all the information required to
     * generate the bundle and its metadata.
     *
     * @param annotations
     * @return AnnotatedEntity
     */
    default AnnotatedEntity<GatewayEntity> createAnnotatedEntity(final Set<Annotation> annotations) {
        if (annotations != null) {
            AnnotatedEntity<GatewayEntity> annotatedEntity = new AnnotatedEntity(this);
            annotations.forEach(annotation -> {
                switch (annotation.getType()) {
                    case ANNOTATION_TYPE_BUNDLE:
                        annotatedEntity.setTags(annotation.getTags());
                        annotatedEntity.setBundle(true);
                        annotatedEntity.setEntityType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
                        String annotatedBundleName = annotation.getName();
                        annotatedEntity.setBundleName(annotatedBundleName);
                        String description = annotation.getDescription();
                        annotatedEntity.setDescription(description);
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

}
