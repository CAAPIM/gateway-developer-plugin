/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.*;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ClientAuthentication.OPTIONAL;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.Feature.*;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.ListenPortEntityBuilder.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools.*;
import static java.lang.Boolean.TRUE;
import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

/**
 *
 */
public class ListenPortEntityBuilderTest {

    private static final int TEST_HTTP_PORT = 12345;
    private static final String TEST_HTTP_PORT_NAME = "Test HTTP Port 12345";
    private static final String SERVICE_REFERENCE = "service-reference";
    private static final IdGenerator ID_GENERATOR = new IdGenerator();

    @Test
    public void buildFromEmptyBundle_expectOnlyDefaultPorts() {
        ListenPortEntityBuilder builder = new ListenPortEntityBuilder(INSTANCE.getDocumentBuilder().newDocument(), ID_GENERATOR);
        final Bundle bundle = new Bundle();
        final List<Entity> listenPortEntities = builder.build(bundle);

        // only two defaults
        assertEquals("No ports specified but the bundle contains more than the default 2", 2, listenPortEntities.size());

        // check 8080 and 8443
        checkExpectedPorts(listenPortEntities, HTTP_DEFAULT_PORT, HTTPS_DEFAULT_PORT);
    }

    @Test
    public void buildWithCustomPort_expectCustomAndDefaults() {
        ListenPortEntityBuilder builder = new ListenPortEntityBuilder(INSTANCE.getDocumentBuilder().newDocument(), ID_GENERATOR);
        final Bundle bundle = new Bundle();
        addPortToBundle(bundle, buildPort());

        final List<Entity> listenPortEntities = builder.build(bundle);

        // two defaults and custom one
        assertEquals("More than 3 expected ports", 3, listenPortEntities.size());

        // check
        checkExpectedPorts(listenPortEntities, HTTP_DEFAULT_PORT, HTTPS_DEFAULT_PORT, TEST_HTTP_PORT);
    }

    @Test(expected = EntityBuilderException.class)
    public void buildWithCustomPortReferencingService_expectedException() {
        ListenPortEntityBuilder builder = new ListenPortEntityBuilder(INSTANCE.getDocumentBuilder().newDocument(), ID_GENERATOR);
        final Bundle bundle = new Bundle();
        addPortToBundle(bundle, buildPortWithServiceRef());

        builder.build(bundle);
    }

    @Test
    public void checkDefaultPortsAreCreated() {
        final Map<String, ListenPort> defaultListenPorts = createDefaultListenPorts();

        assertNotNull("Missing default HTTP", defaultListenPorts.get(DEFAULT_HTTP_8080));
        assertNotNull("Missing default HTTPS", defaultListenPorts.get(DEFAULT_HTTPS_8443));
    }

    @Test
    public void checkDefaultHttpPortConfiguration() {
        ListenPort defaultHttp = createDefaultHttp();
        assertEquals(HTTP_DEFAULT_PORT.intValue(), defaultHttp.getPort());
        assertEquals(PROTOCOL_HTTP, defaultHttp.getProtocol());
        assertTrue(defaultHttp.getEnabledFeatures().containsAll(asList(MESSAGE_INPUT.getDescription(),
                POLICYDISCO.getDescription(),
                PING.getDescription(),
                STS.getDescription(),
                WSDLPROXY.getDescription(),
                SNMPQUERY.getDescription())));
    }

    @Test
    public void checkDefaultHttpsPortConfiguration() {
        ListenPort defaultHttps = createDefaultHttps();
        assertEquals(HTTPS_DEFAULT_PORT.intValue(), defaultHttps.getPort());
        assertEquals(PROTOCOL_HTTPS, defaultHttps.getProtocol());
        assertTrue(defaultHttps.getEnabledFeatures().containsAll(asList(MESSAGE_INPUT.getDescription(),
                ADMIN_REMOTE.getDescription(),
                ADMIN_APPLET.getDescription(),
                OTHER_SERVLETS.getDescription())));
        assertNotNull(defaultHttps.getTlsSettings());
        assertEquals(ClientAuthentication.OPTIONAL, defaultHttps.getTlsSettings().getClientAuthentication());
        assertTrue(defaultHttps.getTlsSettings().getEnabledVersions().containsAll(TLS_VERSIONS));
        assertTrue(defaultHttps.getTlsSettings().getEnabledCipherSuites().containsAll(DEFAULT_RECOMMENDED_CIPHERS));
        assertNotNull(defaultHttps.getTlsSettings().getProperties());
        assertFalse(defaultHttps.getTlsSettings().getProperties().isEmpty());
        assertEquals(1, defaultHttps.getTlsSettings().getProperties().size());
        assertNotNull(defaultHttps.getTlsSettings().getProperties().get("usesTLS"));
        assertTrue((Boolean) defaultHttps.getTlsSettings().getProperties().get("usesTLS"));
    }

