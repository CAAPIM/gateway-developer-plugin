/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ClientAuthentication;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.Feature;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ListenPortTlsSettings;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.collections4.SetUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.DEFAULT_RECOMMENDED_CIPHERS;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.TLS_VERSIONS;
import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
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
        assertEquals(ClientAuthentication.REQUIRED, tlsSettings.getClientAuthentication());
        assertTrue(SetUtils.isEqualSet(tlsSettings.getEnabledCipherSuites(), DEFAULT_RECOMMENDED_CIPHERS));
        assertTrue(SetUtils.isEqualSet(tlsSettings.getEnabledVersions(), TLS_VERSIONS));
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

        listenPortElement.setAttribute(ATTRIBUTE_ID, "id");
        listenPortElement.appendChild(createElementWithTextContent(document, NAME, "port"));
        listenPortElement.appendChild(createElementWithTextContent(document, ENABLED, TRUE.toString())); // people should not bootstrap a disabled listen port.
        listenPortElement.appendChild(createElementWithTextContent(document, PROTOCOL, "http"));
        listenPortElement.appendChild(createElementWithTextContent(document, PORT, Integer.toString(1234)));

        Element enabledFeatures = document.createElement(ENABLED_FEATURES);
        Stream.of(Feature.values()).forEach(s -> enabledFeatures.appendChild(createElementWithTextContent(document, STRING_VALUE, s)));
        listenPortElement.appendChild(enabledFeatures);
        listenPortElement.appendChild(createElementWithTextContent(document, TARGET_SERVICE_REFERENCE, "Service"));

        if (tlsSettings) {
            Element tlsSettingsElement = document.createElement(TLS_SETTINGS);
            tlsSettingsElement.appendChild(createElementWithTextContent(document, CLIENT_AUTHENTICATION, ClientAuthentication.REQUIRED.getType()));

            Element enabledVersions = document.createElement(ENABLED_VERSIONS);
            TLS_VERSIONS.forEach(s -> enabledVersions.appendChild(createElementWithTextContent(document, STRING_VALUE, s)));
            tlsSettingsElement.appendChild(enabledVersions);

            Element enabledCipherSuites = document.createElement(ENABLED_CIPHER_SUITES);
            DEFAULT_RECOMMENDED_CIPHERS.forEach(s -> enabledCipherSuites.appendChild(createElementWithTextContent(document, STRING_VALUE, s)));
            tlsSettingsElement.appendChild(enabledCipherSuites);

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