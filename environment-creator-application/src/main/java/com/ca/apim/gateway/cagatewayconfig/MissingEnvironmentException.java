package com.ca.apim.gateway.cagatewayconfig;

public class MissingEnvironmentException extends RuntimeException {
    public MissingEnvironmentException(String message) {
        super(message);
    }
}
