/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.IdentityProvider;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.IdentityProvider.IdentityProviderType;
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

class IdentityProviderLoaderTest {

    private IdentityProviderLoader loader = new IdentityProviderLoader();

    @Test
    void load() {
        Bundle bundle = new Bundle();
        loader.load(bundle, createIDPXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));

        assertFalse(bundle.getIdentityProviders().isEmpty());
        assertEquals(1, bundle.getIdentityProviders().size());
        assertNotNull(bundle.getIdentityProviders().get("Test"));

        IdentityProvider entity = bundle.getIdentityProviders().get("Test");
        assertNotNull(entity);
        assertEquals(IdentityProviderType.BIND_ONLY_LDAP, entity.getType());
        assertPropertiesContent(ImmutableMap.of(
                "Prop", "Value",
                "Gateway", "7layer"
        ), entity.getProperties());
    }

    private static Element createIDPXml(Document document) {
        Element cassandraElement = createElementWithAttributesAndChildren(
                document,
                ID_PROV,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithTextContent(document, NAME, "Test"),
                createElementWithTextContent(document, ID_PROV_TYPE, IdentityProviderType.BIND_ONLY_LDAP.getValue()),
                buildPropertiesElement(
                        ImmutableMap.of(
                                "Prop", "Value",
                                "Gateway", "7layer"
                        ),
                        document
                )
        );

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.ID_PROVIDER_CONFIG_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        cassandraElement
                )
        );
    }
}