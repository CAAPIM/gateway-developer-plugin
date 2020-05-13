/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

public class Entity {

    private String type;
    private String id;
    private String guid;
    private Element xml;
    private String name;
    private String mappingAction;
    private Map<String, Object> mappingProperties = new HashMap<>();

    public Entity(String type, String name, String id, Element xml) {
        this(type, name, id, xml, null);
    }

    public Entity(String type, String name, String id, Element xml, String guid) {
        this.type = type;
        this.name = name;
        this.id = id;
        this.xml = xml;
        this.guid = guid;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Element getXml() {
        if (xml != null) {
            return (Element) xml.cloneNode(true);
        }
        return xml;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getMappingProperties() {
        return mappingProperties;
    }

    void setMappingProperty(String key, Object value) {
        mappingProperties.put(key, value);
    }

    public String getMappingAction() {
        return mappingAction;
    }

    void setMappingAction(String mappingAction) {
        this.mappingAction = mappingAction;
    }

    @JsonIgnore
    public Metadata getMetadata() {
        return new Metadata() {
            @Override
            public String getType() {
                return type;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getGuid() {
                return guid;
            }


        };
    }
}
