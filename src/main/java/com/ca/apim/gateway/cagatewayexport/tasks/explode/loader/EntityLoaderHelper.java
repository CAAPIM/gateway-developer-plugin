/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleBuilderException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.getChildElements;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.w3c.dom.Node.ELEMENT_NODE;

public final class EntityLoaderHelper {

    private EntityLoaderHelper() {
    }

    /**
     * Map a l7:Properties element values into a Map of key-value objects.
     *
     * @param propertiesElement properties element of bundle (l7:Properties)
     * @return map of properties found into element, empty if null or no properties
     * @throws BundleBuilderException if node is not l7:Properties, if there is any l7:Property without any l7:xxxValue and if the l7:xxxValue is not yet supported.
     */
    static Map<String, Object> mapPropertiesElements(final Element propertiesElement) {
        if (propertiesElement == null) {
            return emptyMap();
        }

        if (!Objects.equals(propertiesElement.getNodeName(), PROPERTIES)) {
            throw new BundleBuilderException("Current node is not l7:Properties node, it is " + propertiesElement.getNodeName());
        }

        final List<Element> properties = getChildElements(propertiesElement, PROPERTY);
        return properties.stream().collect(toMap(s -> s.getAttribute("key"), o -> {
            final String propKey = o.getAttribute("key");
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
            case INT_VALUE: return parseInt(valueElement.getTextContent());
            case LONG_VALUE: return parseLong(valueElement.getTextContent());
            default:
                throw new BundleBuilderException("Type of property " + key + " is " + valueElement.getNodeName() + " which is not yet supported");
        }
    }
}
