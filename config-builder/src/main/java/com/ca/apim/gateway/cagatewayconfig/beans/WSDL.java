package com.ca.apim.gateway.cagatewayconfig.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WSDL {

    @JsonIgnore
    private String rootUrl;
    private String soapVersion;
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

    public String getSoapVersion() {
        return soapVersion;
    }

    public void setSoapVersion(String soapVersion) {
        this.soapVersion = soapVersion;
    }
}
