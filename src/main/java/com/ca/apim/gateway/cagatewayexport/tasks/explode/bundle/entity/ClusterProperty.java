/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import org.w3c.dom.Element;

public class ClusterProperty implements Entity {
    private final String name;
    private final String id;
    private final Element xml;
    private final String value;

    public ClusterProperty(final String name, String value, final String id, Element xml) {
        this.name = name;
        this.value = value;
        this.id = id;
        this.xml = xml;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public Element getXml() {
        return xml;
    }

    @Override
    public String toString() {
        return id + ":" + name;
    }
}
