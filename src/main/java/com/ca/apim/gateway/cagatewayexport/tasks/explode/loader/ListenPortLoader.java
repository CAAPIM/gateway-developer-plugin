/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity.ClientAuthentication;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity.ListenPortEntityTlsSettings;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity.ClientAuthentication.fromType;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper.*;

public class ListenPortLoader implements EntityLoader {

    private static final String ELEMENT_LISTEN_PORT = "l7:ListenPort";
    private static final String ELEMENT_PROTOCOL = "l7:Protocol";
    private static final String ELEMENT_PORT = "l7:Port";
    private static final String ELEMENT_TARGET_SERVICE_REFERENCE = "l7:TargetServiceReference";
    private static final String ELEMENT_ENABLED_FEATURES = "l7:EnabledFeatures";
    private static final String ELEMENT_TLS_SETTINGS = "l7:TlsSettings";
    private static final String ELEMENT_TLS_CLIENT_AUTHENTICATION = "l7:ClientAuthentication";
    private static final String ELEMENT_TLS_ENABLED_VERSIONS = "l7:EnabledVersions";
    private static final String ELEMENT_TLS_ENABLED_CIPHER_SUITES = "l7:EnabledCipherSuites";

    @Override
    public Entity load(Element element) {
        final Element listenPort = getSingleChildElement(getSingleChildElement(element, ELEMENT_RESOURCE), ELEMENT_LISTEN_PORT);

        final String name = getSingleChildElementTextContent(listenPort, ELEMENT_NAME);
        final String protocol = getSingleChildElement(listenPort, ELEMENT_PROTOCOL).getTextContent();
        final int port = Integer.parseInt(getSingleChildElement(listenPort, ELEMENT_PORT).getTextContent());
        final List<String> enabledFeatures = getChildElementsTextContents(getSingleChildElement(listenPort, ELEMENT_ENABLED_FEATURES), ELEMENT_STRING_VALUE);
        final ListenPortEntityTlsSettings tlsSettings = buildTlsSettings(getSingleChildElement(listenPort, ELEMENT_TLS_SETTINGS, true));
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(listenPort, ELEMENT_PROPERTIES, true));
        final String targetServiceReference = getSingleChildElementAttribute(listenPort, ELEMENT_TARGET_SERVICE_REFERENCE, "id");

        return new ListenPortEntity().setId(listenPort.getAttribute("id")).setName(name).setProtocol(protocol).setPort(port).setEnabledFeatures(enabledFeatures).setTlsSettings(tlsSettings).setProperties(properties).setTargetServiceReference(targetServiceReference);
    }

    private ListenPortEntityTlsSettings buildTlsSettings(final Element tlsSettingsElement) {
        if (tlsSettingsElement == null) {
            return null;
        }

        final ClientAuthentication clientAuthentication = fromType(getSingleChildElementTextContent(tlsSettingsElement, ELEMENT_TLS_CLIENT_AUTHENTICATION));
        final List<String> enabledVersions = getChildElementsTextContents(getSingleChildElement(tlsSettingsElement, ELEMENT_TLS_ENABLED_VERSIONS), ELEMENT_STRING_VALUE);
        final List<String> enabledCipherSuites = getChildElementsTextContents(getSingleChildElement(tlsSettingsElement, ELEMENT_TLS_ENABLED_CIPHER_SUITES, true), ELEMENT_STRING_VALUE);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(tlsSettingsElement, ELEMENT_PROPERTIES, true));

        return new ListenPortEntityTlsSettings(clientAuthentication, enabledVersions, enabledCipherSuites, properties);
    }
}
