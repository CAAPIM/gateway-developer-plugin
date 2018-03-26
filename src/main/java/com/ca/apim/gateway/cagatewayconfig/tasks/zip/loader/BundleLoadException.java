package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import java.io.IOException;

public class BundleLoadException extends RuntimeException {
    public BundleLoadException(String message) {
        super(message);
    }

    public BundleLoadException(String message, Exception e) {
        super(message, e);
    }
}
