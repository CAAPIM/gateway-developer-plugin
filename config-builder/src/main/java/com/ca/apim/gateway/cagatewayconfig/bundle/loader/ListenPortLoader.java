/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.ClientAuthentication;
import com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.ListenPortTlsSettings;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.PROTOCOL_FTP;
import static com.ca.apim.gateway.cagatewayconfig.beans.ListenPort.PROTOCOL_FTPS;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.USE_EXTENDED_FTP_COMMAND_SET;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.equalsAny;

@Singleton
public class ListenPortLoader implements BundleEntityLoader {

    @Override
    public void load(final Bundle bundle, final Element element) {
        final Element listenPortElement = getSingleChildElement(getSingleChildElement(element, RESOURCE), LISTEN_PORT);

        final String name = getSingleChildElementTextContent(listenPortElement, NAME);
        final String protocol = getSingleChildElementTextContent(listenPortElement, PROTOCOL);
        final Integer port = parseInt(getSingleChildElementTextContent(listenPortElement, PORT));
        final Set<String> enabledFeatures = getChildElementsTextContents(getSingleChildElement(listenPortElement, ENABLED_FEATURES), STRING_VALUE);
        final String targetServiceReference = getTargetServiceReferenceId(listenPortElement);
        final ListenPortTlsSettings tlsSettings = getTlsSettings(listenPortElement);
        Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(listenPortElement, PROPERTIES, true), PROPERTIES);
        if (!equalsAny(protocol, PROTOCOL_FTP, PROTOCOL_FTPS)) {
            properties.remove(USE_EXTENDED_FTP_COMMAND_SET);
            if (properties.isEmpty()) {
                properties = null;
            }
        }

        ListenPort listenPort = new ListenPort();
        listenPort.setId(listenPortElement.getAttribute(ATTRIBUTE_ID));
        listenPort.setName(name);
        listenPort.setProtocol(protocol);
        listenPort.setPort(port);
        listenPort.setEnabledFeatures(enabledFeatures);
        listenPort.setTargetServiceReference(targetServiceReference);
        listenPort.setTlsSettings(tlsSettings);
        listenPort.setProperties(properties);

        bundle.getListenPorts().put(name, listenPort);
    }

    private String getTargetServiceReferenceId(final Element listenPortElement) {
        final Element targetServiceReference = getSingleChildElement(listenPortElement, TARGET_SERVICE_REFERENCE, true);
        if (targetServiceReference == null) {
            return null;
        }
        return targetServiceReference.getAttribute(ATTRIBUTE_ID);
    }

    private ListenPortTlsSettings getTlsSettings(final Element listenPortElement) {
        Element tlsSettingsElement = getSingleChildElement(listenPortElement, TLS_SETTINGS, true);
        if (tlsSettingsElement == null) {
            return null;
        }

        ListenPortTlsSettings tlsSettings = new ListenPortTlsSettings();

        String clientAuthentication = getSingleChildElementTextContent(tlsSettingsElement, CLIENT_AUTHENTICATION);
        tlsSettings.setClientAuthentication(ClientAuthentication.fromType(clientAuthentication));

        Element enabledCipherSuites = getSingleChildElement(tlsSettingsElement, ENABLED_CIPHER_SUITES, true);
        tlsSettings.setEnabledCipherSuites(new HashSet<>(getChildElementsTextContents(enabledCipherSuites, STRING_VALUE)));

        Element enabledVersions = getSingleChildElement(tlsSettingsElement, ENABLED_VERSIONS, true);
        tlsSettings.setEnabledVersions(new HashSet<>(getChildElementsTextContents(enabledVersions, STRING_VALUE)));

        Element properties = getSingleChildElement(tlsSettingsElement, PROPERTIES, true);
        tlsSettings.setProperties(mapPropertiesElements(properties, PROPERTIES));

        tlsSettings.setPrivateKey(getSingleChildElementAttribute(tlsSettingsElement, PRIVATE_KEY_REFERENCE, ATTRIBUTE_ID));

        return tlsSettings;
    }

    @Override
    public String getEntityType() {
        return EntityTypes.LISTEN_PORT_TYPE;
    }
}
