/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.properties;

import com.ca.apim.gateway.cagatewayexport.util.file.StripFirstLineStream;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import static com.ca.apim.gateway.cagatewayexport.util.properties.PropertyFileUtils.loadExistingProperties;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TemporaryFolderExtension.class)
class PropertyFileUtilsTest {

    @Test
    void loadExistingPropertiesNoProperties(TemporaryFolder temporaryFolder) {
        assertTrue(loadExistingProperties(new File(temporaryFolder.getRoot(), "static.properties")).isEmpty());
    }

    @Test
    void loadExistingPropertiesFromFile(TemporaryFolder temporaryFolder) throws IOException {
        File propertiesFile = new File(temporaryFolder.getRoot(), "static.properties");
        Properties staticProps = new OrderedProperties();
        staticProps.setProperty("my.name", "static value");
        staticProps.setProperty("another", "another static value");

        try (OutputStream outputStream = new StripFirstLineStream(new FileOutputStream(propertiesFile))) {
            staticProps.store(outputStream, null);
        }

        Properties properties = loadExistingProperties(propertiesFile);
        assertTrue(properties.containsKey("my.name"));
        assertTrue(properties.containsKey("another"));
    }

}