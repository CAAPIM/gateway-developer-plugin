/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import org.w3c.dom.Element;

public class Entity {
    private String type;
    private String id;
    private Element xml;
    private String name;

    public Entity(String type, String name, String id, Element xml) {
        this.type = type;
        this.id = id;
        this.xml = xml;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Element getXml() {
        return xml;
    }

    public void setXml(Element xml) {
        this.xml = xml;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
