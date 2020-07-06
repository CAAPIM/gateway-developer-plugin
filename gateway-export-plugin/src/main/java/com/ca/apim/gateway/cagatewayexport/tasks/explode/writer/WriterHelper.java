/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.EntityUtils.GatewayEntityInfo;
import com.ca.apim.gateway.cagatewayconfig.beans.GatewayEntity;
import com.ca.apim.gateway.cagatewayconfig.beans.PropertiesEntity;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.util.file.StripFirstLineStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.PROPERTIES;
import static com.ca.apim.gateway.cagatewayexport.util.properties.PropertyFileUtils.loadExistingProperties;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

class WriterHelper {

    private static final String CONFIG_DIRECTORY = "config";
    private static final String ERROR_WRITE = "Exception writing %s config file";

    private WriterHelper() {
    }

    static void write(Bundle bundle, File rootFolder, GatewayEntityInfo info, DocumentFileUtils documentFileUtils, JsonTools jsonTools) {
        if (info.getFileType() == JSON_YAML) {
            writeFile(rootFolder, documentFileUtils, jsonTools, bundle.getEntities(info.getEntityClass()), info.getFileName(), info.getEntityClass());
        } else if (info.getFileType() == PROPERTIES) {
            writePropertiesFile(rootFolder, documentFileUtils, bundle.getEntities(info.getEntityClass()), info.getFileName());
        } else {
            throw new WriteException("Unsupported file type: " + info.getFileType());
        }
    }


    /**
     * Write beans map to config folder into rootFolder specified, using specified filename.
     *
     * @param rootFolder root folder
     * @param documentFileUtils file utility
     * @param beans beans to be written as properties files
     * @param fileName name of the file
     */
    private static <B extends GatewayEntity> void writePropertiesFile(File rootFolder, DocumentFileUtils documentFileUtils, Map<String, B> beans, String fileName) {
        File configFolder = new File(rootFolder, CONFIG_DIRECTORY);
        documentFileUtils.createFolder(configFolder.toPath());

        Properties properties = new Properties();
        properties.putAll(beans
                .values()
                .stream()
                .map(b -> {
                    b.preWrite(configFolder, documentFileUtils);
                    return (PropertiesEntity) b;
                })
                .collect(toMap(PropertiesEntity::getKey, PropertiesEntity::getValue)));

        writePropertiesFile(rootFolder, documentFileUtils, properties, fileName);
    }

    /**
     * Write {@link Properties} map to config folder into rootFolder specified, using specified filename.
     *
     * @param rootFolder root folder
     * @param documentFileUtils file utility
     * @param properties Properties to be written
     * @param fileName name of the file
     */
    static synchronized void writePropertiesFile(File rootFolder, DocumentFileUtils documentFileUtils, Properties properties, String fileName) {
        if (properties.isEmpty()) {
            return;
        }

        File configFolder = new File(rootFolder, CONFIG_DIRECTORY);
        documentFileUtils.createFolder(configFolder.toPath());

        File propertiesFile = new File(configFolder, fileName + ".properties");
        Properties currentProperties = loadExistingProperties(propertiesFile);
        if (!currentProperties.isEmpty()) {
            // iterate the new properties and join them to the current properties contents
            // new property value is chosen except if its value is null
            properties
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue() != null)
                    .forEach(e -> currentProperties.put(e.getKey(), e.getValue()));
            properties = currentProperties;
        }
        try (OutputStream outputStream = new StripFirstLineStream(new FileOutputStream(propertiesFile))) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            throw new WriteException("Could not create " + fileName + " properties file: " + e.getMessage(), e);
        }
    }

    /**
     * Write map of beans to config folder into rootFolder specified, using specified fileName, in format yaml.
     *
     * @param rootFolder root folder
     * @param documentFileUtils file utility
     * @param jsonTools json utility
     * @param beans map of beans to be written
     * @param fileName name of the file
     * @param beanClass The class type of the bean
     * @param <B> type of bean
     */
    static <B extends GatewayEntity> void writeFile(File rootFolder, DocumentFileUtils documentFileUtils, JsonTools jsonTools, Map<String, B> beans, String fileName, Class<B> beanClass) {
        if (beans.isEmpty()) {
            return;
        }

        File configFolder = new File(rootFolder, CONFIG_DIRECTORY);
        documentFileUtils.createFolder(configFolder.toPath());

        // remap the beans by name and run pre-write methods
        LinkedHashMap<String, B> beansByName = new LinkedHashMap<>();
        beans.forEach((k, v) -> {
            v.preWrite(configFolder, documentFileUtils);
            beansByName.put(v.getMappingValue(), v);
        });
        beans = beansByName;

        ObjectWriter objectWriter = jsonTools.getObjectWriter();

        // check if a current file exists and merge contents
        File configFile = new File(configFolder, fileName + jsonTools.getFileExtension());
        if (configFile.exists()) {
            // then write the new beans first to a byte stream
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try {
                objectWriter.writeValue(byteStream, beans);
            } catch (IOException e) {
                throw new WriteException(format(ERROR_WRITE, fileName), e);
            }

            // read the old contents from the current file and then set them to be joined
            // with the new contents from the byte stream
            try {
                final ObjectMapper objectMapper = jsonTools.getObjectMapper();
                // Use LinkedHashMap since it preserves the order of entities in the file
                final MapType type = objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, beanClass);
                final Map<String, B> contents = objectMapper.readValue(configFile, type);
                beans = objectMapper.readerForUpdating(contents).readValue(new ByteArrayInputStream(byteStream.toByteArray()));
            } catch (IOException e) {
                throw new WriteException("Exception reading existing contents from " + fileName + " config file", e);
            }
        }

        // last write the merged map of beans to the config file
        try (OutputStream fileStream = Files.newOutputStream(configFile.toPath())) {
            objectWriter.writeValue(fileStream, beans);
        } catch (IOException e) {
            throw new WriteException(format(ERROR_WRITE, fileName), e);
        }
    }

}
