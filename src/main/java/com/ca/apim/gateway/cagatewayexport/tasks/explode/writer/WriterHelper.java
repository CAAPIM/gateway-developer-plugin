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

    public static <B> List<B> copyList(List<B> originalList) {
        if (originalList == null || originalList.isEmpty()) {
            return null;
        }

        return new ArrayList<>(originalList);
    }

    public static <K, V> Map<K, V> copyMap(Map<K, V> originalMap) {
        if (originalMap == null) {
            return null;
        }

        // keeping original order if any
        return new LinkedHashMap<>(originalMap);
    }
}
