package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

/**
 * Specify modes for loading bundles.
 */
public enum BundleLoadingMode {

    /**
     * No errors/missing dependencies allowed.
     */
    STRICT,

    /**
     * Allow single loading and no dependency checking.
     */
    PERMISSIVE;
}
