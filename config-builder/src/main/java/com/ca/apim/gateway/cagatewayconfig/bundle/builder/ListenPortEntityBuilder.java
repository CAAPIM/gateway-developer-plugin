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
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.ca.apim.gateway.cagatewayconfig.environment.EnvironmentBundleUtils.getDeploymentBundle;
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
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Builder for Listen ports
 */
@Singleton
public class ListenPortEntityBuilder implements EntityBuilder {

    private static final Map<String, ListenPort> DEFAULT_PORTS = unmodifiableMap(createDefaultListenPorts());
    private static final String USES_TLS = "usesTLS";
    private static final Integer ORDER = 1200;

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
            listenPortElement.appendChild(createServiceElement(bundle, name, listenPort, document));
        }

        if (listenPort.getTlsSettings() != null) {
            listenPortElement.appendChild(createTlsSettings(bundle, name, listenPort, document));
        }

        buildAndAppendPropertiesElement(listenPort.getProperties(), document, listenPortElement);

        return EntityBuilderHelper.getEntityWithNameMapping(LISTEN_PORT_TYPE, name, id, listenPortElement);
    }

    private Element createServiceElement(Bundle bundle, String name, ListenPort listenPort, Document document) {
        String targetServiceReference = listenPort.getTargetServiceReference();
        Service service = bundle.getServices().get(targetServiceReference);

        if (service == null) {
            service = getDeploymentBundle().getServices().get(targetServiceReference.substring(targetServiceReference.lastIndexOf('/') + 1));
        }

        if (service == null) {
            throw new EntityBuilderException("Could not find service binded to listen port " + name + ". Service Path: " + targetServiceReference);
        }

        return createElementWithAttribute(document, TARGET_SERVICE_REFERENCE, ATTRIBUTE_ID, service.getId());
    }

    private Element createTlsSettings(Bundle bundle, String name, ListenPort listenPort, Document document) {
        ListenPortTlsSettings tlsSettings = listenPort.getTlsSettings();

        Element tlsSettingsElement = document.createElement(TLS_SETTINGS);
        tlsSettingsElement.appendChild(createElementWithTextContent(document, CLIENT_AUTHENTICATION, tlsSettings.getClientAuthentication().getType()));

        if (isNotEmpty(tlsSettings.getPrivateKey())) {
            final PrivateKey privateKey = bundle.getPrivateKeys().get(tlsSettings.getPrivateKey());
            if (privateKey == null) {
                throw new EntityBuilderException("Could not find Private Key " + tlsSettings.getPrivateKey() + " associated to Listen Port " + name);
            }
            tlsSettingsElement.appendChild(createElementWithAttribute(document, PRIVATE_KEY_REFERENCE, ATTRIBUTE_ID, privateKey.getId()));
        }

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

        return tlsSettingsElement;
    }

    // visibility for unit testing
    static Map<String, ListenPort> createDefaultListenPorts() {
        Map<String, ListenPort> defaultPorts = new HashMap<>();

        defaultPorts.put(ListenPort.DEFAULT_HTTP_8080, createDefaultHttp());
        defaultPorts.put(ListenPort.DEFAULT_HTTPS_8443, createDefaultHttps());

        return defaultPorts;
    }

    @NotNull
    static ListenPort createDefaultHttps() {
        ListenPort defaultHttps = new ListenPort();
        defaultHttps.setPort(ListenPort.HTTPS_DEFAULT_PORT);
        defaultHttps.setProtocol(ListenPort.PROTOCOL_HTTPS);
        defaultHttps.setEnabledFeatures(new HashSet<>(asList(Feature.MESSAGE_INPUT.getDescription(),
                Feature.ADMIN_REMOTE.getDescription(),
                Feature.ADMIN_APPLET.getDescription(),
                Feature.OTHER_SERVLETS.getDescription())));
        defaultHttps.setTlsSettings(new ListenPortTlsSettings());
        defaultHttps.getTlsSettings().setClientAuthentication(ClientAuthentication.OPTIONAL);
        defaultHttps.getTlsSettings().setEnabledVersions(new HashSet<>(ListenPort.TLS_VERSIONS));
        defaultHttps.getTlsSettings().setEnabledCipherSuites(new HashSet<>(ListenPort.DEFAULT_RECOMMENDED_CIPHERS));
        defaultHttps.getTlsSettings().setProperties(new HashMap<>());
        defaultHttps.getTlsSettings().getProperties().put(USES_TLS, TRUE);
        return defaultHttps;
    }

    @NotNull
    static ListenPort createDefaultHttp() {
        ListenPort defaultHttp = new ListenPort();
        defaultHttp.setPort(ListenPort.HTTP_DEFAULT_PORT);
        defaultHttp.setProtocol(ListenPort.PROTOCOL_HTTP);
        defaultHttp.setEnabledFeatures(new HashSet<>(asList(Feature.MESSAGE_INPUT.getDescription(),
                Feature.POLICYDISCO.getDescription(),
                Feature.PING.getDescription(),
                Feature.STS.getDescription(),
                Feature.WSDLPROXY.getDescription(),
                Feature.SNMPQUERY.getDescription())));
        return defaultHttp;
    }

    @NotNull
    @Override
    public Integer getOrder() {
        return ORDER;
    }

}
