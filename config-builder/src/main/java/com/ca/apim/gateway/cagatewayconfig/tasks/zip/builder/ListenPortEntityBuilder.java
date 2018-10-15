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
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ClientAuthentication.OPTIONAL;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.Feature.*;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.*;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ListenPortTlsSettings.*;
import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder.EntityBuilderHelper.getEntityWithNameMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.LISTEN_PORT_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.MappingActions.NEW_OR_EXISTING;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.unmodifiableMap;

/**
 * Builder for Listen ports
 */
@Singleton
public class ListenPortEntityBuilder implements EntityBuilder {

    public static final String DEFAULT_HTTP_8080 = "Default HTTP (8080)";
    public static final String DEFAULT_HTTPS_8443 = "Default HTTPS (8443)";
    static final List<String> DEFAULT_RECOMMENDED_CIPHERS = Collections.unmodifiableList(asList(
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
    static final List<String> TLS_VERSIONS = asList(TLSV1, TLSV11, TLSV12);
    private static final Map<String, ListenPort> DEFAULT_PORTS = unmodifiableMap(createDefaultListenPorts());
    private static final String USES_TLS = "usesTLS";
    private static final Integer ORDER = 800;

    private final IdGenerator idGenerator;

    @Inject
    ListenPortEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        final Stream<Entity> userPorts = bundle.getListenPorts().entrySet().stream().map(listenPortEntry ->
                buildListenPortEntity(bundle, listenPortEntry.getKey(), listenPortEntry.getValue(), document));

        switch (bundleType) {
            case DEPLOYMENT:
                return userPorts.collect(toList());
            case ENVIRONMENT:
                final Stream<Entity> defaultPorts = DEFAULT_PORTS.entrySet().stream().filter(p -> bundle.getListenPorts().values().stream().noneMatch(up -> up.getPort() == p.getValue().getPort()))
                        .map(listenPortEntry -> {
                            Entity entity = buildListenPortEntity(bundle, listenPortEntry.getKey(), listenPortEntry.getValue(), document);
                            entity.setMappingAction(NEW_OR_EXISTING);
                            return entity;
                        });
                return concat(userPorts, defaultPorts).collect(toList());
            default:
                throw new EntityBuilderException("Unknown bundle type: " + bundleType);
        }
    }

    // also visible for testing
    Entity buildListenPortEntity(Bundle bundle, String name, ListenPort listenPort, Document document) {
        Element listenPortElement = document.createElement(LISTEN_PORT);

        String id = idGenerator.generate();
        listenPortElement.setAttribute(ATTRIBUTE_ID, id);
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
            listenPortElement.appendChild(createElementWithAttribute(document, TARGET_SERVICE_REFERENCE, ATTRIBUTE_ID, service.getId()));
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

            buildAndAppendPropertiesElement(tlsSettings.getProperties(), document, tlsSettingsElement);

            listenPortElement.appendChild(tlsSettingsElement);
        }

        buildAndAppendPropertiesElement(listenPort.getProperties(), document, listenPortElement);

        return getEntityWithNameMapping(LISTEN_PORT_TYPE, name, id, listenPortElement);
    }

    // visibility for unit testing
    static Map<String, ListenPort> createDefaultListenPorts() {
        Map<String, ListenPort> defaultPorts = new HashMap<>();

        defaultPorts.put(DEFAULT_HTTP_8080, createDefaultHttp());
        defaultPorts.put(DEFAULT_HTTPS_8443, createDefaultHttps());

        return defaultPorts;
    }

    @NotNull
    static ListenPort createDefaultHttps() {
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
        defaultHttps.getTlsSettings().setProperties(new HashMap<>());
        defaultHttps.getTlsSettings().getProperties().put(USES_TLS, TRUE);
        return defaultHttps;
    }

    @NotNull
    static ListenPort createDefaultHttp() {
        ListenPort defaultHttp = new ListenPort();
        defaultHttp.setPort(HTTP_DEFAULT_PORT);
        defaultHttp.setProtocol(PROTOCOL_HTTP);
        defaultHttp.setEnabledFeatures(asList(MESSAGE_INPUT.getDescription(),
                POLICYDISCO.getDescription(),
                PING.getDescription(),
                STS.getDescription(),
                WSDLPROXY.getDescription(),
                SNMPQUERY.getDescription()));
        return defaultHttp;
    }

    @Override
    public Integer getOrder() {
        return ORDER;
    }

}
