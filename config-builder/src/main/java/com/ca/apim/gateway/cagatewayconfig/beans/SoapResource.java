package com.ca.apim.gateway.cagatewayconfig.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.io.FilenameUtils;

import javax.inject.Named;

import static com.ca.apim.gateway.cagatewayconfig.beans.SoapResourceType.WSDL;
import static com.ca.apim.gateway.cagatewayconfig.beans.SoapResourceType.XMLSCHEMA;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * An object to contain all related soap service extra configurable resources.
 */
@Named("SOAP_RESOURCE")
@JsonInclude(NON_NULL)
public class SoapResource extends Folderable {

    private String rootUrl;
    private String type;
    @JsonIgnore
    private String content;

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTypeByExtension(String extension) {
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }

        if (WSDL.getExtension().equals(extension)) {
            setType(WSDL.getType());
            return;
        }

        if (XMLSCHEMA.getExtension().equals(extension)) {
            setType(XMLSCHEMA.getType());
            return;
        }

        throw new IllegalStateException("Unsupported Soap Resource file: " + this.getName());
    }

    @JsonIgnore
    public String getBaseName() {
        return FilenameUtils.getBaseName(rootUrl);
    }

    @JsonIgnore
    public String getFileName() {
        String filename = FilenameUtils.getName(rootUrl);
        String extensionToCheck;

        if (WSDL.getType().equals(type)) {
            extensionToCheck = WSDL.getExtension();
        } else if (XMLSCHEMA.getType().equals(type)) {
            extensionToCheck = XMLSCHEMA.getExtension();
        } else {
            throw new IllegalStateException("Unknown Soap Resource type: " + type);
        }

        if (!filename.endsWith(extensionToCheck)) {
            filename += extensionToCheck;
        }
        return filename;
    }

    @JsonIgnore
    public String getExtensionByType() {
        if (WSDL.getType().equals(type)) {
            return WSDL.getExtension();
        }

        if (XMLSCHEMA.getType().equals(type)) {
            return XMLSCHEMA.getExtension();
        }

        throw new IllegalStateException("Unknown Soap Resource type: " + type);
    }
}
