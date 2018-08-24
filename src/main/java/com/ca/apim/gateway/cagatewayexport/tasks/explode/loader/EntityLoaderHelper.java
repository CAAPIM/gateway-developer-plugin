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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang.BooleanUtils.toBoolean;
import static org.w3c.dom.Node.ELEMENT_NODE;

public final class EntityLoaderHelper {

    static final String ELEMENT_RESOURCE = "l7:Resource";
    static final String ELEMENT_NAME = "l7:Name";
    static final String ELEMENT_PROPERTIES = "l7:Properties";
    static final String ELEMENT_STRING_VALUE = "l7:StringValue";
    private static final String ELEMENT_BOOLEAN_VALUE = "l7:BooleanValue";
    private static final String ELEMENT_PROPERTY = "l7:Property";

    private EntityLoaderHelper() {
    }

    /**
     * Search in the children of the element specified a single element with the name specified.
     *
     * @param entityItemElement element to search into
     * @param entityName element name to search
     * @return a single element found
     * @throws BundleBuilderException if not found, multiple found or invalid node type found (not element)
     */
    public static Element getSingleElement(final Element entityItemElement, final String entityName) {
        final NodeList folderNodes = entityItemElement.getElementsByTagName(entityName);
        if (folderNodes.getLength() < 1) {
            throw new BundleBuilderException(entityName + " element not found");
        } else if (folderNodes.getLength() > 1) {
            throw new BundleBuilderException("Multiple " + entityName + " elements found");
        } else {
            final Node folderNode = folderNodes.item(0);
            if (folderNode.getNodeType() == ELEMENT_NODE) {
                return (Element) folderNode;
            } else {
                throw new BundleBuilderException("Unexpected " + entityName + " node discovered: " + folderNode.toString());
            }
        }
    }

    /**
     * Search in the children of the element specified a single element with the name specified.
     *
     * @param entityItemElement element to search into
     * @param elementName element name to search
     * @return a single element found
     * @throws BundleBuilderException if not found, multiple found or invalid node type found (not element)
     */
    public static Element getSingleChildElement(final Element entityItemElement, final String elementName) {
        return getSingleChildElement(entityItemElement, elementName, false);
    }

    /**
     * Search in the children of the element specified a single element with the name specified .
     *
     * @param entityItemElement element to search into
     * @param elementName element name to search
     * @param optional if not found returns null instead of throwing exception
     * @return a single element found
     * @throws BundleBuilderException if multiple found or invalid node type found (not element)
     */
    public static Element getSingleChildElement(final Element entityItemElement, final String elementName, boolean optional) {
        final NodeList childNodes = entityItemElement.getChildNodes();
        Node foundNode = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            if(elementName.equals(childNodes.item(i).getNodeName())){
                if(foundNode == null) {
                    foundNode = childNodes.item(i);
                } else {
                    throw new BundleBuilderException("Multiple " + elementName + " elements found");
                }
            }
        }
        if (foundNode == null){
            if (optional) {
                return null;
            }

            throw new BundleBuilderException(elementName + " element not found");
        }
        if (foundNode.getNodeType() == ELEMENT_NODE) {
            return (Element) foundNode;
        } else {
            throw new BundleBuilderException("Unexpected " + elementName + " node discovered: " + foundNode.toString());
        }
    }

    /**
     * Search in the children of the element specified a single element with the name specified and returns its text content.
     *
     * @param entityItemElement element to search into
     * @param elementName element name to search
     * @return text content from a single element found
     * @throws BundleBuilderException if multiple found or invalid node type found (not element)
     */
    static String getSingleChildElementTextContent(final Element entityItemElement, final String elementName) {
        return getSingleChildElement(entityItemElement, elementName).getTextContent();
    }
    /**
     * Search in the children of the element specified a single element with the name specified and returns the value from the attribute specified.
     *
     * @param entityItemElement element to search into
     * @param elementName element name to search
     * @param attributeName attribute name to get value
     * @return value from the attribute found in a single element, null if element is not present or attribute is not present or its value is empty
     */
    static String getSingleChildElementAttribute(final Element entityItemElement, final String elementName, final String attributeName) {
        Element element = getSingleChildElement(entityItemElement, elementName, true);
        if (element == null) {
            return null;
        }

        String attribute = element.getAttribute(attributeName);
        return attribute == null || attribute.isEmpty() ? null : attribute;
    }

    /**
     * Search in the children of the element specified all elements with the name specified .
     *
     * @param entityItemElement element to search into
     * @param elementName element name to search
     * @return list of elements found, empty if not found any
     */
    private static List<Element> getChildElements(final Element entityItemElement, final String elementName) {
        if (entityItemElement == null) {
            return emptyList();
        }

        final List<Element> elements = new ArrayList<>();
        final NodeList childNodes = entityItemElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (elementName.equals(child.getNodeName()) && child.getNodeType() == ELEMENT_NODE){
                elements.add((Element) child);
            }
        }

        return elements;
    }

    /**
     * Search in the children of the element specified all elements with the name specified and returns text contents from all of them.
     *
     * @param entityItemElement element to search into
     * @param elementName element name to search
     * @return list of contents from elements found, empty if not found any
     */
    static List<String> getChildElementsTextContents(final Element entityItemElement, final String elementName) {
        return getChildElements(entityItemElement, elementName).stream().map(Element::getTextContent).collect(toList());
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

        if (!Objects.equals(propertiesElement.getNodeName(), ELEMENT_PROPERTIES)) {
            throw new BundleBuilderException("Current node is not l7:Properties node, it is " + propertiesElement.getNodeName());
        }

        final List<Element> properties = getChildElements(propertiesElement, ELEMENT_PROPERTY);
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
            case ELEMENT_STRING_VALUE: return valueElement.getTextContent();
            case ELEMENT_BOOLEAN_VALUE: return toBoolean(valueElement.getTextContent());
            default:
                throw new BundleBuilderException("Type of property " + key + " is " + valueElement.getNodeName() + " which is not yet supported");
        }
    }
}
