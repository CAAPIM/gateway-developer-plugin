/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.ca.apim.gateway.cagatewayconfig.beans.GenericEntity.VALUE;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderHelper.getEntityWithNameMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.GENERIC_ENTITY_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static com.fasterxml.jackson.databind.node.JsonNodeType.*;
import static java.util.Collections.emptyList;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * Builder that collect all generic entities loaded in memory and write the bundle with them.
 */
@Singleton
public class GenericEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1600;
    private static final String JAVA = "java";

    private final IdGenerator idGenerator;
    private final JsonTools jsonTools;
    private final FileUtils fileUtils;
    private final DocumentTools documentTools;

    @Inject
    public GenericEntityBuilder(IdGenerator idGenerator, JsonTools jsonTools, FileUtils fileUtils, DocumentTools documentTools) {
        this.idGenerator = idGenerator;
        this.jsonTools = jsonTools;
        this.fileUtils = fileUtils;
        this.documentTools = documentTools;
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        // no generic entity has to be added to environment bundle
        if (bundleType == ENVIRONMENT) {
            return emptyList();
        }

        return bundle.getGenericEntities().entrySet().stream().map(e -> {
            File configFile = bundle.getGenericEntityConfigurations().get(GenericEntity.createKey(e.getValue().getEntityClassName(), e.getKey()));
            if (configFile == null) {
                throw new EntityBuilderException("Configuration file for Generic Entity " + e.getKey() + " is missing.");
            }
            return buildEntity(e.getKey(), e.getValue(), configFile, document);
        }).collect(toList());
    }

    @VisibleForTesting
    Entity buildEntity(String name, GenericEntity genericEntity, File configFile, Document document) {
        String id = idGenerator.generate();
        Element genericEntityElement = createElementWithAttributesAndChildren(
                document,
                GENERIC_ENTITY,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithTextContent(document, ENTITY_CLASS_NAME, genericEntity.getEntityClassName()),
                createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString()),
                createElementWithTextContent(document, VALUE_XML, readConfigAndBuildXml(configFile))
        );

        return getEntityWithNameMapping(GENERIC_ENTITY_TYPE, name, id, genericEntityElement);
    }

    private String readConfigAndBuildXml(File configFile) {
        try {
            ObjectNode rootNode = (ObjectNode) jsonTools.getObjectMapper(getExtension(configFile.getName())).readTree(fileUtils.getInputStream(configFile));
            ObjectNode javaNode = (ObjectNode) rootNode.get(JAVA);
            Document document = documentTools.getDocumentBuilder().newDocument();
            Element java = createXmlElement(JAVA, javaNode, document);

            return documentTools.elementToString(java);
        } catch (IOException e) {
            throw new EntityBuilderException("Could not load Generic Entity configuration file: " + configFile.getName(), e);
        }
    }

    private Element createXmlElement(String nodeName, ObjectNode node, Document document) {
        Element element = createElementWithAttributes(
                document,
                nodeName,
                extractAttributes(node)
        );
        final Map<JsonNodeType, List<Entry<String, JsonNode>>> nodesByType = mapChildNodesByType(node);
        TextNode value = (TextNode) nodesByType.getOrDefault(STRING, emptyList()).stream().filter(e -> e.getKey().equals(VALUE)).map(Entry::getValue).findFirst().orElse(null);
        if (value != null) {
            element.setTextContent(value.asText());
            return element;
        }

        nodesByType.getOrDefault(OBJECT, emptyList()).forEach(e -> element.appendChild(createXmlElement(e.getKey(), (ObjectNode) e.getValue(), document)));
        nodesByType.getOrDefault(ARRAY, emptyList()).forEach(e -> {
            ArrayNode arrayNode = (ArrayNode) e.getValue();
            arrayNode.forEach(arrayElement -> element.appendChild(createXmlElement(e.getKey(), (ObjectNode) arrayElement, document)));
        });
        return element;
    }

    private static Map<String, String> extractAttributes(ObjectNode node) {
        return filterChildNodes(node, TextNode.class).stream().filter(e -> !VALUE.equals(e.getKey())).collect(toMap(Entry::getKey, v -> v.getValue().textValue()));
    }

    private static Map<JsonNodeType, List<Entry<String, JsonNode>>> mapChildNodesByType(ObjectNode node) {
        return stream(spliteratorUnknownSize(node.fields(), ORDERED), false).collect(groupingBy(n -> n.getValue().getNodeType(), mapping(identity(), toList())));
    }

    private static List<Entry<String, JsonNode>> filterChildNodes(ObjectNode node, Class<? extends JsonNode> accepted) {
        return stream(spliteratorUnknownSize(node.fields(), ORDERED), false).filter(e -> accepted.isAssignableFrom(e.getValue().getClass())).collect(toList());
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
