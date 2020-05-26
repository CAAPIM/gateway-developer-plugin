/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

public class Entity {

    public static final String PROPERTY_BUNDLE_ENTITY_NAME = "bundleName";
    public static final String PROPERTY_GUID = "guid";
    public static final String PROPERTY_HAS_ROUTING = "hasRouting";

    private final String type;
    private final String id;
    private final Element xml;
    private final String originalName;
    private final Map<String, Object> properties = new HashMap<>();
    private String mappingAction;
    private final Map<String, Object> mappingProperties = new HashMap<>();

    public Entity(String type, String originalName, String id, Element xml) {
        this.type = type;
        this.originalName = originalName;
        this.id = id;
        this.xml = xml;
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

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        if (properties != null) {
            this.properties.clear();
            this.properties.putAll(properties);
        }
    }

    public String getGuid() {
        return (String) properties.get(PROPERTY_GUID);
    }

    public String getName() {
        return ObjectUtils.firstNonNull((String) properties.get(PROPERTY_BUNDLE_ENTITY_NAME), originalName);
    }

    public String getOriginalName() {
        return originalName;
    }

    public boolean isHasRouting() {
        return BooleanUtils.toBooleanDefaultIfNull((Boolean) properties.get(PROPERTY_HAS_ROUTING), false);
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
                return Entity.this.getName();
            }

            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getGuid() {
                return Entity.this.getGuid();
            }
        };
    }
}
