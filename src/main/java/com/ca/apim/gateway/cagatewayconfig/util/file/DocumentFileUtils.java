/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.file;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.w3c.dom.Element;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DocumentFileUtils {
    private static final Logger LOGGER = Logger.getLogger(DocumentFileUtils.class.getName());

    public static final DocumentFileUtils INSTANCE = new DocumentFileUtils(DocumentTools.INSTANCE);
    private final DocumentTools documentTools;

    public DocumentFileUtils(DocumentTools documentTools) {
        this.documentTools = documentTools;
    }

    public void createFile(Element element, Path path) {
        OutputStream fos = null;
        try {
            fos = Files.newOutputStream(path);
            printXML(element, fos);
        } catch (IOException e) {
            throw new DocumentFileUtilsException("Error writing to file '" + path + "': " + e.getMessage(), e);
        } finally {
            closeQuietly(fos);
        }
    }

    public String elementToString(Element element) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        printXML(element, byteArrayOutputStream);
        try {
            return byteArrayOutputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new DocumentFileUtilsException("Error writing xml: " + e.getMessage(), e);
        } finally {
            closeQuietly(byteArrayOutputStream);
        }
    }

    private void printXML(final Element node, final OutputStream outStream) {
        final Transformer transformer = documentTools.getTransformer();
        final OutputStreamWriter writer;
        try {
            writer = new OutputStreamWriter(outStream, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new DocumentFileUtilsException("Unexpected exception creating output stream writer", e);
        }
        try {
            transformer.transform(new DOMSource(node),
                    new StreamResult(writer));
        } catch (TransformerException e) {
            throw new DocumentFileUtilsException("Exception writing xml element to stream.", e);
        }
    }


    /**
     * Close a {@link java.io.Closeable} without throwing any exceptions.
     *
     * @param closeable the object to close.
     */
    private static void closeQuietly(java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioe) {
                LOGGER.log(Level.INFO, "IO error when closing closeable '" + ioe.getMessage() + "'");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unexpected error when closing object", e);
            }
        }
    }
}
