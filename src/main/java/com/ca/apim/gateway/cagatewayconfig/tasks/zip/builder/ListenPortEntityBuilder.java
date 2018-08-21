package com.ca.apim.gateway.cagatewayconfig.tasks.zip.builder;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ListenPort.TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static java.util.stream.Collectors.toList;

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
        Element listenPortElement = document.createElement("l7:ListenPort");

        String id = idGenerator.generate();
        listenPortElement.setAttribute("id", id);
        listenPortElement.appendChild(createElementWithTextContent(document,"l7:Name", name));
        listenPortElement.appendChild(createElementWithTextContent(document,"l7:Enabled", Boolean.toString(listenPort.isEnabled())));
        listenPortElement.appendChild(createElementWithTextContent(document,"l7:Protocol", listenPort.getProtocol()));
        listenPortElement.appendChild(createElementWithTextContent(document,"l7:Port", Integer.toString(listenPort.getPort())));
        listenPortElement.appendChild(createElementWithTextContent(document,"l7:Interface", listenPort.getTargetServiceId()));
        listenPortElement.appendChild(createElementWithTextContent(document,"l7:TargetServiceReference", name));


        return new Entity(TYPE, name, id, listenPortElement);
    }
}
