/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport;

import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class CAGatewayExportTest {
    private static final Logger LOGGER = Logger.getLogger(CAGatewayExportTest.class.getName());

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testEnvironmentProperties(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "example-project";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        File buildGradleFile = new File(testProjectDir, "build.gradle");
        File bundleFile = new File(testProjectDir, "bundle.bundle");

        String gradleBuild = "" +
                "plugins {\n" +
                "    id 'com.ca.apim.gateway.gateway-export-plugin-base'\n" +
                "}\n" +
                "group 'com.ca'\n" +
                "version '1.2.3-SNAPSHOT'\n" +
                "task('explode', type: com.ca.apim.gateway.cagatewayexport.tasks.explode.ExplodeBundleTask) {\n" +
                "    folderPath = '/environment-variable'\n" +
                "    inputBundleFile = file('bundle.bundle')\n" +
                "    exportDir = file('gateway')\n" +
                "}";

        FileUtils.writeStringToFile(buildGradleFile, gradleBuild, Charset.defaultCharset());

        String bundle = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("bundles/environment-properties-test.bundle")).toURI()), Charset.defaultCharset());
        FileUtils.writeStringToFile(bundleFile, bundle, Charset.defaultCharset());

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("explode", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());

        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":explode")).getOutcome());

        File exportDir = new File(testProjectDir, "gateway");
        File configDir = new File(exportDir, "config");

        Properties environmentProperties = new Properties();
        environmentProperties.load(new FileReader(new File(configDir, "env.properties")));

        assertTrue(environmentProperties.containsKey("empty-value"));
        assertTrue(environmentProperties.containsKey("message-variable"));
        assertTrue(environmentProperties.containsKey("local.env.var"));
        assertTrue(environmentProperties.containsKey("gateway.my-global-property"));
        assertTrue(environmentProperties.containsKey("gateway.another.global"));
    }
}