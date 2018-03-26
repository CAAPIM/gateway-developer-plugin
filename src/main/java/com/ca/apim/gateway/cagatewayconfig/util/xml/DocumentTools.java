/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.xml;

import org.gradle.api.GradleException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Tools used to parse and process XML documents
 */
public class DocumentTools {
    public static final DocumentTools INSTANCE = new DocumentTools();

    private final DocumentBuilder builder;
    private final TransformerFactory transformerFactory;

    public DocumentTools() {
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            builder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new DocumentToolsException("Unexpected exception creating DocumentBuilder", e);
        }

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
}
