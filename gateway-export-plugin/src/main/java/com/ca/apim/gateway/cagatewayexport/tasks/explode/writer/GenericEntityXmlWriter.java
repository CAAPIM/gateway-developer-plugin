/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity.VALUE;
import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.JSON;
import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.JSON_EXTENSION;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.namedNodeMap;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.nodeList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.w3c.dom.Node.ELEMENT_NODE;

/**
 * Writes a yaml/json version of the generic entity xml to a specific folder structure config/genericEntities/${className}/${name}.json/yaml.
 */
@Singleton
public class GenericEntityXmlWriter implements EntityWriter {

    private final JsonTools jsonTools;
    private final FileUtils fileUtils;
    private final DocumentTools documentTools;

    @Inject
    public GenericEntityXmlWriter(JsonTools jsonTools, FileUtils fileUtils, DocumentTools documentTools) {
        this.jsonTools = jsonTools;
        this.fileUtils = fileUtils;
        this.documentTools = documentTools;
    }

    @Override
    public void write(Bundle bundle, File rootFolder) {
        File genericEntitiesFolder = new File(new File(rootFolder, "config"), "genericEntities");
        fileUtils.createFolders(genericEntitiesFolder.toPath());

        bundle.getGenericEntities().values().forEach(ge -> {
            File entityFolder = new File(genericEntitiesFolder, ge.getEntityClassName());
            fileUtils.createFolder(entityFolder.toPath());

            JsonNode jsonNode;
            try {
                jsonNode = xmlToJson(ge.getXml());
            } catch (DocumentParseException e) {
                throw new WriteException("Could not read Generic Entity " + ge.getName() + " XML Configuration", e);
            }

            File entityFile = new File(entityFolder, ge.getName() + "." + JSON_EXTENSION);
            try (OutputStream stream = fileUtils.getOutputStream(entityFile)) {
                jsonTools.getObjectWriter(JSON).writeValue(stream, jsonNode);
            } catch (IOException e) {
                throw new WriteException("Could not write Generic Entity " + ge.getName() + " configuration to file: " + entityFile.toString(), e);
            }
        });
    }

    JsonNode xmlToJson(String xml) throws DocumentParseException {
        Element xmlRoot = documentTools.parse(xml).getDocumentElement();
        ObjectMapper mapper = jsonTools.getObjectMapper(JSON);

        TempNode tempNode = toTempNodes(xmlRoot);
        ObjectNode node = toJsonNode(tempNode, mapper);

        return mapper.createObjectNode().set(tempNode.name, node);
    }

    private static TempNode toTempNodes(Node xmlNode) {
        TempNode tempNode = new TempNode();
        tempNode.name = xmlNode.getNodeName();
        namedNodeMap(xmlNode.getAttributes()).forEach(attr -> tempNode.attributes.put(attr.getNodeName(), attr.getNodeValue()));
        List<Node> childNodes = stream(nodeList(xmlNode.getChildNodes()).spliterator(), false)
                .filter(n -> n.getNodeType() == ELEMENT_NODE).collect(toList());
        if (childNodes.isEmpty()) {
            tempNode.value = xmlNode.getTextContent();
        } else {
            childNodes.forEach(c -> tempNode.children.add(toTempNodes(c)));
        }
        return tempNode;
    }

    private static ObjectNode toJsonNode(TempNode tempNode, ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();
        tempNode.attributes.forEach(node::put);
        if (isNotBlank(tempNode.value)) {
            node.put(VALUE, tempNode.value);
        } else if (tempNode.children.size() == 1) {
            TempNode child = tempNode.children.get(0);
            node.set(child.name, toJsonNode(child, mapper));
        } else {
            tempNode.mappedChildren().forEach((key, value) -> {
                ArrayNode arrayNode = mapper.createArrayNode();
                value.forEach(t -> arrayNode.add(toJsonNode(t, mapper)));
                node.set(key, arrayNode);
            });
        }
        return node;
    }

    private static class TempNode {
        private String name;
        private Map<String, String> attributes = new LinkedHashMap<>();
        private String value;
        private List<TempNode> children = new ArrayList<>();

        Map<String, List<TempNode>> mappedChildren() {
            return children.stream().collect(groupingBy(t -> t.name, mapping(identity(), toList())));
        }
    }
}
