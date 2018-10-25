/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleBuilderException;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getChildElements;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.time.DateUtils.parseDateStrictly;
import static org.w3c.dom.Node.ELEMENT_NODE;

final class EntityLoaderHelper {

    private static final String DATE_VALUE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private EntityLoaderHelper() {
    }

    /**
     * Map a l7:Properties element values into a Map of key-value objects.
     *
     * @param propertiesElement properties element of bundle (l7:Properties)
     * @param propertiesElementName Name of the properties element to be validated
     * @return map of properties found into element, empty if null or no properties
     * @throws BundleBuilderException if node is not l7:Properties, if there is any l7:Property without any l7:xxxValue and if the l7:xxxValue is not yet supported.
     */
    static Map<String, Object> mapPropertiesElements(final Element propertiesElement, final String propertiesElementName) {
        if (propertiesElement == null) {
            return emptyMap();
        }

        if (!Objects.equals(propertiesElement.getNodeName(), propertiesElementName)) {
            throw new BundleBuilderException("Current node is not l7:Properties node, it is " + propertiesElement.getNodeName());
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

            throw new BundleBuilderException("Property " + propKey + " does not have a value");
        }));
    }

    private static Object extractPropertyValue(final String key, final Element valueElement) {
        switch (valueElement.getNodeName()) {
            case STRING_VALUE: return valueElement.getTextContent();
            case BOOLEAN_VALUE: return toBoolean(valueElement.getTextContent());
            case INTEGER_VALUE:
            case INT_VALUE: return parseInt(valueElement.getTextContent());
            case LONG_VALUE: return parseLong(valueElement.getTextContent());
            case DATE_VALUE: return parseDateFromString(key, valueElement.getTextContent());
            default:
                throw new BundleBuilderException("Type of property " + key + " is " + valueElement.getNodeName() + " which is not yet supported");
        }
    }

    @NotNull
    private static Date parseDateFromString(final String key, final String dateAsString) {
        try {
            return parseDateStrictly(dateAsString, DATE_VALUE_PATTERN);
        } catch (ParseException e) {
            throw new BundleBuilderException("Unable to parse date property (" + key + ") value: " + dateAsString);
        }
    }
}
