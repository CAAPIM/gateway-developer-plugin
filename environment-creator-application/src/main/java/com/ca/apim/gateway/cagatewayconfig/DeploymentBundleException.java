package com.ca.apim.gateway.cagatewayconfig;

public class DeploymentBundleException extends RuntimeException {
    public DeploymentBundleException(String message) {
        super(message);
    }

    public DeploymentBundleException(String message, Throwable cause) {
        super(message, cause);
    }
}
