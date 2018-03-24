/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.capublisherplugin;

import org.apache.commons.io.FileUtils;
import org.gradle.internal.impldep.org.junit.Assert;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CAGatewayDeveloperTest {
    private static final Logger LOGGER = Logger.getLogger(CAGatewayDeveloperTest.class.getName());

    @Rule
    public final TemporaryFolder rootProjectDir = new TemporaryFolder();

    @Test
    public void testExampleProject() throws IOException, URISyntaxException {
        String projectFolder = "example-project";
        File testProjectDir = new File(rootProjectDir.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());
        Assert.assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":build")).getOutcome());

        File buildDir = new File(testProjectDir, "build");
        Assert.assertTrue(buildDir.isDirectory());
        File buildGatewayDir = new File(buildDir, "gateway");
        Assert.assertTrue(buildGatewayDir.isDirectory());
        File builtBundleFile = new File(buildGatewayDir, projectFolder + ".bundle");
        Assert.assertTrue(builtBundleFile.isFile());
    }
}