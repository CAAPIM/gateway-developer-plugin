/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayexport.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentParseException;
import com.ca.apim.gateway.cagatewayexport.util.xml.DocumentTools;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayexport.util.json.JsonTools.YAML;

public class WriterHelper {

    private WriterHelper() {
    }

    public static Element stringToXML(DocumentTools documentTools, String string) throws DocumentParseException {
        Document document = documentTools.parse(string);
        documentTools.cleanup(document);
        return document.getDocumentElement();
    }

    /**
     * Write map of beans to config folder into rootFolder specified, using specified fileName, in format yaml.
     *
     * @param rootFolder root folder
     * @param documentFileUtils file utility
     * @param jsonTools json utility
     * @param beans map of beans to be written
     * @param fileName name of the file
     */
    public static <B> void writeFile(File rootFolder, DocumentFileUtils documentFileUtils, JsonTools jsonTools, Map<String, B> beans, String fileName) {
        File configFolder = new File(rootFolder, "config");
        documentFileUtils.createFolder(configFolder.toPath());

        File listenPortsFile = new File(configFolder, fileName);

        ObjectWriter yamlWriter = jsonTools.getObjectWriter(YAML);
        try (OutputStream fileStream = Files.newOutputStream(listenPortsFile.toPath())) {
            yamlWriter.writeValue(fileStream, beans);
        } catch (IOException e) {
            throw new WriteException("Exception writing " + fileName + " config file", e);
        }
    }

    /**
     * Copy one list to new one, returning null if the original is empty or null.
     *
     * @param originalList
     * @return null if originalList is null, otherwise new arraylist with the contents.
     */
    public static <B> List<B> copyList(List<B> originalList) {
        if (originalList == null) {
            return null;
        }

        return new ArrayList<>(originalList);
    }

    /**
     * Copy one map to new one, returning null if the original is empty or null.
     *
     * @param originalMap
     * @return null if originalMap is null, otherwise new linked hash map with the contents.
     */
    public static <K, V> Map<K, V> copyMap(Map<K, V> originalMap) {
        if (originalMap == null) {
            return null;
        }

        // keeping original order if any
        return new LinkedHashMap<>(originalMap);
    }
}
