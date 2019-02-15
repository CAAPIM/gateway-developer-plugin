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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilderHelper.getEntityWithNameMapping;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.GENERIC_ENTITY_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FilenameUtils.getExtension;

/**
 * Builder that collect all generic entities loaded in memory and write the bundle with them.
 */
@Singleton
public class GenericEntityBuilder implements EntityBuilder {

    private static final Integer ORDER = 1600;

    private final IdGenerator idGenerator;
    private final JsonTools jsonTools;
    private final XmlMapper xmlMapper;
    private final FileUtils fileUtils;

    @Inject
    public GenericEntityBuilder(IdGenerator idGenerator, JsonTools jsonTools, FileUtils fileUtils) {
        this.idGenerator = idGenerator;
        this.jsonTools = jsonTools;
        this.fileUtils = fileUtils;
        this.xmlMapper = new XmlMapper();
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        // no generic entity has to be added to environment bundle
        if (bundleType == ENVIRONMENT) {
            return emptyList();
        }

        return bundle.getGenericEntities().entrySet().stream().map(e -> {
            File configFile = bundle.getGenericEntityConfigurations().get(e.getKey());
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
                createElementWithTextContent(document, ENABLED, Boolean.TRUE.toString())
        );
        genericEntityElement.appendChild(createElementWithTextContent(document, ENTITY_CLASS_NAME, genericEntity.getEntityClassName()));
        genericEntityElement.appendChild(createElementWithTextContent(document, VALUE_XML, readConfigAndBuildXml(configFile)));

        return getEntityWithNameMapping(GENERIC_ENTITY_TYPE, name, id, genericEntityElement);
    }

    private String readConfigAndBuildXml(File configFile) {
        try {
            JsonNode jsonNode = jsonTools.getObjectMapper(getExtension(configFile.getName())).readTree(fileUtils.getInputStream(configFile));
            return this.xmlMapper.writer().withRootName("java").writeValueAsString(jsonNode);
        } catch (IOException e) {
            throw new EntityBuilderException("Could not load Generic Entity configuration file: " + configFile.getName(), e);
        }
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
