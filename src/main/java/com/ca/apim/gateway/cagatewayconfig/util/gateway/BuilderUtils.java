/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;

public class BuilderUtils {

    public static Element buildPropertiesElement(final Map<String, Object> properties, final Document document) {
        Element propertiesElement = document.createElement(PROPERTIES);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Element propertyElement = document.createElement(PROPERTY);
            propertyElement.setAttribute("key", entry.getKey());
            String elementType = STRING_VALUE;

            if (Integer.class.isAssignableFrom(entry.getValue().getClass())) {
                elementType = INT_VALUE;
            } else if (Long.class.isAssignableFrom(entry.getValue().getClass())) {
                elementType = LONG_VALUE;
            } else if (Boolean.class.isAssignableFrom(entry.getValue().getClass())) {
                elementType = BOOLEAN_VALUE;
            }

            Element valueElement = document.createElement(elementType);
            valueElement.setTextContent(entry.getValue().toString());
            propertyElement.appendChild(valueElement);
            propertiesElement.appendChild(propertyElement);
        }
        return propertiesElement;
    }

    private BuilderUtils() {
    }
}
