/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

class WriterHelper {

    private WriterHelper() {
    }

    /**
     * Write map of beans to config folder into rootFolder specified, using specified fileName, in format yaml.
     *
     * @param rootFolder root folder
     * @param documentFileUtils file utility
     * @param jsonTools json utility
     * @param beans map of beans to be written
     * @param fileName name of the file
     * @param <B> type of bean
     */
    static <B> void writeFile(File rootFolder, DocumentFileUtils documentFileUtils, JsonTools jsonTools, Map<String, B> beans, String fileName) {
        File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());

        File configFile = new File(configFolder, fileName + jsonTools.getFileExtension());

        ObjectWriter objectWriter = jsonTools.getObjectWriter();
        try (OutputStream fileStream = Files.newOutputStream(configFile.toPath())) {
            objectWriter.writeValue(fileStream, beans);
        } catch (IOException e) {
            throw new WriteException("Exception writing " + fileName + " config file", e);
        }
    }

    /**
     * Copy one list to new one, returning null if the original is empty or null.
     *
     * @param originalList original list of beans
     * @return empty list if originalList is null, otherwise new arraylist with the contents.
     * @param <B> type of bean
     */
    static <B> List<B> copyList(List<B> originalList) {
        if (originalList == null) {
            return emptyList();
        }

        return new ArrayList<>(originalList);
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
