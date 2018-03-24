package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

public class Policy {
    private String type;
    private String path;
    private String policyXML;
    private String name;
    private Folder parentFolder;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPolicyXML() {
        return policyXML;
    }

    public void setPolicyXML(String policyXML) {
        this.policyXML = policyXML;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Folder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }
}
