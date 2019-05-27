package com.ca.apim.gateway.cagatewayconfig.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.io.FilenameUtils;

import javax.inject.Named;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * An object to contain all related soap service extra configurable resources.
 */
@Named("SOAP_RESOURCE")
@JsonInclude(NON_NULL)
public class SoapResource extends Folderable {

    public static final String TYPE_WSDL = "wsdl";
    public static final String TYPE_XSD = "xmlschema";
    public static final String WSDL_EXTENSION = ".wsdl";
    public static final String XSD_EXTENSION = ".xsd";

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

        switch (extension) {
            case WSDL_EXTENSION:
                setType(TYPE_WSDL);
                return;
            case XSD_EXTENSION:
                setType(TYPE_XSD);
                return;
            default:
                throw new IllegalStateException("Unsupported Soap Resource file: " + this.getName());
        }
    }

    @JsonIgnore
    public String getBaseName() {
        return FilenameUtils.getBaseName(rootUrl);
    }

    @JsonIgnore
    public String getFileName() {
        String filename = FilenameUtils.getName(rootUrl);
        String extensionToCheck;
        switch (type) {
            case TYPE_WSDL:
                extensionToCheck = WSDL_EXTENSION;
                break;
            case TYPE_XSD:
                extensionToCheck = XSD_EXTENSION;
                break;
            default:
                throw new IllegalStateException("Unknown Soap Resource type: " + type);
        }
        if (!filename.endsWith(extensionToCheck)) {
            filename += extensionToCheck;
        }
        return filename;
    }

    @JsonIgnore
    public String getExtensionByType() {
        switch (type) {
            case TYPE_WSDL:
                return WSDL_EXTENSION;
            case TYPE_XSD:
                return XSD_EXTENSION;
            default:
                throw new IllegalStateException("Unknown Soap Resource type: " + type);
        }
    }
}
