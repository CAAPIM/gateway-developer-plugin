/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.ClientAuthentication;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.Feature;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.ListenPortTlsSettings;
import com.ca.apim.gateway.cagatewayconfig.beans.PrivateKey;
import com.ca.apim.gateway.cagatewayconfig.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.bundle.loader.BundleLoadException;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.util.TestUtils.assertPropertiesContent;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.lang.Boolean.TRUE;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class ListenPortEntityBuilderTest {

    private static final int TEST_HTTP_PORT = 12345;
    private static final String TEST_HTTP_PORT_NAME = "Test HTTP Port 12345";
    private static final String SERVICE_REFERENCE = "service-reference";
    private static final String PRIVATE_KEY = "private-key";
    private static final IdGenerator ID_GENERATOR = new IdGenerator();

    @Test
    void buildFromEmptyDeploymentBundle() {
        ListenPortEntityBuilder builder = new ListenPortEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        final List<Entity> listenPortEntities = builder.build(bundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(listenPortEntities.isEmpty());
    }

    @Test
    void buildFromEmptyEnvironmentBundle() {
        ListenPortEntityBuilder builder = new ListenPortEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        final List<Entity> listenPortEntities = builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        // only two defaults
        assertEquals(2, listenPortEntities.size(), "Expecting only the 2 default ports");

        // check 8080 and 8443
        checkExpectedPorts(listenPortEntities, ListenPort.HTTP_DEFAULT_PORT, ListenPort.HTTPS_DEFAULT_PORT);
    }

    @Test
    void buildWithCustomPortDeploymentBundle_expectCustomOnly() {
        ListenPortEntityBuilder builder = new ListenPortEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        addPortToBundle(bundle, buildPort());

        final List<Entity> listenPortEntities = builder.build(bundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        // two defaults and custom one
        assertEquals(1, listenPortEntities.size(),"More than 1 expected ports");

        // check
        checkExpectedPorts(listenPortEntities, TEST_HTTP_PORT);
    }


    @Test
    void buildWithCustomPortEnvironmentBundle_expectCustomAndDefaults() {
        ListenPortEntityBuilder builder = new ListenPortEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        addPortToBundle(bundle, buildPort());

        final List<Entity> listenPortEntities = builder.build(bundle, BundleType.ENVIRONMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        // two defaults and custom one
        assertEquals(3, listenPortEntities.size(),"More than 3 expected ports");

        // check
        checkExpectedPorts(listenPortEntities, ListenPort.HTTP_DEFAULT_PORT, ListenPort.HTTPS_DEFAULT_PORT, TEST_HTTP_PORT);
    }

    @Test
    void buildWithCustomPortReferencingService_expectedException() {
        ListenPortEntityBuilder builder = new ListenPortEntityBuilder(ID_GENERATOR);
        final Bundle bundle = new Bundle();
        addPortToBundle(bundle, buildPortWithServiceRef());

        assertThrows(BundleLoadException.class, () -> builder.build(bundle, BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument()));
    }

    @Test
    void checkDefaultPortsAreCreated() {
        final Map<String, ListenPort> defaultListenPorts = ListenPortEntityBuilder.createDefaultListenPorts();

        assertNotNull(defaultListenPorts.get(ListenPort.DEFAULT_HTTP_8080), "Missing default HTTP");
        assertNotNull(defaultListenPorts.get(ListenPort.DEFAULT_HTTPS_8443), "Missing default HTTPS");
    }

    @Test
    void checkDefaultHttpPortConfiguration() {
        ListenPort defaultHttp = ListenPortEntityBuilder.createDefaultHttp();
        Assertions.assertEquals(ListenPort.HTTP_DEFAULT_PORT.intValue(), defaultHttp.getPort());
        Assertions.assertEquals(ListenPort.PROTOCOL_HTTP, defaultHttp.getProtocol());
        assertTrue(defaultHttp.getEnabledFeatures().containsAll(asList(Feature.MESSAGE_INPUT.getDescription(),
                Feature.POLICYDISCO.getDescription(),
                Feature.PING.getDescription(),
                Feature.STS.getDescription(),
                Feature.WSDLPROXY.getDescription(),
                Feature.SNMPQUERY.getDescription())));
    }

    @Test
    void checkDefaultHttpsPortConfiguration() {
        ListenPort defaultHttps = ListenPortEntityBuilder.createDefaultHttps();
        Assertions.assertEquals(ListenPort.HTTPS_DEFAULT_PORT.intValue(), defaultHttps.getPort());
        Assertions.assertEquals(ListenPort.PROTOCOL_HTTPS, defaultHttps.getProtocol());
        assertTrue(defaultHttps.getEnabledFeatures().containsAll(asList(Feature.MESSAGE_INPUT.getDescription(),
                Feature.ADMIN_REMOTE.getDescription(),
                Feature.ADMIN_APPLET.getDescription(),
                Feature.OTHER_SERVLETS.getDescription())));
        assertNotNull(defaultHttps.getTlsSettings());
        Assertions.assertEquals(ClientAuthentication.OPTIONAL, defaultHttps.getTlsSettings().getClientAuthentication());
        assertTrue(defaultHttps.getTlsSettings().getEnabledVersions().containsAll(ListenPort.TLS_VERSIONS));
        assertTrue(defaultHttps.getTlsSettings().getEnabledCipherSuites().containsAll(ListenPort.DEFAULT_RECOMMENDED_CIPHERS));
        assertNotNull(defaultHttps.getTlsSettings().getProperties());
        assertFalse(defaultHttps.getTlsSettings().getProperties().isEmpty());
        assertEquals(1, defaultHttps.getTlsSettings().getProperties().size());
        assertNotNull(defaultHttps.getTlsSettings().getProperties().get("usesTLS"));
        assertTrue((Boolean) defaultHttps.getTlsSettings().getProperties().get("usesTLS"));
    }

    @Test
    void checkListenPortXmlElements() {
        ListenPortEntityBuilder builder = new ListenPortEntityBuilder(new IdGenerator());

        Service service = new Service();
        service.setId(ID_GENERATOR.generate());
        final Bundle bundle = new Bundle();
        bundle.putAllServices(ImmutableMap.of(SERVICE_REFERENCE, service));
        PrivateKey privateKey = new PrivateKey();
        privateKey.setId(ID_GENERATOR.generate());
        bundle.putAllPrivateKeys(ImmutableMap.of(PRIVATE_KEY, privateKey));

        final ListenPort listenPort = buildPortWithAllOptions();
        final ListenPortTlsSettings tlsSettings = listenPort.getTlsSettings();
        final Entity entity = builder.buildListenPortEntity(bundle, TEST_HTTP_PORT_NAME, listenPort, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());
        assertNotNull(entity.getXml());
        assertNotNull(entity.getId());

        final Element xml = entity.getXml();
        // check if we have the correct amount of elements
        assertNotNull(xml.getAttribute(ATTRIBUTE_ID));
        assertNotNull(getSingleChildElement(xml, NAME));
        assertNotNull(getSingleChildElement(xml, ENABLED));
        assertNotNull(getSingleChildElement(xml, PROTOCOL));
        assertNotNull(getSingleChildElement(xml, PORT));
        assertNotNull(getSingleChildElement(xml, ENABLED_FEATURES));
        assertNotNull(getChildElements(getSingleChildElement(xml, ENABLED_FEATURES), STRING_VALUE));
        assertFalse(getChildElements(getSingleChildElement(xml, ENABLED_FEATURES), STRING_VALUE).isEmpty());
        assertNotNull(getSingleChildElement(xml, TARGET_SERVICE_REFERENCE));
        assertNotNull(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), CLIENT_AUTHENTICATION));
        assertNotNull(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), PRIVATE_KEY_REFERENCE));
        assertNotNull(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_VERSIONS));
        assertFalse(getChildElements(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_VERSIONS), STRING_VALUE).isEmpty());
        assertNotNull(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_CIPHER_SUITES));
        assertFalse(getChildElements(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_CIPHER_SUITES), STRING_VALUE).isEmpty());
        assertNotNull(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), PROPERTIES));
        assertNotNull(getSingleChildElement(xml, PROPERTIES));

        // then check contents
        assertEquals(entity.getId(), xml.getAttribute(ATTRIBUTE_ID));
        assertEquals(TEST_HTTP_PORT_NAME, getSingleChildElementTextContent(xml, NAME));
        assertEquals(TRUE.toString(), getSingleChildElementTextContent(xml, ENABLED));
        assertEquals(listenPort.getProtocol(), getSingleChildElementTextContent(xml, PROTOCOL));
        assertEquals(Integer.toString(listenPort.getPort()), getSingleChildElementTextContent(xml, PORT));
        assertTrue(getChildElementsTextContents(getSingleChildElement(xml, ENABLED_FEATURES), STRING_VALUE).containsAll(listenPort.getEnabledFeatures()));
        assertEquals(service.getId(), getSingleChildElement(xml, TARGET_SERVICE_REFERENCE).getAttribute(ATTRIBUTE_ID));
        assertEquals(tlsSettings.getClientAuthentication().getType(), getSingleChildElementTextContent(getSingleChildElement(xml, TLS_SETTINGS), CLIENT_AUTHENTICATION));
        assertEquals(privateKey.getId(), getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), PRIVATE_KEY_REFERENCE).getAttribute(ATTRIBUTE_ID));
        assertTrue(tlsSettings.getEnabledVersions().containsAll(getChildElementsTextContents(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_VERSIONS), STRING_VALUE)));
        assertTrue(tlsSettings.getEnabledCipherSuites().containsAll(getChildElementsTextContents(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_CIPHER_SUITES), STRING_VALUE)));
        assertPropertiesContent(tlsSettings.getProperties(), mapPropertiesElements(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), PROPERTIES), PROPERTIES));
        assertPropertiesContent(listenPort.getProperties(), mapPropertiesElements(getSingleChildElement(xml, PROPERTIES), PROPERTIES));
    }

    private static void checkExpectedPorts(List<Entity> listenPortEntities, Integer... expectedPorts) {
        List<Integer> expectedList = new ArrayList<>(asList(expectedPorts));
        listenPortEntities.forEach(e -> expectedList.remove((Object) parseInt(getSingleChildElementTextContent(e.getXml(), PORT))));

        assertTrue(expectedList.isEmpty(), () -> "Expected ports were not present in the bundle xml: " + Arrays.toString(expectedList.toArray()));
    }

    private static void addPortToBundle(Bundle bundle, ListenPort port) {
        bundle.putAllListenPorts(new HashMap<String, ListenPort>() {{
            put(TEST_HTTP_PORT_NAME, port);
        }});
    }

    @NotNull
    private static ListenPort buildPort() {
        final ListenPort testPort = new ListenPort();
        testPort.setPort(TEST_HTTP_PORT);
        testPort.setProtocol("HTTP");
        testPort.setEnabledFeatures(new HashSet<>(asList(Feature.MESSAGE_INPUT.getDescription(),
                Feature.POLICYDISCO.getDescription(),
                Feature.PING.getDescription(),
                Feature.STS.getDescription(),
                Feature.WSDLPROXY.getDescription(),
                Feature.SNMPQUERY.getDescription())));
        return testPort;
    }

    @NotNull
    private static ListenPort buildPortWithServiceRef() {
        final ListenPort testPort = buildPort();
        testPort.setTargetServiceReference(SERVICE_REFERENCE);
        return testPort;
    }

    @NotNull
    private static ListenPort buildPortWithAllOptions() {
        final ListenPort testPort = buildPort();
        testPort.setTargetServiceReference(SERVICE_REFERENCE);
        testPort.setEnabledFeatures(Stream.of(Feature.values()).map(Feature::getDescription).collect(toSet()));
        testPort.setTlsSettings(new ListenPortTlsSettings());
        testPort.getTlsSettings().setClientAuthentication(ClientAuthentication.OPTIONAL);
        testPort.getTlsSettings().setPrivateKey(PRIVATE_KEY);
        testPort.getTlsSettings().setEnabledVersions(new HashSet<>(ListenPort.TLS_VERSIONS));
        testPort.getTlsSettings().setEnabledCipherSuites(new HashSet<>(ListenPort.DEFAULT_RECOMMENDED_CIPHERS));
        testPort.getTlsSettings().setProperties(new HashMap<>());
        testPort.getTlsSettings().getProperties().put("usesTLS", TRUE);
        testPort.setProperties(new HashMap<>());
        testPort.getProperties().put("threadPoolSize", "20");
        return testPort;
    }
}
