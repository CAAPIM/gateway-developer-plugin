/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.xml;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.DependencyBundleLoadException;
import com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * Utility methods for XML handling.
 */
public class DocumentUtils {

    private DocumentUtils () {}

    public static Element getSingleElement(final Element entityItemElement, final String entityName) throws DocumentParseException {
        final NodeList folderNodes = entityItemElement.getElementsByTagName(entityName);
        if (folderNodes.getLength() < 1) {
            throw new DocumentParseException(entityName + " element not found");
        } else if (folderNodes.getLength() > 1) {
            throw new DocumentParseException("Multiple " + entityName + " elements found");
        } else {
            final Node folderNode = folderNodes.item(0);
            if (folderNode.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) folderNode;
            } else {
                throw new DocumentParseException("Unexpected " + entityName + " node discovered: " + folderNode.toString());
            }
        }
    }

    public static Element getSingleChildElement(final Element entityItemElement, final String elementName) {
        return getSingleChildElement(entityItemElement, elementName, false);
    }

    /**
     * Search in the children of the element specified a single element with the name specified .
     *
     * @param entityItemElement element to search into
     * @param elementName       element name to search
     * @param optional          if not found returns null instead of throwing exception
     * @return a single element found
     * @throws DependencyBundleLoadException if multiple found or invalid node type found (not element)
     */
    public static Element getSingleChildElement(final Element entityItemElement, final String elementName, boolean optional) {
        final NodeList childNodes = entityItemElement.getChildNodes();
        Node foundNode = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (elementName.equals(childNodes.item(i).getNodeName())) {
                if (foundNode == null) {
                    foundNode = childNodes.item(i);
                } else {
                    throw new DependencyBundleLoadException("Multiple " + elementName + " elements found");
                }
            }
        }
        if (foundNode == null) {
            if (optional) {
                return null;
            }

            throw new DependencyBundleLoadException(elementName + " element not found");
        }
        if (foundNode.getNodeType() == ELEMENT_NODE) {
            return (Element) foundNode;
        } else {
            throw new DependencyBundleLoadException("Unexpected " + elementName + " node discovered: " + foundNode.toString());
        }
    }

    /**
     * Search in the children of the element specified a single element with the name specified and returns its text content.
     *
     * @param entityItemElement element to search into
     * @param elementName       element name to search
     * @return text content from a single element found, null if no element found
     */
    public static String getSingleChildElementTextContent(final Element entityItemElement, final String elementName) {
        final Element element = getSingleChildElement(entityItemElement, elementName, true);
        if (element == null) {
            return null;
        }
        return element.getTextContent();
    }

    /**
     * Search in the children of the element specified all elements with the name specified and returns text contents from all of them.
     *
     * @param entityItemElement element to search into
     * @param elementName       element name to search
     * @return list of contents from elements found, empty if not found any
     */
    public static List<String> getChildElementsTextContents(final Element entityItemElement, final String elementName) {
        return getChildElements(entityItemElement, elementName).stream().map(Element::getTextContent).collect(toList());
    }

    /**
     * Search in the children of the element specified all elements with the name specified .
     *
     * @param entityItemElement element to search into
     * @param elementName       element name to search
     * @return list of elements found, empty if not found any
     */
    public static List<Element> getChildElements(final Element entityItemElement, final String elementName) {
        if (entityItemElement == null) {
            return emptyList();
        }

        final List<Element> elements = new ArrayList<>();
        final NodeList childNodes = entityItemElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (elementName.equals(child.getNodeName()) && child.getNodeType() == ELEMENT_NODE) {
                elements.add((Element) child);
            }
        }

        return elements;
    }

    public static Element createElementWithTextContent(final Document document, final String elementName, final Object textContent) {
        Element element = document.createElement(elementName);
        element.setTextContent(textContent != null ? textContent.toString() : EMPTY);
        return element;
    }

    public static Element createElementWithAttribute(final Document document, final String elementName, final String attributeName, final String attributeValue) {
        return createElementWithAttributes(document, elementName, ImmutableMap.of(attributeName, attributeValue));
    }

    public static Element createElementWithAttributes(final Document document, final String elementName, final Map<String, String> attributes) {
        Element element = document.createElement(elementName);
        attributes.forEach(element::setAttribute);
        return element;
    }

    public static Element createElementWithChildren(final Document document, final String elementName, final Element... child) {
        Element element = document.createElement(elementName);
        Stream.of(child).forEach(element::appendChild);
        return element;
    }
}
