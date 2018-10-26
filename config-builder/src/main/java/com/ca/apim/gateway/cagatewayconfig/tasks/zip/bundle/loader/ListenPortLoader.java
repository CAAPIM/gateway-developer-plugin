/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ListenPortTlsSettings;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ClientAuthentication.fromType;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.lang.Integer.parseInt;

@Singleton
public class ListenPortLoader implements BundleDependencyLoader {

    @Override
    public void load(final Bundle bundle, final Element element) {
        final Element listenPortElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), LISTEN_PORT);

        final String name = getSingleChildElementTextContent(listenPortElement, NAME);
        final String protocol = getSingleChildElementTextContent(listenPortElement, PROTOCOL);
        final Integer port = parseInt(getSingleChildElementTextContent(listenPortElement, PORT));
        final Set<String> enabledFeatures = getChildElementsTextContents(getSingleChildElement(listenPortElement, ENABLED_FEATURES), STRING_VALUE);
        final String targetServiceReference = getSingleChildElementTextContent(listenPortElement, TARGET_SERVICE_REFERENCE);
        final ListenPortTlsSettings tlsSettings = getTlsSettings(listenPortElement);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(listenPortElement, PROPERTIES, true), PROPERTIES);

        ListenPort listenPort = new ListenPort();
        listenPort.setProtocol(protocol);
        listenPort.setPort(port);
        listenPort.setEnabledFeatures(enabledFeatures);
        listenPort.setTargetServiceReference(targetServiceReference);
        listenPort.setTlsSettings(tlsSettings);
        listenPort.setProperties(properties);

        bundle.getListenPorts().put(name, listenPort);
    }

    private ListenPortTlsSettings getTlsSettings(final Element listenPortElement) {
        Element tlsSettingsElement = getSingleChildElement(listenPortElement, TLS_SETTINGS, true);
        if (tlsSettingsElement == null) {
            return null;
        }

        ListenPortTlsSettings tlsSettings = new ListenPortTlsSettings();

        String clientAuthentication = getSingleChildElementTextContent(tlsSettingsElement, CLIENT_AUTHENTICATION);
        tlsSettings.setClientAuthentication(fromType(clientAuthentication));

        Element enabledCipherSuites = getSingleChildElement(tlsSettingsElement, ENABLED_CIPHER_SUITES, true);
        tlsSettings.setEnabledCipherSuites(new HashSet<>(getChildElementsTextContents(enabledCipherSuites, STRING_VALUE)));

        Element enabledVersions = getSingleChildElement(tlsSettingsElement, ENABLED_VERSIONS, true);
        tlsSettings.setEnabledVersions(new HashSet<>(getChildElementsTextContents(enabledVersions, STRING_VALUE)));

        Element properties = getSingleChildElement(tlsSettingsElement, PROPERTIES, true);
        tlsSettings.setProperties(mapPropertiesElements(properties, PROPERTIES));

        return tlsSettings;
    }

    @Override
    public String getEntityType() {
        return EntityTypes.LISTEN_PORT_TYPE;
    }
}
