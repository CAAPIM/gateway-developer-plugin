/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Writes a yaml/json version of the generic entity xml to a specific folder structure config/genericEntities/${className}/${name}.json/yaml.
 */
@Singleton
public class GenericEntityXmlWriter implements EntityWriter {

    private final JsonTools jsonTools;
    private final XmlMapper xmlMapper;
    private final FileUtils fileUtils;

    @Inject
    public GenericEntityXmlWriter(JsonTools jsonTools, FileUtils fileUtils) {
        this.jsonTools = jsonTools;
        this.fileUtils = fileUtils;
        this.xmlMapper = new XmlMapper();
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
                jsonNode = xmlMapper.readTree(ge.getXml());
            } catch (IOException e) {
                throw new WriteException("Could not read Generic Entity " + ge.getName() + " XML Configuration", e);
            }

            File entityFile = new File(entityFolder, ge.getName() + "." + jsonTools.getFileExtension());
            try (OutputStream stream = fileUtils.getOutputStream(entityFile)){
                jsonTools.getObjectWriter().writeValue(stream, jsonNode);
            } catch (IOException e) {
                throw new WriteException("Could not write Generic Entity " + ge.getName() + " configuration to file: " + entityFile.toString(), e);
            }
        });
    }
}
