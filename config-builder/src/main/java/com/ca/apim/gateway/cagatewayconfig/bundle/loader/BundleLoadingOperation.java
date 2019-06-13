package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

/**
 * Specify operations for loading bundles.
 */
public enum BundleLoadingOperation {

    /**
     * Export operation: No errors/missing dependencies allowed.
     */
    EXPORT,

    /**
     * Validate operation: Allow single loading, skip dependency checking and block by default duplicated policies.
     */
    VALIDATE;
}
