/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.linker;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Bundle;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ClusterProperty;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.EnvironmentProperty;
import com.ca.apim.gateway.cagatewayexport.util.file.StripFirstLineStream;
import com.ca.apim.gateway.cagatewayexport.util.properties.OrderedProperties;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TemporaryFolderExtension.class)
class ClusterPropertyLinkerTest {

    private Bundle bundle;
    private ClusterPropertyLinker clusterPropertyLinker;

    @BeforeEach
    void setUp() {
        clusterPropertyLinker = new ClusterPropertyLinker();
        bundle = new Bundle();
        bundle.addEntity(new ClusterProperty("my.name", "my-value", "1"));
        bundle.addEntity(new ClusterProperty("ENV.my.name", "my-value", "2"));
        bundle.addEntity(new ClusterProperty("another", "my-value", "3"));
        bundle.addEntity(new ClusterProperty("ENV.hello", "my-value", "4"));
    }

    @Test
    void linkUnsupportedOperation() {
        assertThrows(UnsupportedOperationException.class, () -> clusterPropertyLinker.link(bundle, null));
    }

    @Test
    void linkNoStaticPropertiesFile(TemporaryFolder temporaryFolder) {
        clusterPropertyLinker.link(bundle, null, temporaryFolder.getRoot());

        assertEquals(0, bundle.getEntities(ClusterProperty.class).size());
        assertEquals(4, bundle.getEntities(EnvironmentProperty.class).size());

        verifyEnvironmentProperties();
    }

    @Test
    void linkStaticPropertiesFile(TemporaryFolder temporaryFolder) throws IOException {
        // Set up static properties file
        File configFolder = temporaryFolder.createDirectory("config");
        File propertiesFile = new File(configFolder, "static.properties");
        Properties staticProps = new OrderedProperties();
        staticProps.setProperty("my.name", "static value");
        staticProps.setProperty("another", "another static value");

        try (OutputStream outputStream = new StripFirstLineStream(new FileOutputStream(propertiesFile))) {
            staticProps.store(outputStream, null);
        }

        // Test
        clusterPropertyLinker.link(bundle, null, temporaryFolder.getRoot());

        assertEquals(2, bundle.getEntities(ClusterProperty.class).size());
        assertEquals(2, bundle.getEntities(EnvironmentProperty.class).size());

        // static.properties are considered ClusterProperties
        assertEquals("my.name", bundle.getEntities(ClusterProperty.class).get("1").getName());
        assertEquals("another", bundle.getEntities(ClusterProperty.class).get("3").getName());

        // value from static.properties is replaced
        assertEquals("my-value", bundle.getEntities(ClusterProperty.class).get("1").getValue());
        assertEquals("my-value", bundle.getEntities(ClusterProperty.class).get("3").getValue());

        verifyEnvironmentProperties();
    }

    private void verifyEnvironmentProperties() {
        assertTrue(bundle.getEntities(EnvironmentProperty.class).containsKey("GLOBAL:ENV.my.name"));
        assertEquals("ENV.my.name", bundle.getEntities(EnvironmentProperty.class).get("GLOBAL:ENV.my.name").getName());
        assertEquals(EnvironmentProperty.Type.GLOBAL, bundle.getEntities(EnvironmentProperty.class).get("GLOBAL:ENV.my.name").getType());
        assertTrue(bundle.getEntities(EnvironmentProperty.class).containsKey("GLOBAL:ENV.hello"));
        assertEquals("ENV.hello", bundle.getEntities(EnvironmentProperty.class).get("GLOBAL:ENV.hello").getName());
        assertEquals(EnvironmentProperty.Type.GLOBAL, bundle.getEntities(EnvironmentProperty.class).get("GLOBAL:ENV.hello").getType());

    }
}