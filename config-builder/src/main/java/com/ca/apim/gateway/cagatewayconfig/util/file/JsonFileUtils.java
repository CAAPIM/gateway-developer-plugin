package com.ca.apim.gateway.cagatewayconfig.util.file;

import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils.closeQuietly;

public class JsonFileUtils {

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
            throw new DocumentFileUtilsException("Error writing to file '" + path + "': " + e.getMessage(), e);
        } finally {
            closeQuietly(fos);
        }
    }
}
