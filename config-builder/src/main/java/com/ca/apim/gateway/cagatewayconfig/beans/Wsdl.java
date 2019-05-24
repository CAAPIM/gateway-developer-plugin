package com.ca.apim.gateway.cagatewayconfig.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.inject.Named;

/**
 * An object to contain all related Wsdl configurables
 */
@Named("WSDL")
public class Wsdl extends Folderable {

    private String rootUrl;
    @JsonIgnore
    private String wsdlXml;

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getWsdlXml() {
        return wsdlXml;
    }

    public void setWsdlXml(String wsdlXml) {
        this.wsdlXml = wsdlXml;
    }

}
