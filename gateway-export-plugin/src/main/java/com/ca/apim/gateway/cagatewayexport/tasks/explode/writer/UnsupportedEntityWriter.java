package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.UnsupportedGatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.file.JsonFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.toInputStream;

@Singleton
public class UnsupportedEntityWriter implements EntityWriter {
    private final DocumentFileUtils documentFileUtils;
    private final JsonFileUtils jsonFileUtils;
    private final DocumentTools documentTools;

    @Inject
    public UnsupportedEntityWriter(DocumentFileUtils documentFileUtils,
                                   JsonFileUtils jsonFileUtils, DocumentTools documentTools) {
        this.documentFileUtils = documentFileUtils;
        this.jsonFileUtils = jsonFileUtils;
        this.documentTools = documentTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder, Bundle rawBundle) {
        File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());

        Map<String, UnsupportedGatewayEntity> unsupportedGatewayEntityMap = bundle.getUnsupportedEntities();
        DocumentBuilder builder = documentTools.getDocumentBuilder();
        Document document = builder.newDocument();
        Element items = document.createElement("l7:Items");
        unsupportedGatewayEntityMap.values().parallelStream().forEach(unsupportedGatewayEntity -> {
            Node item = unsupportedGatewayEntity.getElement().cloneNode(true);
            document.adoptNode(item);
            items.appendChild(item);
        });
        items.setAttribute("xmlns:l7", "http://ns.l7tech.com/2010/04/gateway-management");
        writeElement(configFolder, items);
    }

    private void writeElement(File configFolder, Element item) {
        Path unsupportedEntitiesFilePath = configFolder.toPath().resolve("unsupported-entities.xml");
        try (InputStream itemStream = toInputStream(documentTools.elementToString(item), UTF_8)) {
            FileUtils.copyInputStreamToFile(itemStream, unsupportedEntitiesFilePath.toFile());
        } catch (IOException e) {
            throw new WriteException("Unable to write unsupported entities to xml file", e);
        }
    }
}
