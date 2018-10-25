/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.file;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.WriteException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
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

    public void createFile(Element element, Path path, boolean addNamespace) {
        OutputStream fos = null;
        try {
            fos = Files.newOutputStream(path);
            printXML(element, fos, addNamespace);
        } catch (IOException e) {
            throw new DocumentFileUtilsException("Error writing to file '" + path + "': " + e.getMessage(), e);
        } finally {
            closeQuietly(fos);
        }
    }

    public synchronized void createFolder(Path folderPath) {
        if (!folderPath.toFile().exists()) {
            try {
                Files.createDirectory(folderPath);
            } catch (IOException e) {
                throw new WriteException("Exception creating folder: " + folderPath, e);
            }
        } else if (!folderPath.toFile().isDirectory()) {
            throw new WriteException("Wanted to create folder but found a file: " + folderPath);
        }
    }

    /**
     * Create all folder in this path. Does not fail if any of them already exist.
     *
     * @param folderPath Path representing all folders that should be created.
     */
    public synchronized void createFolders(Path folderPath) {
        if (!folderPath.toFile().exists()) {
            try {
                Files.createDirectories(folderPath);
            } catch (IOException e) {
                throw new WriteException("Exception creating folder(s): " + folderPath, e);
            }
        } else if (!folderPath.toFile().isDirectory()) {
            throw new WriteException("Wanted to create folder but found a file: " + folderPath);
        }
    }

    private void printXML(final Element node, final OutputStream outStream, boolean addNamespace) {
        if (addNamespace) {
            node.setAttribute("xmlns:l7", "http://ns.l7tech.com/2010/04/gateway-management");
        }

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
