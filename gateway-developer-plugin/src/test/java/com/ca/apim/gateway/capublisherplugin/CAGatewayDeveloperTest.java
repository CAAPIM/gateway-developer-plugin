/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.capublisherplugin;

import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.io.FileUtils;
import org.gradle.internal.impldep.org.junit.Assert;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

class CAGatewayDeveloperTest {
    private static final Logger LOGGER = Logger.getLogger(CAGatewayDeveloperTest.class.getName());

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExampleProject(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "example-project";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        Assert.assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build")).getOutcome());

        File buildDir = new File(testProjectDir, "build");
        Assert.assertTrue(buildDir.isDirectory());
        File buildGatewayDir = new File(buildDir, "gateway");
        Assert.assertTrue(buildGatewayDir.isDirectory());
        File buildGatewayBundlesDir = new File(buildGatewayDir, "bundle");
        Assert.assertTrue(buildGatewayBundlesDir.isDirectory());
        File builtBundleFile = new File(buildGatewayBundlesDir, projectFolder + ".req.bundle");
        Assert.assertTrue(builtBundleFile.isFile());
        File gw7PackageFile = new File(buildGatewayDir, projectFolder + ".gw7");
        Assert.assertTrue(gw7PackageFile.isFile());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testExampleProjectCustomOrganization(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "example-project-custom-organization";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        Assert.assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build")).getOutcome());

        File buildGatewayDir = new File(testProjectDir, "dist");
        Assert.assertTrue(buildGatewayDir.isDirectory());
        File builtBundleFile = new File(buildGatewayDir, "example-project-custom-organization.req.bundle");
        Assert.assertTrue(builtBundleFile.isFile());
    }
}