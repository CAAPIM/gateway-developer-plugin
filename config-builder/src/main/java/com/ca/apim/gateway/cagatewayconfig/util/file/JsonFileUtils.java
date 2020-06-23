/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.file;

import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleDefinedEntities;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.BundleMetadata;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.closeQuietly;
import static com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools.YAML_EXTENSION;

public class JsonFileUtils {

    public static final String METADATA_FILE_NAME_SUFFIX = ".metadata" + JsonTools.INSTANCE.getFileExtension();
    private static final String CONFIG_DIR = "config";
    private static final String POLICIES_CONFIG_FILE = "policies" + JsonTools.INSTANCE.getFileExtension();

    public static final JsonFileUtils INSTANCE = new JsonFileUtils(JsonTools.INSTANCE);

    private final JsonTools jsonTools;

    private JsonFileUtils(final JsonTools jsonTools) {
        this.jsonTools = jsonTools;
    }

    public void createFile(Object objectToWrite, Path path) {
        OutputStream fos = null;
        try {
            fos = Files.newOutputStream(path);
            jsonTools.writeObject(objectToWrite, fos);
        } catch (IOException e) {
            throw new JsonFileUtilsException("Error writing to file '" + path + "': " + e.getMessage(), e);
        } finally {
            closeQuietly(fos);
        }
    }

    public <T> Map<String, T> readPoliciesConfigFile(final File rootDir, Class<T> tClass) {
        final File file = getPoliciesConfigFile(rootDir);
        return file.exists() ? jsonTools.readDocumentFile(getPoliciesConfigFile(rootDir),
                jsonTools.getObjectMapper().getTypeFactory().constructMapType(HashMap.class, String.class, tClass)) :
                null;
    }

    public void writePoliciesConfigFile(Object object, final File rootDir) {
        File configFolder = new File(rootDir, CONFIG_DIR);
        DocumentFileUtils documentFileUtils = DocumentFileUtils.INSTANCE;
        documentFileUtils.createFolder(configFolder.toPath());
        createFile(object, getPoliciesConfigFile(rootDir).toPath());
    }

    private File getPoliciesConfigFile(final File rootDir) {
        return new File(new File(rootDir, CONFIG_DIR), POLICIES_CONFIG_FILE);
    }

    public void createBundleMetadataFile(Object objectToWrite, String fileName, File outputDir) {
        createFile(objectToWrite, new File(outputDir, fileName + METADATA_FILE_NAME_SUFFIX).toPath());
    }

    public BundleDefinedEntities readBundleMetadataFile(final File metaDataFile) {
        return metaDataFile.exists() ? jsonTools.readDocumentFile(metaDataFile,
                jsonTools.getObjectMapper().getTypeFactory().constructType(BundleDefinedEntities.class)) :
                null;
    }

    public <T> T readBundleMetadataFile(final File metaDataFile, Class<T> tClass) {
        try {
            return jsonTools.getObjectMapper(YAML_EXTENSION).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).
                    readValue(metaDataFile, tClass);
        } catch (IOException e) {
            throw new JsonFileUtilsException("Error reading the bundle metadata file " + metaDataFile.toString(), e);
        }
    }
}
