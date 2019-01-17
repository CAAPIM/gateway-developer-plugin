/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.file;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TemporaryFolderExtension.class)
class DocumentFileUtilsTest {

    private TemporaryFolder rootProjectDir;
    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        rootProjectDir = temporaryFolder;
    }

    @Test
    void createFile() throws IOException {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "test.xml");
        DocumentFileUtils.INSTANCE.createFile(sampleElement(), path);

        assertTrue(path.toFile().exists());
        assertNotNull(readFileToString(path.toFile(), Charset.defaultCharset()));
    }

    @Test
    void createFileNonexistingPath() {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "test", "test.xml");
        assertThrows(DocumentFileUtilsException.class, () -> DocumentFileUtils.INSTANCE.createFile(sampleElement(), path));
    }

    @Test
    void createFileWithNamespace() throws IOException {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "test.xml");
        DocumentFileUtils.INSTANCE.createFile(sampleElement(), path, true);

        assertTrue(path.toFile().exists());
        String content = readFileToString(path.toFile(), Charset.defaultCharset());
        assertNotNull(content);
        assertTrue(content.contains("xmlns:l7"));
        assertTrue(content.contains("http://ns.l7tech.com/2010/04/gateway-management"));
    }

    @Test
    void createFolder() {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "test");
        DocumentFileUtils.INSTANCE.createFolder(path);
        assertTrue(path.toFile().exists());
        assertTrue(path.toFile().isDirectory());
    }

    @Test
    void createFolderWithInvalidPath() {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "test", "test1");
        assertThrows(DocumentFileUtilsException.class, () -> DocumentFileUtils.INSTANCE.createFolder(path));
    }

    @Test
    void createFolderWithFileSameName() throws IOException {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "test.f");
        Files.touch(path.toFile());
        assertThrows(DocumentFileUtilsException.class, () -> DocumentFileUtils.INSTANCE.createFolder(path));
    }

    @Test
    void createFolders() {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "test", "test1");
        DocumentFileUtils.INSTANCE.createFolders(path);
        assertTrue(path.toFile().exists());
        assertTrue(path.toFile().isDirectory());
        assertTrue(path.getParent().toFile().exists());
        assertTrue(path.getParent().toFile().isDirectory());
    }

    @Test
    void createFoldersExistingFile() throws IOException {
        Path path = Paths.get(rootProjectDir.getRoot().toPath().toString(), "test.f");
        Files.touch(path.toFile());
        assertThrows(DocumentFileUtilsException.class, () -> DocumentFileUtils.INSTANCE.createFolders(path));
    }

    @Test
    void elementToString() {
        String xml = DocumentTools.INSTANCE.elementToString(sampleElement());
        assertNotNull(xml);
    }

    private static Element sampleElement() {
        final Document document = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        return createElementWithAttributesAndChildren(document,
                CLUSTER_PROPERTY,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithTextContent(document, NAME, "Prop"),
                createElementWithTextContent(document, VALUE, "Value")
        );
    }
}