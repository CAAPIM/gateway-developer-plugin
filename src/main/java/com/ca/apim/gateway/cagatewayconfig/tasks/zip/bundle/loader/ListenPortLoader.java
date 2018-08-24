/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayconfig.tasks.zip.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ListenPortTlsSettings;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ClientAuthentication.fromType;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools.*;
import static java.lang.Integer.parseInt;

public class ListenPortLoader implements BundleEntityLoader {

    ListenPortLoader() {
    }

    @Override
    public void load(final Bundle bundle, final Element element) {
        final Element listenPortElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), LISTEN_PORT);

        final String name = getSingleChildElement(listenPortElement, NAME).getTextContent();
        final String protocol = getSingleChildElement(listenPortElement, PROTOCOL).getTextContent();
        final Integer port = parseInt(getSingleChildElement(listenPortElement, PORT).getTextContent());
        final List<String> enabledFeatures = getChildElementsTextContents(getSingleChildElement(listenPortElement, ENABLED_FEATURES), STRING_VALUE);
        final String targetServiceReference = getTargetServiceReference(listenPortElement);
        final ListenPortTlsSettings tlsSettings = getTlsSettings(listenPortElement);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(listenPortElement, PROPERTIES, true));

        ListenPort listenPort = new ListenPort();
        listenPort.setProtocol(protocol);
        listenPort.setPort(port);
        listenPort.setEnabledFeatures(enabledFeatures);
        listenPort.setTargetServiceReference(targetServiceReference);
        listenPort.setTlsSettings(tlsSettings);
        listenPort.setProperties(properties);

        bundle.getListenPorts().put(name, listenPort);
    }

    private String getTargetServiceReference(final Element listenPortElement) {
        Element targetServiceReference = getSingleChildElement(listenPortElement, TARGET_SERVICE_REFERENCE);
        if (targetServiceReference == null) {
            return null;
        }
        return targetServiceReference.getTextContent();
    }

    private ListenPortTlsSettings getTlsSettings(final Element listenPortElement) {
        Element tlsSettingsElement = getSingleChildElement(listenPortElement, TLS_SETTINGS);
        if (tlsSettingsElement == null) {
            return null;
        }

        ListenPortTlsSettings tlsSettings = new ListenPortTlsSettings();
        tlsSettings.setClientAuthentication(fromType(getSingleChildElementTextContent(tlsSettingsElement, CLIENT_AUTHENTICATION)));
        tlsSettings.setEnabledCipherSuites(getChildElementsTextContents(getSingleChildElement(tlsSettingsElement, ENABLED_VERSIONS, true), STRING_VALUE));
        tlsSettings.setEnabledVersions(getChildElementsTextContents(getSingleChildElement(tlsSettingsElement, ENABLED_CIPHER_SUITES, true), STRING_VALUE));
        tlsSettings.setProperties(mapPropertiesElements(getSingleChildElement(tlsSettingsElement, PROPERTIES, true)));

        return tlsSettings;
    }
}
