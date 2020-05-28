package com.ca.apim.gateway.cagatewayconfig.util.entity;

import java.util.HashSet;
import java.util.Set;

public class AnnotationConstants {
    /**
     * Constants for Gateway entity annotation types supported by the plugin.
     */

    public static final String ANNOTATION_TYPE_BUNDLE = "@bundle";
    public static final String ANNOTATION_TYPE_REUSABLE = "@reusable";
    public static final String ANNOTATION_TYPE_REDEPLOYABLE = "@redeployable";
    public static final String ANNOTATION_TYPE_EXCLUDE = "@exclude";
    public static final String ANNOTATION_TYPE_BUNDLE_ENTITY = "@bundle-entity";

    public static final Set<String> SUPPORTED_ANNOTATION_TYPES;
    static {
        SUPPORTED_ANNOTATION_TYPES = new HashSet<>();
        SUPPORTED_ANNOTATION_TYPES.add(ANNOTATION_TYPE_BUNDLE);
        SUPPORTED_ANNOTATION_TYPES.add(ANNOTATION_TYPE_REUSABLE);
        SUPPORTED_ANNOTATION_TYPES.add(ANNOTATION_TYPE_REDEPLOYABLE);
        SUPPORTED_ANNOTATION_TYPES.add(ANNOTATION_TYPE_EXCLUDE);
        SUPPORTED_ANNOTATION_TYPES.add(ANNOTATION_TYPE_BUNDLE_ENTITY);
    }

    private AnnotationConstants() {
    }
}

