/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity.ClientAuthentication;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity.ListenPortEntityTlsSettings;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ListenPortEntity.ClientAuthentication.fromType;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayexport.util.xml.DocumentUtils.*;

@Singleton
public class ListenPortLoader implements EntityLoader<ListenPortEntity> {

    @Override
    public ListenPortEntity load(Element element) {
        final Element listenPort = getSingleChildElement(getSingleChildElement(element, RESOURCE), LISTEN_PORT);

        final String name = getSingleChildElementTextContent(listenPort, NAME);
        final String protocol = getSingleChildElement(listenPort, PROTOCOL).getTextContent();
        final int port = Integer.parseInt(getSingleChildElement(listenPort, PORT).getTextContent());
        final List<String> enabledFeatures = getChildElementsTextContents(getSingleChildElement(listenPort, ENABLED_FEATURES), STRING_VALUE);
        final ListenPortEntityTlsSettings tlsSettings = buildTlsSettings(getSingleChildElement(listenPort, TLS_SETTINGS, true));
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(listenPort, PROPERTIES, true));
        final String targetServiceReference = getSingleChildElementAttribute(listenPort, TARGET_SERVICE_REFERENCE, ATTRIBUTE_ID);

        return new ListenPortEntity.Builder()
                .id(listenPort.getAttribute(ATTRIBUTE_ID))
                .name(name)
                .protocol(protocol)
                .port(port)
                .enabledFeatures(enabledFeatures)
                .tlsSettings(tlsSettings)
                .properties(properties)
                .targetServiceReference(targetServiceReference)
                .build();
    }

    private ListenPortEntityTlsSettings buildTlsSettings(final Element tlsSettingsElement) {
        if (tlsSettingsElement == null) {
            return null;
        }

        final ClientAuthentication clientAuthentication = fromType(getSingleChildElementTextContent(tlsSettingsElement, CLIENT_AUTHENTICATION));
        final List<String> enabledVersions = getChildElementsTextContents(getSingleChildElement(tlsSettingsElement, ENABLED_VERSIONS), STRING_VALUE);
        final List<String> enabledCipherSuites = getChildElementsTextContents(getSingleChildElement(tlsSettingsElement, ENABLED_CIPHER_SUITES, true), STRING_VALUE);
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(tlsSettingsElement, PROPERTIES, true));

        return new ListenPortEntityTlsSettings(clientAuthentication, enabledVersions, enabledCipherSuites, properties);
    }

    @Override
    public Class<ListenPortEntity> entityClass() {
        return ListenPortEntity.class;
    }
}
