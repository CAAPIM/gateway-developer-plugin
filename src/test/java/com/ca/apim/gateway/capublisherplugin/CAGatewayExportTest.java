/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.capublisherplugin;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

@Ignore
public class CAGatewayExportTest {
    private static final Logger LOGGER = Logger.getLogger(CAGatewayExportTest.class.getName());

    @Rule
    public final TemporaryFolder rootProjectDir = new TemporaryFolder();

    @Test
    public void testExampleProject() throws IOException, URISyntaxException {
        String projectFolder = "example-project-live";
        File testProjectDir = new File(rootProjectDir.getRoot(), projectFolder);
        FileUtils.copyDirectory(new File(Objects.requireNonNull(getClass().getClassLoader().getResource(projectFolder)).toURI()), testProjectDir);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("export")
                .withPluginClasspath()
                .withDebug(true)
                .build();

        LOGGER.log(Level.INFO, result.getOutput());

        assertEquals(TaskOutcome.SUCCESS, Objects.requireNonNull(result.task(":export")).getOutcome());
    }
}