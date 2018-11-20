/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.xml;

import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadException;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.EMPTY;
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

    /**
     * Search in the children of the element specified a single element with the name specified.
     *
     * @param entityItemElement element to search into
     * @param elementName element name to search
     * @return a single element found
     * @throws BundleLoadException if not found, multiple found or invalid node type found (not element)
     */
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
     * @throws BundleLoadException if multiple found or invalid node type found (not element)
     */
    public static Element getSingleChildElement(final Element entityItemElement, final String elementName, boolean optional) {
        final NodeList childNodes = entityItemElement.getChildNodes();
        Node foundNode = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (elementName.equals(childNodes.item(i).getNodeName())) {
                if (foundNode == null) {
                    foundNode = childNodes.item(i);
                } else {
                    throw new BundleLoadException("Multiple " + elementName + " elements found");
                }
            }
        }
        if (foundNode == null) {
            if (optional) {
                return null;
            }

            throw new BundleLoadException(elementName + " element not found");
        }
        if (foundNode.getNodeType() == ELEMENT_NODE) {
            return (Element) foundNode;
        } else {
            throw new BundleLoadException("Unexpected " + elementName + " node discovered: " + foundNode.toString());
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
    public static Set<String> getChildElementsTextContents(final Element entityItemElement, final String elementName) {
        return getChildElements(entityItemElement, elementName).stream().map(Element::getTextContent).collect(toSet());
    }

    /**
     * Search in the children of the element specified all elements with the name specified
     * and returns the specified attribute from all of them.
     *
     * @param entityItemElement element to search into
     * @param elementName element name to search
     * @param attribute attribute to search
     * @return list of attribute value from elements found, empty if not found any
     */
    public static List<String> getChildElementAttributeValues(final Element entityItemElement, final String elementName, final String attribute) {
        return getChildElements(entityItemElement, elementName).stream().map(e -> e.getAttribute(attribute)).collect(toList());
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

    public static Element createElementWithChildren(final Document document, final String elementName, final Element... children) {
        Element element = document.createElement(elementName);
        Stream.of(children).forEach(element::appendChild);
        return element;
    }

    public static Element createElementWithAttributesAndChildren(final Document document, final String elementName, final Map<String, String> attributes, final Element... children) {
        Element element = document.createElement(elementName);
        attributes.forEach(element::setAttribute);
        Stream.of(children).forEach(element::appendChild);
        return element;
    }

    public static Element createElementWithAttributesAndTextContent(final Document document, final String elementName, final Map<String, String> attributes, final Object textContent) {
        Element element = document.createElement(elementName);
        attributes.forEach(element::setAttribute);
        element.setTextContent(textContent != null ? textContent.toString() : EMPTY);
        return element;
    }

    /**
     * Search in the children of the element specified a single element with the name specified and returns the value from the attribute specified.
     *
     * @param entityItemElement element to search into
     * @param elementName element name to search
     * @param attributeName attribute name to get value
     * @return value from the attribute found in a single element, null if element is not present or attribute is not present or its value is empty
     */
    public static String getSingleChildElementAttribute(final Element entityItemElement, final String elementName, final String attributeName) {
        Element element = getSingleChildElement(entityItemElement, elementName, true);
        if (element == null) {
            return null;
        }

        String attribute = element.getAttribute(attributeName);
        return attribute == null || attribute.isEmpty() ? null : attribute;
    }

    /**
     * Generate dom Element from a XML String.
     *
     * @param documentTools DocumentTools instance used for parsing
     * @param string xml String
     * @return Xml Element
     * @throws DocumentParseException if any errors
     */
    public static Element stringToXML(DocumentTools documentTools, String string) throws DocumentParseException {
        Document document = stringToXMLDocument(documentTools, string);
        return document.getDocumentElement();
    }

    /**
     * Generate dom Document from a XML String.
     *
     * @param documentTools DocumentTools instance used for parsing
     * @param string xml String
     * @return Xml Element
     * @throws DocumentParseException if any errors
     */
    public static Document stringToXMLDocument(DocumentTools documentTools, String string) throws DocumentParseException {
        Document document = documentTools.parse(string);
        documentTools.cleanup(document);
        return document;
    }

    /**
     * Returns a Iterable wrapper for {@link NodeList} to be used in foreach loops.
     *
     * @param nodeList node list
     * @return iterable wrapper
     */
    public static Iterable<Node> nodeList(@NotNull final NodeList nodeList) {
        return () -> new Iterator<Node>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < nodeList.getLength();
            }

            @Override
            public Node next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return nodeList.item(index++);
            }
        };
    }
}
