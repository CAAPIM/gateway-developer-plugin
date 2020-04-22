/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.json;

import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;
import static org.apache.commons.io.FilenameUtils.getExtension;

public class JsonTools {
    private static final Logger LOGGER = Logger.getLogger(JsonTools.class.getName());
    public static final JsonTools INSTANCE = new JsonTools(FileUtils.INSTANCE);

    public static final String JSON = "json";
    public static final String YAML = "yaml";
    public static final String JSON_EXTENSION = "json";
    public static final String YML_EXTENSION = "yml";
    private static final String YAML_EXTENSION = "yaml";
    private final Map<String, ObjectMapper> objectMapperMap = new HashMap<>();
    private final FileUtils fileUtils;
    private String outputType;
    private String fileExtension;

    public JsonTools(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
        objectMapperMap.put(JSON, buildObjectMapper(new JsonFactory()));
        objectMapperMap.put(YAML, buildObjectMapper(new YAMLFactory().disable(WRITE_DOC_START_MARKER)));
        outputType = YAML;
        fileExtension = "." + YML_EXTENSION;
    }

    public ObjectMapper getObjectMapper() {
        return getObjectMapper(outputType);
    }

    public ObjectWriter getObjectWriter() {
        return getObjectWriter(this.outputType);
    }

    public ObjectWriter getObjectWriter(String outputType) {
        return getObjectMapper(outputType).writer().withDefaultPrettyPrinter();
    }

    public ObjectMapper getObjectMapper(final String type) {
        ObjectMapper objectMapper = objectMapperMap.get(type);
        if (objectMapper == null) {
            throw new IllegalArgumentException("Unknown object mapper for type: " + type);
        }
        return objectMapper;
    }

    public File getDocumentFile(final File rootDir, final String fileName) {
        return Optional.ofNullable(findFile(rootDir, fileName)).orElseGet(() -> findFile(new File(rootDir, "config"), fileName));
    }

    private File findFile(final File directory, final String fileName) {
        final File jsonFile = new File(directory, fileName + "." + JSON_EXTENSION);
        final File yamlFile = new File(directory, fileName + "." + YAML_EXTENSION);
        final File ymlFile = new File(directory, fileName + "." + YML_EXTENSION);

        File fileToUse = null;
        if (jsonFile.exists() && yamlFile.exists() && ymlFile.exists()) {
            throw new JsonToolsException("Can have either a " + fileName + ".json or a " + fileName + ".yml not both.");
        } else if (jsonFile.isFile()) {
            fileToUse = jsonFile;
        } else if (yamlFile.isFile()) {
            fileToUse = yamlFile;
        } else if (ymlFile.isFile()) {
            fileToUse = ymlFile;
        }

        if (fileToUse == null) {
            LOGGER.log(Level.FINE, "Did not find a {0} configuration file. Not loading any.", fileName);
        } else {
            LOGGER.log(Level.FINE, "Found file: {0}", fileToUse);
        }
        return fileToUse;
    }

    public <T> T readDocumentFile(final File file, final JavaType entityMapType) {
        final String type = getTypeFromFile(file);
        try (InputStream stream = fileUtils.getInputStream(file)) {
            return readStream(stream, type, entityMapType);
        } catch (IOException e) {
            throw new JsonToolsException("Could not parse configuration file for type: " + entityMapType.getGenericSignature() + " Message:" + e.getMessage(), e);
        }
    }

    public <T> T readStream(final InputStream stream, final String type, final JavaType entityMapType) {
        final ObjectMapper objectMapper = getObjectMapper(type);
        try {
            return objectMapper.readValue(stream, entityMapType);
        } catch (IOException e) {
            throw new JsonToolsException("Could not parse configuration file for type: " + entityMapType.getGenericSignature() + " Message:" + e.getMessage(), e);
        }
    }

    @NotNull
    public String getTypeFromFile(File file) {
        String type = getTypeFromExtension(getExtension(file.getName()));
        if(type == null) {
            throw new JsonToolsException("Invalid file: " + file.getName() + ". Expecting json or yaml file formats.");
        }
        return type;
    }

    @Nullable
    public String getTypeFromExtension(String extension) {
        switch (extension) {
            case JSON_EXTENSION:
                return JSON;
            case YML_EXTENSION:
            case YAML_EXTENSION:
                return YAML;
            default:
                return null;
        }
    }

    public void setOutputType(String outputType) {
        if (JSON.equalsIgnoreCase(outputType)) {
            this.outputType = JSON;
            fileExtension = "." + JSON_EXTENSION;
        } else if (YAML.equalsIgnoreCase(outputType)) {
            this.outputType = YAML;
            fileExtension = "." + YML_EXTENSION;
        } else {
            LOGGER.log(Level.WARNING,
                    "Output type specified is not YAML nor JSON. Using YAML as the default output type.");
            this.outputType = YAML;
            fileExtension = "." + YML_EXTENSION;
        }
    }

    public String getFileExtension() {
        return fileExtension;
    }


    private static ObjectMapper buildObjectMapper(JsonFactory jf) {
        ObjectMapper objectMapper = new ObjectMapper(jf);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // identityProviderDetail is determined by type. In the case of Federated ID provider,
        // if it does not have any references to certs, an identityProviderDetail is not generated. Normally this would
        // throw JsonToolsException. See test case IdentityProviderLoaderTest#loadFedIdWithNoCertRefs()
        objectMapper.configure(FAIL_ON_MISSING_EXTERNAL_TYPE_ID_PROPERTY, false);
        if (jf instanceof YAMLFactory) {
            //make it so that null values get left as blanks rather then the string `null`
            objectMapper.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
                @Override
                public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.writeNumber("");
                }
            });
        }
        return objectMapper;
    }

    public void writeObject(Object object, File file) throws IOException {
        ObjectWriter objectWriter = getObjectWriter();
        try (OutputStream fileStream = Files.newOutputStream(file.toPath())) {
            objectWriter.writeValue(fileStream, object);
        } catch (IOException e) {
            throw e;
        }
    }
}
