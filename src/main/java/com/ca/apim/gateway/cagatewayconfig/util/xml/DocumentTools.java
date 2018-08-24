/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.xml;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader.DependencyBundleLoadException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.GradleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * Tools used to parse and process XML documents
 */
public class DocumentTools {
    public static final DocumentTools INSTANCE = new DocumentTools();

    private final DocumentBuilder builder;
    private final XPathFactory xPathFactory;
    private final TransformerFactory transformerFactory;

    public DocumentTools() {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            builder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new DocumentToolsException("Unexpected exception creating DocumentBuilder", e);
        }

        xPathFactory = XPathFactory.newInstance();
        transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);
    }

    public Transformer getTransformer() {
        try {
            return configureTransformer(transformerFactory.newTransformer());
        } catch (TransformerConfigurationException e) {
            throw new GradleException("Exception loading stylesheet.", e);
        }
    }

    private Transformer configureTransformer(final Transformer transformer) {
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        return transformer;
    }

    public DocumentBuilder getDocumentBuilder() {
        return builder;
    }

    public Document parse(final File file) throws DocumentParseException {
        try {
            return parse(FileUtils.openInputStream(file));
        } catch (IOException e) {
            throw new DocumentParseException("Exception reading file: " + file, e);
        }
    }

    public Document parse(String string) throws DocumentParseException {
        try {
            return parse(IOUtils.toInputStream(string, "UTF-8"));
        } catch (IOException e) {
            throw new DocumentParseException("Exception creating stream from a String", e);
        }
    }

    /**
     * Parses an input stream into a document object
     *
     * @param inputStream The input stream to parse into a document
     * @return The parsed document
     * @throws DocumentParseException Thrown if there is an exception while parsing the document
     */
    private synchronized Document parse(final InputStream inputStream) throws DocumentParseException {
        try {
            return builder.parse(inputStream);
        } catch (SAXException e) {
            throw new DocumentParseException("Exception parsing document from input stream", e);
        } catch (IOException e) {
            throw new DocumentParseException("Exception reading document from input stream", e);
        }
    }

    public Element getSingleElement(final Element entityItemElement, final String entityName) throws DocumentParseException {
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
     * @param elementName element name to search
     * @param optional if not found returns null instead of throwing exception
     * @return a single element found
     * @throws DependencyBundleLoadException if multiple found or invalid node type found (not element)
     */
    public static Element getSingleChildElement(final Element entityItemElement, final String elementName, boolean optional) {
        final NodeList childNodes = entityItemElement.getChildNodes();
        Node foundNode = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            if(elementName.equals(childNodes.item(i).getNodeName())){
                if(foundNode == null) {
                    foundNode = childNodes.item(i);
                } else {
                    throw new DependencyBundleLoadException("Multiple " + elementName + " elements found");
                }
            }
        }
        if (foundNode == null){
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
     * @param elementName element name to search
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
     * @param elementName element name to search
     * @return list of contents from elements found, empty if not found any
     */
    public static List<String> getChildElementsTextContents(final Element entityItemElement, final String elementName) {
        return getChildElements(entityItemElement, elementName).stream().map(Element::getTextContent).collect(toList());
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
     * Returns a new xPath that can be used to query a document
     *
     * @return an xPath that can be used to query a document
     */
    private XPath newXPath() {
        return xPathFactory.newXPath();
    }

    public void cleanup(final Document bundleDocument) {
        bundleDocument.normalize();
        final XPath xPath = newXPath();
        final NodeList nodeList;
        try {
            nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
                    bundleDocument,
                    XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new DocumentToolsException("Unexpected error evaluating xpath.", e);
        }

        for (int i = 0; i < nodeList.getLength(); ++i) {
            final Node node = nodeList.item(i);
            node.getParentNode().removeChild(node);
        }
    }

    public static Element createElementWithTextContent(final Document document, final String elementName, final Object textContent) {
        Element element = document.createElement(elementName);
        element.setTextContent(textContent != null ? textContent.toString() : EMPTY);
        return element;
    }

    public static Element createElementWithAttribute(final Document document, final String elementName, final String attributeName, final String attributeValue) {
        Element element = document.createElement(elementName);
        element.setAttribute(attributeName, attributeValue);
        return element;
    }
}