    @Test
    public void checkListenPortXmlElements() {
        ListenPortEntityBuilder builder = new ListenPortEntityBuilder(INSTANCE.getDocumentBuilder().newDocument(), new IdGenerator());

        Service service = new Service();
        service.setId(ID_GENERATOR.generate());
        final Bundle bundle = new Bundle();
        bundle.putAllServices(new HashMap<String, Service>() {{
            put(SERVICE_REFERENCE, service);
        }});

        final ListenPort listenPort = buildPortWithAllOptions();
        final ListenPortTlsSettings tlsSettings = listenPort.getTlsSettings();
        final Entity entity = builder.buildListenPortEntity(bundle, TEST_HTTP_PORT_NAME, listenPort);
        assertNotNull(entity.getXml());
        assertNotNull(entity.getId());

        final Element xml = entity.getXml();
        // check if we have the correct amount of elements
        assertNotNull(xml.getAttribute(ID));
        assertNotNull(getSingleChildElement(xml, NAME));
        assertNotNull(getSingleChildElement(xml, ENABLED));
        assertNotNull(getSingleChildElement(xml, PROTOCOL));
        assertNotNull(getSingleChildElement(xml, PORT));
        assertNotNull(getSingleChildElement(xml, ENABLED_FEATURES));
        assertNotNull(getChildElements(getSingleChildElement(xml, ENABLED_FEATURES), STRING_VALUE));
        assertFalse(getChildElements(getSingleChildElement(xml, ENABLED_FEATURES), STRING_VALUE).isEmpty());
        assertNotNull(getSingleChildElement(xml, TARGET_SERVICE_REFERENCE));
        assertNotNull(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), CLIENT_AUTHENTICATION));
        assertNotNull(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_VERSIONS));
        assertFalse(getChildElements(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_VERSIONS), STRING_VALUE).isEmpty());
        assertNotNull(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_CIPHER_SUITES));
        assertFalse(getChildElements(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_CIPHER_SUITES), STRING_VALUE).isEmpty());
        assertNotNull(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), PROPERTIES));
        assertNotNull(getSingleChildElement(xml, PROPERTIES));

        // then check contents
        assertEquals(entity.getId(), xml.getAttribute(ID));
        assertEquals(TEST_HTTP_PORT_NAME, getSingleChildElementTextContent(xml, NAME));
        assertEquals(TRUE.toString(), getSingleChildElementTextContent(xml, ENABLED));
        assertEquals(listenPort.getProtocol(), getSingleChildElementTextContent(xml, PROTOCOL));
        assertEquals(Integer.toString(listenPort.getPort()), getSingleChildElementTextContent(xml, PORT));
        assertTrue(getChildElementsTextContents(getSingleChildElement(xml, ENABLED_FEATURES), STRING_VALUE).containsAll(listenPort.getEnabledFeatures()));
        assertEquals(service.getId(), getSingleChildElement(xml, TARGET_SERVICE_REFERENCE).getAttribute(ID));
        assertEquals(tlsSettings.getClientAuthentication().getType(), getSingleChildElementTextContent(getSingleChildElement(xml, TLS_SETTINGS), CLIENT_AUTHENTICATION));
        assertTrue(tlsSettings.getEnabledVersions().containsAll(getChildElementsTextContents(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_VERSIONS), STRING_VALUE)));
        assertTrue(tlsSettings.getEnabledCipherSuites().containsAll(getChildElementsTextContents(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), ENABLED_CIPHER_SUITES), STRING_VALUE)));
        assertPropertiesContent(tlsSettings.getProperties(), mapPropertiesElements(getSingleChildElement(getSingleChildElement(xml, TLS_SETTINGS), PROPERTIES)));
        assertPropertiesContent(listenPort.getProperties(), mapPropertiesElements(getSingleChildElement(xml, PROPERTIES)));
    }

    private static void assertPropertiesContent(Map<String, Object> expected, Map<String, Object> actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.size(), actual.size());

        expected.forEach((key, value) -> {
            assertNotNull(actual.get(key));
            assertEquals(value, actual.get(key));
            actual.remove(key);
        });

        assertTrue(actual.isEmpty());
    }

    private static void checkExpectedPorts(List<Entity> listenPortEntities, Integer... expectedPorts) {
        List<Integer> expectedList = new ArrayList<>(asList(expectedPorts));
        listenPortEntities.forEach(e -> expectedList.remove((Object) parseInt(getSingleChildElementTextContent(e.getXml(), PORT))));

        assertTrue("Expected ports were not present in the bundle xml: " + Arrays.toString(expectedList.toArray()), expectedList.isEmpty());
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
        testPort.setEnabledFeatures(asList(MESSAGE_INPUT.getDescription(),
                POLICYDISCO.getDescription(),
                PING.getDescription(),
                STS.getDescription(),
                WSDLPROXY.getDescription(),
                SNMPQUERY.getDescription()));
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
        testPort.setEnabledFeatures(Stream.of(Feature.values()).map(Feature::getDescription).collect(toList()));
        testPort.setTlsSettings(new ListenPortTlsSettings());
        testPort.getTlsSettings().setClientAuthentication(OPTIONAL);
        testPort.getTlsSettings().setEnabledVersions(TLS_VERSIONS);
        testPort.getTlsSettings().setEnabledCipherSuites(DEFAULT_RECOMMENDED_CIPHERS);
        testPort.getTlsSettings().setProperties(new HashMap<>());
        testPort.getTlsSettings().getProperties().put("usesTLS", TRUE);
        testPort.setProperties(new HashMap<>());
        testPort.getProperties().put("threadPoolSize", "20");
        return testPort;
    }
}
