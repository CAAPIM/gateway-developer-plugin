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
import org.gradle.testkit.runner.UnexpectedBuildFailure;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CAGatewayExportTest {
    private static final Logger LOGGER = Logger.getLogger(CAGatewayExportTest.class.getName());

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExplode(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
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
                "    exportEntities = [\n" +
                "        passwords: [ \"my-password\" ],\n" +
                "        clusterProperties: [ \"that-property\" ]\n" +
                "    ]\n" +
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
        try (FileReader reader = new FileReader(new File(configDir, "env.properties"))) {
            environmentProperties.load(reader);
        }

        assertTrue(environmentProperties.containsKey("empty-value"));
        assertTrue(environmentProperties.containsKey("message-variable"));
        assertTrue(environmentProperties.containsKey("local.env.var"));
        assertTrue(environmentProperties.containsKey("gateway.ENV.my-global-property"));
        assertTrue(environmentProperties.containsKey("gateway.ENV.another.global"));

        assertFalse(new File(configDir, "static.properties").exists());

        File passwordsFile = new File(configDir, "stored-passwords.properties");
        assertTrue(passwordsFile.exists());
        Properties passwordProperties = new Properties();
        try (FileReader reader = new FileReader(passwordsFile)) {
            passwordProperties.load(reader);
        }
        assertTrue(passwordProperties.containsKey("my-password"));
        assertFalse(passwordProperties.containsKey("another-password"));
        assertEquals(1, passwordProperties.size());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExplodeUnknownExportEntityType(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
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
                "    exportEntities = [\n" +
                "        passwords: [ \"my-password\" ],\n" +
                "        unknownEntities: [ \"some-other-entity\" ],\n" +
                "        clusterProperties: [ \"that-property\" ]\n" +
                "    ]\n" +
                "}";

        FileUtils.writeStringToFile(buildGradleFile, gradleBuild, Charset.defaultCharset());

        String bundle = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("bundles/environment-properties-test.bundle")).toURI()), Charset.defaultCharset());
        FileUtils.writeStringToFile(bundleFile, bundle, Charset.defaultCharset());

        UnexpectedBuildFailure exception = assertThrows(UnexpectedBuildFailure.class, () -> GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("explode", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build());

        assertTrue(exception.getMessage().contains("unknownEntities"));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExplodeMissingExportEntity(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
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
                "    exportEntities = [\n" +
                "        passwords: [ \"my-password\", \"unknown-password\" ],\n" +
                "        clusterProperties: [ \"that-property\" ]\n" +
                "    ]\n" +
                "}";

        FileUtils.writeStringToFile(buildGradleFile, gradleBuild, Charset.defaultCharset());

        String bundle = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("bundles/environment-properties-test.bundle")).toURI()), Charset.defaultCharset());
        FileUtils.writeStringToFile(bundleFile, bundle, Charset.defaultCharset());

        UnexpectedBuildFailure exception = assertThrows(UnexpectedBuildFailure.class, () -> GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("explode", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build());

        assertTrue(exception.getMessage().contains("unknown-password"));
        assertTrue(exception.getMessage().contains("Missing"));
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExplodeMismatchFolderPath(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
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
                "    folderPath = '/non-existing-folder'\n" +
                "    inputBundleFile = file('bundle.bundle')\n" +
                "    exportDir = file('gateway')\n" +
                "    exportEntities = [\n" +
                "        passwords: [ \"my-password\" ],\n" +
                "        unknownEntities: [ \"some-other-entity\" ],\n" +
                "        clusterProperties: [ \"that-property\" ]\n" +
                "    ]\n" +
                "}";

        FileUtils.writeStringToFile(buildGradleFile, gradleBuild, Charset.defaultCharset());

        String bundle = FileUtils.readFileToString(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("bundles/environment-properties-test.bundle")).toURI()), Charset.defaultCharset());
        FileUtils.writeStringToFile(bundleFile, bundle, Charset.defaultCharset());

        UnexpectedBuildFailure exception = assertThrows(UnexpectedBuildFailure.class, () -> GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("explode", "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .build());

        assertTrue(exception.getMessage().contains("does not exist in the target gateway."));
    }

}