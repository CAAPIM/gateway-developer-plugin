package com.ca.apim.gateway.cagatewayconfig.beans;

import org.w3c.dom.Element;

public class UnsupportedGatewayEntity extends GatewayEntity {
    private String type;
    private Element element;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }
}
