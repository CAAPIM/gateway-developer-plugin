/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.properties;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class PropertyFileUtils {

    @NotNull
    public static Properties loadExistingProperties(File propertiesFile) {
        Properties properties = new OrderedProperties();
        // check if the properties file exist and load current properties into the properties object
        if (propertiesFile.exists()) {
            try (InputStream stream = Files.newInputStream(propertiesFile.toPath())) {
                properties.load(stream);
            } catch (IOException e) {
                throw new PropertyFileException("Exception reading existing contents from " + propertiesFile.getName() + " file", e);
            }
        }
        return properties;
    }

    private PropertyFileUtils(){}
}
