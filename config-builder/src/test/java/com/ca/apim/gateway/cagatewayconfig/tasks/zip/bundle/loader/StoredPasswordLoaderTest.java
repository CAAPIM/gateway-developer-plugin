/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.StoredPassword;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class StoredPasswordLoaderTest {

    @Test
    void load() {
        StoredPasswordLoader loader = new StoredPasswordLoader();
        Bundle bundle = new Bundle();
        Element password = createStoredPasswordXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        loader.load(bundle, password);

        assertFalse(bundle.getStoredPasswords().isEmpty());
        assertEquals(1, bundle.getStoredPasswords().size());
        assertNotNull(bundle.getStoredPasswords().get("Test"));

        StoredPassword entity = bundle.getStoredPasswords().get("Test");
        assertNotNull(entity);
        assertEquals("Test", entity.getName());
        assertNull(entity.getPassword());
        assertPropertiesContent(ImmutableMap.of(
                "description", "gateway",
                "usageFromVariable", true
        ), entity.getProperties());
    }

    private static Element createStoredPasswordXml(Document document) {
        Element cassandraElement = createElementWithAttributesAndChildren(
                document,
                STORED_PASSWD,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithTextContent(document, NAME, "Test"),
                createElementWithTextContent(document, PASSWORD, "Test"),
                buildPropertiesElement(
                        ImmutableMap.of(
                                "description", "gateway",
                                "usageFromVariable", true
                        ),
                        document
                )
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.STORED_PASSWORD_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        cassandraElement
                )
        );
    }
}