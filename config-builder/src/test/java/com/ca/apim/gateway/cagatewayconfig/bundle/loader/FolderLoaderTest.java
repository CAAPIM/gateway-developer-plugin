/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Folder;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Paths;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class FolderLoaderTest {

    private static final String TEST_FOLDER_1 = "Folder1";
    private static final String TEST_FOLDER_2 = "Folder2";
    private FolderLoader loader = new FolderLoader();

    @Test
    void loadWithoutParent() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        assertThrows(BundleLoadException.class, () -> loader.load(bundle, createFolderXml(doc, TEST_FOLDER_1, TEST_FOLDER_1, Folder.ROOT_FOLDER_ID)));
    }

    @Test
    void loadWithMoreThanOneParent() {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        Folder f1 = new Folder();
        f1.setId(TEST_FOLDER_1);
        bundle.getFolders().put(TEST_FOLDER_1, f1);
        bundle.getFolders().put(TEST_FOLDER_1 + "_1", f1);

        assertThrows(BundleLoadException.class, () -> loader.load(bundle, createFolderXml(doc, TEST_FOLDER_2, TEST_FOLDER_2, TEST_FOLDER_1)));
    }

    @Test
    void loadWithInvalidCharName() throws UnsupportedEncodingException {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        final String encodedRoot = URLEncoder.encode(Folder.ROOT_FOLDER_NAME, "UTF-8");
        final String encodedTestFolder =  URLEncoder.encode(TEST_FOLDER_1 + "/1", "UTF-8");
        loader.load(bundle, createFolderXml(doc, Folder.ROOT_FOLDER_NAME, Folder.ROOT_FOLDER_ID, null));
        loader.load(bundle, createFolderXml(doc, TEST_FOLDER_1 + "/1", TEST_FOLDER_1, Folder.ROOT_FOLDER_ID));
        assertNotNull(bundle.getFolders().get(encodedRoot));
        assertEquals(encodedRoot, bundle.getFolders().get(encodedRoot).getName());
        assertNotNull(bundle.getFolders().get(encodedTestFolder));
        assertEquals(encodedTestFolder, bundle.getFolders().get(encodedTestFolder).getName());
    }

    @Test
    void load() throws UnsupportedEncodingException {
        Document doc = DocumentTools.INSTANCE.getDocumentBuilder().newDocument();
        Bundle bundle = new Bundle();
        loader.load(bundle, createFolderXml(doc, Folder.ROOT_FOLDER_NAME, Folder.ROOT_FOLDER_ID, null));
        loader.load(bundle, createFolderXml(doc, TEST_FOLDER_1, TEST_FOLDER_1, Folder.ROOT_FOLDER_ID));
        loader.load(bundle, createFolderXml(doc, TEST_FOLDER_2, TEST_FOLDER_2, TEST_FOLDER_1));

        assertFalse(bundle.getFolders().isEmpty());
        assertEquals(3, bundle.getFolders().size());

        final String encodedRoot = URLEncoder.encode(Folder.ROOT_FOLDER_NAME, "UTF-8");
        Folder root = bundle.getFolders().get(encodedRoot);
        assertNotNull(root);
        Assertions.assertEquals(Folder.ROOT_FOLDER_ID, root.getId());
        Assertions.assertEquals(encodedRoot, root.getName());
        assertNull(root.getParentFolder());
        Assertions.assertEquals(encodedRoot, root.getPath());

        Folder folder1 = bundle.getFolders().get(TEST_FOLDER_1);
        assertNotNull(folder1);
        assertEquals(TEST_FOLDER_1, folder1.getId());
        assertEquals(TEST_FOLDER_1, folder1.getName());
        assertEquals(root, folder1.getParentFolder());
        assertEquals(TEST_FOLDER_1, folder1.getPath());

        String f2Path = Paths.get(TEST_FOLDER_1, TEST_FOLDER_2).toString();
        Folder folder2 = bundle.getFolders().get(f2Path);
        assertNotNull(folder2);
        assertEquals(TEST_FOLDER_2, folder2.getId());
        assertEquals(TEST_FOLDER_2, folder2.getName());
        assertEquals(folder1, folder2.getParentFolder());
        assertEquals(f2Path, folder2.getPath());
    }

    private static Element createFolderXml(Document document, String folderName, String folderID, String parentFolderID) {
        Element element = createElementWithAttributesAndChildren(
                document,
                FOLDER,
                ImmutableMap.of(ATTRIBUTE_ID, folderID),
                createElementWithTextContent(document, NAME, folderName)
        );

        if (parentFolderID != null) {
            element.setAttribute(ATTRIBUTE_FOLDER_ID, parentFolderID);
        }

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, folderID),
                createElementWithTextContent(document, TYPE, EntityTypes.FOLDER_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        element
                )
        );
    }
}