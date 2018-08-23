/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.*;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ClientAuthentication.OPTIONAL;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.Feature.*;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools.createElementWithTextContent;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.unmodifiableMap;

/**
 * Builder for Listen ports
 */
public class ListenPortEntityBuilder implements EntityBuilder {

    private static final List<String> DEFAULT_RECOMMENDED_CIPHERS = Collections.unmodifiableList(asList(
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
            "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA",
            "TLS_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_RSA_WITH_AES_256_CBC_SHA256",
            "TLS_RSA_WITH_AES_256_CBC_SHA",
            "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256",
            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA",
            "TLS_RSA_WITH_AES_128_GCM_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA"));
    private static final List<String> TLS_VERSIONS = asList("TLSv1", "TLSv1.1", "TLSv1.2");
    private static final Map<String, ListenPort> DEFAULT_PORTS = unmodifiableMap(createDefaultListenPorts());

    private final Document document;
    private final IdGenerator idGenerator;

    public ListenPortEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    @Override
    public List<Entity> build(Bundle bundle) {
        final Stream<Entry<String, ListenPort>> userPorts = bundle.getListenPorts().entrySet().stream();
        final Stream<Entry<String, ListenPort>> defaultPorts = DEFAULT_PORTS.entrySet().stream();

        return concat(userPorts, defaultPorts).map(listenPortEntry ->
                buildListenPortEntity(bundle, listenPortEntry.getKey(), listenPortEntry.getValue())
        ).collect(toList());
    }

    private Entity buildListenPortEntity(Bundle bundle, String name, ListenPort listenPort) {
        Element listenPortElement = document.createElement(LISTEN_PORT);

        String id = idGenerator.generate();
        listenPortElement.setAttribute("id", id);
        listenPortElement.appendChild(createElementWithTextContent(document, NAME, name));
        listenPortElement.appendChild(createElementWithTextContent(document, ENABLED, TRUE.toString())); // people should not bootstrap a disabled listen port.
        listenPortElement.appendChild(createElementWithTextContent(document, PROTOCOL, listenPort.getProtocol()));
        listenPortElement.appendChild(createElementWithTextContent(document, PORT, Integer.toString(listenPort.getPort())));

        Element enabledFeatures = document.createElement(ENABLED_FEATURES);
        listenPort.getEnabledFeatures().forEach(s -> enabledFeatures.appendChild(createElementWithTextContent(document, STRING_VALUE, s)));
        listenPortElement.appendChild(enabledFeatures);

        if (listenPort.getTargetServiceReference() != null) {
            final Service service = bundle.getServices().get(listenPort.getTargetServiceReference());
            if (service == null) {
                throw new EntityBuilderException("Could not find service binded to listen port " + name + ". Service Path: " + listenPort.getTargetServiceReference());
            }
            listenPortElement.appendChild(createElementWithAttribute(document, TARGET_SERVICE_REFERENCE, "id", service.getId()));
        }

        if (listenPort.getTlsSettings() != null) {
            ListenPortTlsSettings tlsSettings = listenPort.getTlsSettings();

            Element tlsSettingsElement = document.createElement(TLS_SETTINGS);
            tlsSettingsElement.appendChild(createElementWithTextContent(document, CLIENT_AUTHENTICATION, tlsSettings.getClientAuthentication().getType()));

            if (isNotEmpty(tlsSettings.getEnabledVersions())) {
                Element enabledVersions = document.createElement(ENABLED_VERSIONS);
                tlsSettings.getEnabledVersions().forEach(s -> enabledVersions.appendChild(createElementWithTextContent(document, STRING_VALUE, s)));
                tlsSettingsElement.appendChild(enabledVersions);
            }

            if (isNotEmpty(tlsSettings.getEnabledCipherSuites())) {
                Element enabledCipherSuites = document.createElement(ENABLED_CIPHER_SUITES);
                tlsSettings.getEnabledCipherSuites().forEach(s -> enabledCipherSuites.appendChild(createElementWithTextContent(document, STRING_VALUE, s)));
                tlsSettingsElement.appendChild(enabledCipherSuites);
            }

            if (isNotEmpty(tlsSettings.getProperties())) {
                tlsSettingsElement.appendChild(buildPropertiesElement(tlsSettings.getProperties(), document));
            }

            listenPortElement.appendChild(tlsSettingsElement);
        }

        if (isNotEmpty(listenPort.getProperties())) {
            listenPortElement.appendChild(buildPropertiesElement(listenPort.getProperties(), document));
        }

        return new Entity(TYPE, name, id, listenPortElement);
    }

    private static Map<String, ListenPort> createDefaultListenPorts() {
        Map<String, ListenPort> defaultPorts = new HashMap<>();

        ListenPort defaultHttp = new ListenPort();
        defaultHttp.setPort(HTTP_DEFAULT_PORT);
        defaultHttp.setProtocol(PROTOCOL_HTTP);
        defaultHttp.setEnabledFeatures(asList(MESSAGE_INPUT.getDescription(),
                POLICYDISCO.getDescription(),
                PING.getDescription(),
                STS.getDescription(),
                WSDLPROXY.getDescription(),
                SNMPQUERY.getDescription()));

        ListenPort defaultHttps = new ListenPort();
        defaultHttps.setPort(HTTPS_DEFAULT_PORT);
        defaultHttps.setProtocol(PROTOCOL_HTTPS);
        defaultHttps.setEnabledFeatures(asList(MESSAGE_INPUT.getDescription(),
                ADMIN_REMOTE.getDescription(),
                ADMIN_APPLET.getDescription(),
                OTHER_SERVLETS.getDescription()));
        defaultHttps.setTlsSettings(new ListenPortTlsSettings());
        defaultHttps.getTlsSettings().setClientAuthentication(OPTIONAL);
        defaultHttps.getTlsSettings().setEnabledVersions(TLS_VERSIONS);
        defaultHttps.getTlsSettings().setEnabledCipherSuites(DEFAULT_RECOMMENDED_CIPHERS);
        defaultHttps.setProperties(new HashMap<>());
        defaultHttps.getProperties().put("usesTLS", TRUE);

        defaultPorts.put("Default HTTP (8080)", defaultHttp);
        defaultPorts.put("Default HTTPS (8443)", defaultHttps);

        return defaultPorts;
    }


}
