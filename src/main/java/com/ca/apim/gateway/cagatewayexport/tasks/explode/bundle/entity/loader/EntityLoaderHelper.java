/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleBuilderException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class EntityLoaderHelper {
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
            if (folderNode.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) folderNode;
            } else {
                throw new BundleBuilderException("Unexpected " + entityName + " node discovered: " + folderNode.toString());
            }
        }
    }

    public static Element getSingleChildElement(final Element entityItemElement, final String elementName) {
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
        if(foundNode == null){
            throw new BundleBuilderException(elementName + " element not found");
        }
        if (foundNode.getNodeType() == Node.ELEMENT_NODE) {
            return (Element) foundNode;
        } else {
            throw new BundleBuilderException("Unexpected " + elementName + " node discovered: " + foundNode.toString());
        }
    }
}
