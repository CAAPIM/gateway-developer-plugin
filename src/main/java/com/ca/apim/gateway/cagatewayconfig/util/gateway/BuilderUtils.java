/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

public class BuilderUtils {

    public static Element buildPropertiesElement(final Map<String, Object> properties, final Document document) {
        Element propertiesElement = document.createElement("l7:Properties");
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Element propertyElement = document.createElement("l7:Property");
            propertyElement.setAttribute("key", entry.getKey());
            Element valueElement;
            if (Integer.class.isAssignableFrom(entry.getValue().getClass())) {
                valueElement = document.createElement("l7:IntValue");
            } else if (Long.class.isAssignableFrom(entry.getValue().getClass())) {
                valueElement = document.createElement("l7:LongValue");
            } else if (Boolean.class.isAssignableFrom(entry.getValue().getClass())) {
                valueElement = document.createElement("l7:BooleanValue");
            } else {
                valueElement = document.createElement("l7:StringValue");
            }
            valueElement.setTextContent(entry.getValue().toString());
            propertyElement.appendChild(valueElement);
            propertiesElement.appendChild(propertyElement);
        }
        return propertiesElement;
    }

    private BuilderUtils() {
    }
}
