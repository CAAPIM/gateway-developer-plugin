/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.capublisherplugin;

import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.gradle.internal.impldep.org.junit.Assert;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CAGatewayDeveloperTest {
    private static final Logger LOGGER = Logger.getLogger(CAGatewayDeveloperTest.class.getName());
    private final String projectVersion = "-1.2.3-SNAPSHOT";

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
        validateBuildDir(projectFolder, buildDir);
    }

    // @Test
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
        File builtBundleFile = new File(buildGatewayDir, projectFolder + projectVersion + ".bundle");
        Assert.assertTrue(builtBundleFile.isFile());
    }

    @Test
    @ExtendWith(TemporaryFolderExtension.class)
    void testMultiProject(TemporaryFolder temporaryFolder) throws IOException, URISyntaxException {
        String projectFolder = "multi-project";
        File testProjectDir = new File(temporaryFolder.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build", "--stacktrace", "-PjarDir=" + System.getProperty("user.dir") + "/build/test-mvn-repo")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        Assert.assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":project-a:build")).getOutcome());
        Assert.assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":project-b:build")).getOutcome());

        validateBuildDir("project-a", new File(new File(testProjectDir, "project-a"), "build"));
        validateBuildDir("project-b", new File(new File(testProjectDir, "project-b"), "build"));
        validateBuildDir("project-c", new File(new File(testProjectDir, "project-c"), "build"));
        validateBuildDir("project-d", new File(new File(testProjectDir, "project-d"), "build"));

        File projectC_GW7 = new File(new File(new File(new File(testProjectDir, "project-c"), "build"), "gateway"), "project-c" + projectVersion + ".gw7");

        TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(projectC_GW7)));
        TarArchiveEntry entry;
        Set<String> entries = new HashSet<>();
        while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
            entries.add(entry.getName());
        }
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_1_project-b-1.2.3-SNAPSHOT.req.bundle"));
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_2_project-d-1.2.3-SNAPSHOT.req.bundle"));
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_3_project-a-1.2.3-SNAPSHOT.req.bundle"));
        assertTrue(entries.contains("opt/docker/rc.d/bundle/templatized/_4_project-c-1.2.3-SNAPSHOT.req.bundle"));
    }

    private void validateBuildDir(String projectName, File buildDir) {
        Assert.assertTrue(buildDir.isDirectory());
        File buildGatewayDir = new File(buildDir, "gateway");
        Assert.assertTrue(buildGatewayDir.isDirectory());
        File buildGatewayBundlesDir = new File(buildGatewayDir, "bundle");
        Assert.assertTrue(buildGatewayBundlesDir.isDirectory());
        File builtBundleFile = new File(buildGatewayBundlesDir, projectName + projectVersion + ".bundle");
        Assert.assertTrue(builtBundleFile.isFile());
        File gw7PackageFile = new File(buildGatewayDir, projectName + projectVersion + ".gw7");
        Assert.assertTrue(gw7PackageFile.isFile());
    }
}