/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.xml;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleBuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * Utility methods to handle xml document elements.
 */
public class DocumentUtils {

    private DocumentUtils() {
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
        Document document = documentTools.parse(string);
        documentTools.cleanup(document);
        return document.getDocumentElement();
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
    public static String getSingleChildElementTextContent(final Element entityItemElement, final String elementName) {
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
    public static String getSingleChildElementAttribute(final Element entityItemElement, final String elementName, final String attributeName) {
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
    public static List<Element> getChildElements(final Element entityItemElement, final String elementName) {
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
    public static List<String> getChildElementsTextContents(final Element entityItemElement, final String elementName) {
        return getChildElements(entityItemElement, elementName).stream().map(Element::getTextContent).collect(toList());
    }
}
