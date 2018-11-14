/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.KeyStoreType;
import com.ca.apim.gateway.cagatewayconfig.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class PrivateKeyLoaderTest {

    @Test
    void load() {
        PrivateKeyLoader loader = new PrivateKeyLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, createXml(
                DocumentTools.INSTANCE.getDocumentBuilder().newDocument()
        ));

        assertFalse(bundle.getPrivateKeys().isEmpty());
        assertEquals(1, bundle.getPrivateKeys().size());
        PrivateKey privateKey = bundle.getPrivateKeys().get("alias");
        assertNotNull(privateKey);
        assertEquals("alias", privateKey.getName());
        assertEquals("alias", privateKey.getAlias());
        assertEquals(KeyStoreType.GENERIC, privateKey.getKeyStoreType());
        assertEquals(KeyStoreType.GENERIC.getName(), privateKey.getKeystore());
        assertEquals("EC", privateKey.getAlgorithm());
    }

    private static Element createXml(Document document) {
        Element element = createElementWithAttributesAndChildren(
                document,
                PRIVATE_KEY,
                ImmutableMap.of(ATTRIBUTE_ALIAS, "alias", ATTRIBUTE_KEYSTORE_ID, KeyStoreType.GENERIC.getId()),
                buildPropertiesElement(ImmutableMap.of("keyAlgorithm", "EC"), document)
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, KeyStoreType.GENERIC.generateKeyId("alias")),
                createElementWithTextContent(document, TYPE, EntityTypes.PRIVATE_KEY_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        element
                )
        );
    }
}