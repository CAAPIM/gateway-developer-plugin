package com.ca.apim.gateway.cagatewayconfig.util.file;

import java.io.IOException;

@FunctionalInterface
public interface SupplierWithIO<T> {
    T getWithIO() throws IOException;
}