/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.ClientAuthentication;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.Feature;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.ListenPortTlsSettings;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.collections4.SetUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;

import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

class ListenPortLoaderTest {

    @Test
    void load() {
        ListenPortLoader loader = new ListenPortLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, buildListenPortElement(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true));

        assertFalse(bundle.getListenPorts().isEmpty());
        assertEquals(1, bundle.getListenPorts().size());

        ListenPort listenPort = bundle.getListenPorts().get("port");
        assertNotNull(listenPort);
        assertEquals(1234, listenPort.getPort());
        assertEquals("http", listenPort.getProtocol());
        assertTrue(SetUtils.isEqualSet(listenPort.getEnabledFeatures(), Stream.of(Feature.values()).map(Feature::name).collect(Collectors.toSet())));
        assertEquals("Service", listenPort.getTargetServiceReference());
        assertNotNull(listenPort.getTlsSettings());
        ListenPortTlsSettings tlsSettings = listenPort.getTlsSettings();
        Assertions.assertEquals(ClientAuthentication.REQUIRED, tlsSettings.getClientAuthentication());
        assertTrue(SetUtils.isEqualSet(tlsSettings.getEnabledCipherSuites(), ListenPort.DEFAULT_RECOMMENDED_CIPHERS));
        assertTrue(SetUtils.isEqualSet(tlsSettings.getEnabledVersions(), ListenPort.TLS_VERSIONS));
        assertEquals("Key", tlsSettings.getPrivateKey());
        assertPropertiesContent(Collections.emptyMap(), tlsSettings.getProperties());
        assertPropertiesContent(ImmutableMap.of("prop", "value"), listenPort.getProperties());
    }

    @Test
    void loadNoTLSSettings() {
        ListenPortLoader loader = new ListenPortLoader();
        Bundle bundle = new Bundle();
        loader.load(bundle, buildListenPortElement(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), false));

        assertFalse(bundle.getListenPorts().isEmpty());
        assertEquals(1, bundle.getListenPorts().size());

        ListenPort listenPort = bundle.getListenPorts().get("port");
        assertNotNull(listenPort);
        assertEquals(1234, listenPort.getPort());
        assertEquals("http", listenPort.getProtocol());
        assertTrue(SetUtils.isEqualSet(listenPort.getEnabledFeatures(), Stream.of(Feature.values()).map(Feature::name).collect(Collectors.toSet())));
        assertEquals("Service", listenPort.getTargetServiceReference());
        assertNull(listenPort.getTlsSettings());
        assertPropertiesContent(ImmutableMap.of("prop", "value"), listenPort.getProperties());
    }

    private static Element buildListenPortElement(Document document, boolean tlsSettings) {
        Element listenPortElement = document.createElement(LISTEN_PORT);

        Service service = new Service();
        service.setId("Service");

        listenPortElement.setAttribute(ATTRIBUTE_ID, "id");
        listenPortElement.appendChild(createElementWithTextContent(document, NAME, "port"));
        listenPortElement.appendChild(createElementWithTextContent(document, ENABLED, TRUE.toString())); // people should not bootstrap a disabled listen port.
        listenPortElement.appendChild(createElementWithTextContent(document, PROTOCOL, "http"));
        listenPortElement.appendChild(createElementWithTextContent(document, PORT, Integer.toString(1234)));

        Element enabledFeatures = document.createElement(ENABLED_FEATURES);
        Stream.of(Feature.values()).forEach(s -> enabledFeatures.appendChild(createElementWithTextContent(document, STRING_VALUE, s)));
        listenPortElement.appendChild(enabledFeatures);
        listenPortElement.appendChild(createElementWithAttribute(document, TARGET_SERVICE_REFERENCE, ATTRIBUTE_ID, service.getId()));

        if (tlsSettings) {
            Element tlsSettingsElement = document.createElement(TLS_SETTINGS);
            tlsSettingsElement.appendChild(createElementWithTextContent(document, CLIENT_AUTHENTICATION, ClientAuthentication.REQUIRED.getType()));

            Element enabledVersions = document.createElement(ENABLED_VERSIONS);
            ListenPort.TLS_VERSIONS.forEach(s -> enabledVersions.appendChild(createElementWithTextContent(document, STRING_VALUE, s)));
            tlsSettingsElement.appendChild(enabledVersions);

            Element enabledCipherSuites = document.createElement(ENABLED_CIPHER_SUITES);
            ListenPort.DEFAULT_RECOMMENDED_CIPHERS.forEach(s -> enabledCipherSuites.appendChild(createElementWithTextContent(document, STRING_VALUE, s)));
            tlsSettingsElement.appendChild(enabledCipherSuites);

            tlsSettingsElement.appendChild(createElementWithAttribute(document, PRIVATE_KEY_REFERENCE, ATTRIBUTE_ID, "Key"));
            listenPortElement.appendChild(tlsSettingsElement);
        }

        buildAndAppendPropertiesElement(ImmutableMap.of("prop", "value"), document, listenPortElement);
        return createElementWithChildren(
                document,
                ITEM,
                createElementWithTextContent(document, ID, "id"),
                createElementWithTextContent(document, TYPE, EntityTypes.LISTEN_PORT_TYPE),
                createElementWithChildren(
                        document,
                        RESOURCE,
                        listenPortElement
                )
        );
    }
}