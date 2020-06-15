package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Annotation;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.util.entity.AnnotationConstants.*;

public interface AnnotableEntity {

    Annotation BUNDLE_ANNOTATION = new Annotation(ANNOTATION_TYPE_BUNDLE);
    Annotation REUSABLE_ANNOTATION = new Annotation(ANNOTATION_TYPE_REUSABLE);
    Annotation REDEPLOYABLE_ANNOTATION = new Annotation(ANNOTATION_TYPE_REDEPLOYABLE);
    Annotation EXCLUDE_ANNOTATION = new Annotation(ANNOTATION_TYPE_EXCLUDE);

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
    @JsonIgnore
    String getType();

    /**
     * This method returns all the Annotation applied to the entity.
     *
     * @return Set of all the annotations
     */
    Set<Annotation> getAnnotations();


    /**
     * Creates AnnotatedEntity object by scanning all the annotations and gathering all the information required to
     * generate the bundle and its metadata.
     *
     * @return AnnotatedEntity
     */
    default AnnotatedEntity<GatewayEntity> createAnnotatedEntity() {
        final Set<Annotation> annotations = getAnnotations();
        if (annotations != null) {
            AnnotatedEntity<GatewayEntity> annotatedEntity = new AnnotatedEntity(this);
            annotations.forEach(annotation -> {
                if (ANNOTATION_TYPE_BUNDLE.equalsIgnoreCase(annotation.getType())) {
                    annotatedEntity.setMetadataId(annotation.getId());
                    annotatedEntity.setTags(annotation.getTags());
                    annotatedEntity.setEntityType(EntityTypes.ENCAPSULATED_ASSERTION_TYPE);
                    annotatedEntity.setBundleName(annotation.getName());
                    annotatedEntity.setDescription(annotation.getDescription());
                } else if(ANNOTATION_TYPE_BUNDLE_ENTITY.equalsIgnoreCase(annotation.getType())) {
                    annotatedEntity.setId(annotation.getId());
                    annotatedEntity.setGuid(annotation.getGuid());
                }
            });
            return annotatedEntity;
        }
        return null;
    }


    /**
     * Returns TRUE if "@bundle" annotation is added to the entity
     *
     * @return TRUE if "@bundle" annotation is added
     */
    @JsonIgnore
    default boolean isBundle() {
        return getAnnotations() != null && getAnnotations().contains(BUNDLE_ANNOTATION);
    }

    /**
     * Returns TRUE if "@redeployable" annotation is added to the entity
     *
     * @return TRUE if "@redeployable" annotation is added
     */
    @JsonIgnore
    default boolean isRedeployable() {
        return getAnnotations() != null && getAnnotations().contains(REDEPLOYABLE_ANNOTATION);
    }

    /**
     * Returns TRUE if "@reusable" annotation is added to the entity
     *
     * @return TRUE if "@reusable" annotation is added
     */
    @JsonIgnore
    default boolean isReusable() {
        return getAnnotations() != null && getAnnotations().contains(REUSABLE_ANNOTATION);
    }

    /**
     * Returns TRUE if "@exclude" annotation is added to the entity
     *
     * @return TRUE if "@exclude" annotation is added
     */
    @JsonIgnore
    default boolean isExcluded() {
        return getAnnotations() != null && getAnnotations().contains(EXCLUDE_ANNOTATION);
    }
}
