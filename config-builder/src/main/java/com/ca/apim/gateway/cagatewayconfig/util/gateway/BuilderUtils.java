/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderException;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadException;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getChildElements;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.parseDateStrictly;
import static org.w3c.dom.Node.ELEMENT_NODE;

public class BuilderUtils {

    private static final String DATE_VALUE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static void buildAndAppendPropertiesElement(final Map<String, Object> properties, final Document document, final Element elementToAppendInto) {
        if (MapUtils.isEmpty(properties)) {
            return;
        }

        elementToAppendInto.appendChild(buildPropertiesElement(properties, document));
    }

    public static Element buildPropertiesElement(final Map<String, Object> properties, final Document document) {
        return buildPropertiesElement(properties, document, PROPERTIES);
    }

    public static Element buildPropertiesElement(final Map<String, Object> properties, final Document document, final String propertiesElementName) {
        Element propertiesElement = document.createElement(propertiesElementName);
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            // skip property if null value
            if (entry.getValue() == null) {
                continue;
            }

            propertiesElement.appendChild(createPropertyElement(document, entry.getKey(), entry.getValue()));
        }
        return propertiesElement;
    }

    @NotNull
    private static Element createPropertyElement(Document document, String key, Object value) {
        Element propertyElement = document.createElement(PROPERTY);
        propertyElement.setAttribute(ATTRIBUTE_KEY, key);
        String elementType;
        String elementValue = value.toString();

        if (String.class.isAssignableFrom(value.getClass())) {
            elementType = STRING_VALUE;
        } else if (Integer.class.isAssignableFrom(value.getClass())) {
            elementType = INTEGER_VALUE;
        } else if (Long.class.isAssignableFrom(value.getClass())) {
            elementType = LONG_VALUE;
        } else if (Boolean.class.isAssignableFrom(value.getClass())) {
            elementType = BOOLEAN_VALUE;
        } else if (Date.class.isAssignableFrom(value.getClass())) {
            elementType = DATE_VALUE;
            elementValue = format((Date) value, DATE_VALUE_PATTERN);
        } else {
            throw new EntityBuilderException("Could not create property (" + key + ") for value type: " + value.getClass().getTypeName());
        }

        Element valueElement = document.createElement(elementType);
        valueElement.setTextContent(elementValue);
        propertyElement.appendChild(valueElement);
        return propertyElement;
    }

    /**
     * Map a l7:Properties element values into a Map of key-value objects.
     *
     * @param propertiesElement properties element of bundle (l7:Properties)
     * @param propertiesElementName name of the node expected
     * @return map of properties found into element, empty if null or no properties
     * @throws BundleLoadException if node is not 'propertiesElementName', if there is any l7:Property without any l7:xxxValue and if the l7:xxxValue is not yet supported.
     */
    public static Map<String, Object> mapPropertiesElements(final Element propertiesElement, final String propertiesElementName) {
        if (propertiesElement == null) {
            return emptyMap();
        }

        if (!Objects.equals(propertiesElement.getNodeName(), propertiesElementName)) {
            throw new BundleLoadException("Current node is not " + propertiesElementName + " node, it is " + propertiesElement.getNodeName());
        }

        final List<Element> properties = getChildElements(propertiesElement, PROPERTY);
        return properties.stream().collect(toMap(s -> s.getAttribute(ATTRIBUTE_KEY), o -> {
            final String propKey = o.getAttribute(ATTRIBUTE_KEY);
            final NodeList childNodes = o.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node child = childNodes.item(i);
                if (child.getNodeType() == ELEMENT_NODE) {
                    return extractPropertyValue(propKey, (Element) child);
                }
            }

            throw new BundleLoadException("Property " + propKey + " does not have a value");
        }));
    }

    private static Object extractPropertyValue(final String key, final Element valueElement) {
        switch (valueElement.getNodeName()) {
            case STRING_VALUE: return valueElement.getTextContent();
            case BOOLEAN_VALUE: return toBoolean(valueElement.getTextContent());
            case LONG_VALUE: return parseLong(valueElement.getTextContent());
            case INTEGER_VALUE:
            case INT_VALUE: return parseInt(valueElement.getTextContent());
            case DATE_VALUE: return parseDateFromString(key, valueElement.getTextContent());
            default:
                throw new BundleLoadException("Type of property " + key + " is " + valueElement.getNodeName() + " which is not yet supported");
        }
    }

    @NotNull
    private static Date parseDateFromString(final String key, final String dateAsString) {
        try {
            return parseDateStrictly(dateAsString, DATE_VALUE_PATTERN);
        } catch (ParseException e) {
            throw new EntityBuilderException("Unable to parse date property (" + key + ") value: " + dateAsString);
        }
    }

    private BuilderUtils() {
    }
}
