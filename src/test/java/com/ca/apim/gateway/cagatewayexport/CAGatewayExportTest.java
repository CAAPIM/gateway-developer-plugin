/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport;

import com.ca.apim.gateway.cagatewayexport.util.json.JsonTools;
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
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class CAGatewayExportTest {
    private static final Logger LOGGER = Logger.getLogger(CAGatewayExportTest.class.getName());

    private final String OUTPUT_TYPE_OPTION = "--outputType";
    private final String EXPLODE_TASK = "explode";

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testEnvironmentProperties(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        File testProjectDir = setUpTestProjectDir("env-test", "environment-variable", temporaryFolder, "bundles/environment-properties-test.bundle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("explode", "--stacktrace")
                .withArguments(EXPLODE_TASK)
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

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testYamlExportAsDefault(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        File testProjectDir = setUpTestProjectDir("yml-default", "", temporaryFolder, "bundles/export-test.bundle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(EXPLODE_TASK)
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());

        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":explode")).getOutcome());

        validateFilesOutputTypeFromDir(testProjectDir, JsonTools.YAML_FILE_EXTENSION);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testJsonExport(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        File testProjectDir = setUpTestProjectDir("json-export", "", temporaryFolder, "bundles/export-test.bundle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(EXPLODE_TASK, OUTPUT_TYPE_OPTION, JsonTools.JSON)
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());

        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":explode")).getOutcome());

        validateFilesOutputTypeFromDir(testProjectDir, JsonTools.JSON_FILE_EXTENSION);

        JsonTools.INSTANCE.setOutputType(JsonTools.YAML);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testYamlExportWhenOutPutTypeNotRecognized(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        File testProjectDir = setUpTestProjectDir("resort-to-yml", "", temporaryFolder, "bundles/export-test.bundle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(EXPLODE_TASK, OUTPUT_TYPE_OPTION, "xml")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());

        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":explode")).getOutcome());

        validateFilesOutputTypeFromDir(testProjectDir, JsonTools.YAML_FILE_EXTENSION);
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testYamlExport(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        File testProjectDir = setUpTestProjectDir("yml-export", "", temporaryFolder, "bundles/export-test.bundle");

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(EXPLODE_TASK, OUTPUT_TYPE_OPTION, JsonTools.YAML)
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());

        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":explode")).getOutcome());

        validateFilesOutputTypeFromDir(testProjectDir, JsonTools.YAML_FILE_EXTENSION);
    }

    private File setUpTestProjectDir(String projectName, String folderPath, TemporaryFolder temporaryFolder, String resourceBundle) throws IOException, URISyntaxException {
        File testProjectDir = new File(temporaryFolder.getRoot(), projectName);
        File buildGradleFile = new File(testProjectDir, "build.gradle");
        File bundleFile = new File(testProjectDir, "bundle.bundle");

        String gradleBuild = "" +
                "plugins {\n" +
                "    id 'com.ca.apim.gateway.gateway-export-plugin-base'\n" +
                "}\n" +
                "group 'com.ca'\n" +
                "version '1.2.3-SNAPSHOT'\n" +
                "task('explode', type: com.ca.apim.gateway.cagatewayexport.tasks.explode.ExplodeBundleTask) {\n" +
                "    folderPath = '/" + folderPath + "'\n" +
                "    inputBundleFile = file('bundle.bundle')\n" +
                "    exportDir = file('gateway')\n" +
                "}";

        FileUtils.writeStringToFile(buildGradleFile, gradleBuild, Charset.defaultCharset());

        String bundle = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(resourceBundle)).toURI()), Charset.defaultCharset());
        FileUtils.writeStringToFile(bundleFile, bundle, Charset.defaultCharset());

        return testProjectDir;
    }

    private void validateFilesOutputTypeFromDir(File testProjectDir, String outputType) {
        File exportDir = new File(testProjectDir, "gateway");
        File configDir = new File(exportDir, "config");
        File[] files = configDir.listFiles();
        Arrays.stream(files)
                //Every config file except properties files should be the outputType
                .filter(file -> !file.getName().equals("static.properties") && !file.getName().equals("env.properties"))
                .collect(Collectors.toList())
                .forEach(file -> assertEquals(file.getName().substring(file.getName().length() - outputType.length()), outputType));
    }
}