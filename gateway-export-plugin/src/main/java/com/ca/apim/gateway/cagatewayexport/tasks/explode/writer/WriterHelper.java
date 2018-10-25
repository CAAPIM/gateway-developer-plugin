/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.file.StripFirstLineStream;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static com.ca.apim.gateway.cagatewayexport.util.properties.PropertyFileUtils.loadExistingProperties;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

class WriterHelper {

    private static final String ERROR_WRITE = "Exception writing %s config file";

    private WriterHelper() {
    }

    /**
     * Write {@link Properties} map to config folder into rootFolder specified, using specified filename.
     *
     * @param rootFolder root folder
     * @param documentFileUtils file utility
     * @param properties Properties to be written
     * @param fileName name of the file
     */
    static void writePropertiesFile(File rootFolder, DocumentFileUtils documentFileUtils, Properties properties, String fileName) {
        if (properties.isEmpty()) {
            return;
        }

        File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());

        File propertiesFile = new File(configFolder, fileName + ".properties");
        Properties currentProperties = loadExistingProperties(propertiesFile);
        if (!currentProperties.isEmpty()) {
            // iterate the new properties and join them to the current properties contents
            // new property value is chosen except if its value is null
            properties
                    .entrySet()
                    .stream()
                    .filter(e -> isNotEmpty((String) e.getValue()))
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
    static <B> void writeFile(File rootFolder, DocumentFileUtils documentFileUtils, JsonTools jsonTools, Map<String, B> beans, String fileName, Class<B> beanClass) {
        if (beans.isEmpty()) {
            return;
        }

        File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());
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

    /**
     * Copy one set to new one, returning null if the original is empty or null.
     *
     * @param originalSet original set of beans
     * @return empty list if originalSet is null, otherwise new linkedhashset with the contents.
     * @param <B> type of bean
     */
    static <B> Set<B> copySet(Set<B> originalSet) {
        if (originalSet == null) {
            return emptySet();
        }

        return new LinkedHashSet<>(originalSet);
    }

    /**
     * Copy one map to new one, returning null if the original is empty or null.
     *
     * @param originalMap original map of beans
     * @return empty map if originalMap is null, otherwise new linked hash map with the contents.
     * @param <K> type of key
     * @param <V> type of value
     */
    static <K, V> Map<K, V> copyMap(Map<K, V> originalMap) {
        if (originalMap == null) {
            return emptyMap();
        }

        // keeping original order if any
        return new LinkedHashMap<>(originalMap);
    }

}
