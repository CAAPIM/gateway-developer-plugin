package com.ca.apim.gateway.cagatewayconfig.beans;

/**
 * Possible types of soap service resources.
 */
public enum SoapResourceType {

    WSDL(".wsdl"),

    XMLSCHEMA(".xsd");

    private String extension;

    SoapResourceType(String extension) {
        this.extension = extension;
    }

    public final String getExtension() {
        return extension;
    }

    public String getType() {
        return name().toLowerCase();
    }
}
