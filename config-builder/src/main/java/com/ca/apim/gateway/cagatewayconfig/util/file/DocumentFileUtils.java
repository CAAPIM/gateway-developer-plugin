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

import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.closeQuietly;
import static java.nio.charset.StandardCharsets.UTF_8;

public class DocumentFileUtils {

    public static final DocumentFileUtils INSTANCE = new DocumentFileUtils(DocumentTools.INSTANCE);
    private final DocumentTools documentTools;

    private DocumentFileUtils(DocumentTools documentTools) {
        this.documentTools = documentTools;
    }

    public void createFile(Element element, Path path) {
        createFile(element, path, false);
    }

    public void createFile(Element element, Path path, boolean addNamespace) {
        OutputStream fos = null;
        try {
            fos = Files.newOutputStream(path);
            documentTools.printXML(element, fos, addNamespace);
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
                throw new DocumentFileUtilsException("Exception creating folder: " + folderPath, e);
            }
        } else if (!folderPath.toFile().isDirectory()) {
            throw new DocumentFileUtilsException("Wanted to create folder but found a file: " + folderPath);
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
                throw new DocumentFileUtilsException("Exception creating folder(s): " + folderPath, e);
            }
        } else if (!folderPath.toFile().isDirectory()) {
            throw new DocumentFileUtilsException("Wanted to create folder but found a file: " + folderPath);
        }
    }
}
