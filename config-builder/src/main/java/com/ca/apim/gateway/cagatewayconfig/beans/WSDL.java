package com.ca.apim.gateway.cagatewayconfig.beans;

public class WSDL {

    private String url;
    private String soapVersion;
    private String wsdlXml;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
