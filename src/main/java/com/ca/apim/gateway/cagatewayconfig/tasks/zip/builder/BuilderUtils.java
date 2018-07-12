/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

class BuilderUtils {

    static Element buildPropertiesElement(final Map<String,Object> properties, final Document document) {
        Element propertiesElement = document.createElement("l7:Properties");
        for (String key: properties.keySet()) {
            Element propertyElement = document.createElement("l7:Property");
            propertyElement.setAttribute("key", key);
            Element valueElement;
            Object value = properties.get(key);
            if(Integer.class.isAssignableFrom(value.getClass())){
                valueElement = document.createElement("l7:IntValue");
            } else if(Integer.class.isAssignableFrom(value.getClass())){
                valueElement = document.createElement("l7:LongValue");
            } else if(Integer.class.isAssignableFrom(value.getClass())){
                valueElement = document.createElement("l7:BooleanValue");
            } else {
                valueElement = document.createElement("l7:StringValue");
            }
            valueElement.setTextContent(value.toString());
            propertyElement.appendChild(valueElement);
            propertiesElement.appendChild(propertyElement);
        }
        return propertiesElement;
    }
}
