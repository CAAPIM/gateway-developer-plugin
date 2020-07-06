package com.ca.apim.gateway.cagatewayconfig.util.entity;

import java.util.HashSet;
import java.util.Set;

public class AnnotationType {

    /**
     * Constants for Gateway entity annotation types supported by the plugin.
     */
    public static final String BUNDLE = "@bundle";
    public static final String REUSABLE = "@reusable";
    public static final String REDEPLOYABLE = "@redeployable";
    public static final String EXCLUDE = "@exclude";
    public static final String BUNDLE_HINTS = "@bundle-hints";

    public static final Set<String> SUPPORTED_ANNOTATION_TYPES;

    static {
        SUPPORTED_ANNOTATION_TYPES = new HashSet<>();
        SUPPORTED_ANNOTATION_TYPES.add(BUNDLE);
        SUPPORTED_ANNOTATION_TYPES.add(REUSABLE);
        SUPPORTED_ANNOTATION_TYPES.add(REDEPLOYABLE);
        SUPPORTED_ANNOTATION_TYPES.add(EXCLUDE);
        SUPPORTED_ANNOTATION_TYPES.add(BUNDLE_HINTS);
    }

    private AnnotationType() {
    }
}

