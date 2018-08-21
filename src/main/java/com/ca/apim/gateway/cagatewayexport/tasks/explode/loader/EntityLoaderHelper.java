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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.w3c.dom.Node.ELEMENT_NODE;

public final class EntityLoaderHelper {

    public static final String ELEMENT_RESOURCE = "l7:Resource";
    public static final String ELEMENT_NAME = "l7:Name";
    public static final String ELEMENT_PROPERTIES = "l7:Properties";
    public static final String ELEMENT_STRING_VALUE = "l7:StringValue";
    public static final String ELEMENT_BOOLEAN_VALUE = "l7:BooleanValue";
    public static final String ELEMENT_PROPERTY = "l7:Property";

    private EntityLoaderHelper() {
    }

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

    public static Element getSingleChildElement(final Element entityItemElement, final String elementName) {
        return getSingleChildElement(entityItemElement, elementName, false);
    }

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

    public static String getSingleChildElementTextContent(final Element entityItemElement, final String elementName) {
        return getSingleChildElement(entityItemElement, elementName).getTextContent();
    }

    public static List<Element> getChildElements(final Element entityItemElement, final String elementName) {
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

    public static List<String> getChildElementsTextContents(final Element entityItemElement, final String elementName) {
        return getChildElements(entityItemElement, elementName).stream().map(Element::getTextContent).collect(toList());
    }

    public static Map<String, Object> mapPropertiesElements(final Element propertiesElement) {
        if (propertiesElement == null) {
            return emptyMap();
        }

        if (!Objects.equals(propertiesElement.getNodeName(), ELEMENT_PROPERTIES)) {
            throw new BundleBuilderException("Current node is not l7:Properties node, it is " + propertiesElement.getNodeName());
        }

        final List<Element> properties = getChildElements(propertiesElement, ELEMENT_PROPERTY);
        return properties.stream().collect(toMap(s -> s.getAttribute("key"), o -> {
            final NodeList childNodes = o.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node child = childNodes.item(i);
                if ((ELEMENT_STRING_VALUE.equals(child.getNodeName()) || ELEMENT_BOOLEAN_VALUE.equals(child.getNodeName())) && child.getNodeType() == ELEMENT_NODE) {
                    return child;
                }
            }

            throw new BundleBuilderException("Property " + o.getAttribute("key") + " does not have a valid value of type " + ELEMENT_BOOLEAN_VALUE + " or " + ELEMENT_STRING_VALUE);
        }));
    }
}
