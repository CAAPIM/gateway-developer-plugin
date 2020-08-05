package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import javax.inject.Named;

import java.io.File;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Named("UNSUPPORTED")
@ConfigurationFile(name = "unsupported-entities", type = JSON_YAML)
@EnvironmentType("UNSUPPORTED")
public class UnsupportedGatewayEntity extends GatewayEntity {
    private String type;
    private String id;
    private boolean excluded;

    @JsonIgnore
    private Element element;

    public String getType() {
        return type;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    @JsonIgnore
    public String getMappingValue() {
        return getMappingValue(getType(), getName());
    }

    public void postLoad(String entityKey, Bundle bundle, @Nullable File rootFolder, IdGenerator idGenerator) {
        final String prefix = getType() + "/";
        if (entityKey.startsWith(prefix)) {
            setName(entityKey.substring(prefix.length()));
        }
    }

    @JsonIgnore
    public static String getMappingValue(final String type, final String name) {
        return type + "/" + name;
    }
}
