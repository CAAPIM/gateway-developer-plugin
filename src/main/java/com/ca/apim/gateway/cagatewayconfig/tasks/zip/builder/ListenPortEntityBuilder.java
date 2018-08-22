package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.ListenPortTlsSettings;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Service;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools.createElementWithTextContent;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

/**
 * Builder for Listen ports
 */
public class ListenPortEntityBuilder implements EntityBuilder {

    private final Document document;
    private final IdGenerator idGenerator;

    public ListenPortEntityBuilder(Document document, IdGenerator idGenerator) {
        this.document = document;
        this.idGenerator = idGenerator;
    }

    @Override
    public List<Entity> build(Bundle bundle) {
        return bundle.getListenPorts().entrySet().stream().map(listenPortEntry ->
                buildListenPortEntity(bundle, listenPortEntry.getKey(), listenPortEntry.getValue())
        ).collect(toList());
    }

    private Entity buildListenPortEntity(Bundle bundle, String name, ListenPort listenPort) {
        Element listenPortElement = document.createElement(LISTEN_PORT);

        String id = idGenerator.generate();
        listenPortElement.setAttribute("id", id);
        listenPortElement.appendChild(createElementWithTextContent(document, NAME, name));
        listenPortElement.appendChild(createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString())); // people should not bootstrap a disabled listen port.
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
}
