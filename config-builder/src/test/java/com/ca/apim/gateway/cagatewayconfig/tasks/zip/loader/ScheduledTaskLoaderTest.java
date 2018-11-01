/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ScheduledTask;
import com.ca.apim.gateway.cagatewayconfig.util.file.FileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonToolsException;
import io.github.glytching.junit.extension.folder.TemporaryFolder;
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Extensions({ @ExtendWith(MockitoExtension.class), @ExtendWith(TemporaryFolderExtension.class) })
class ScheduledTaskLoaderTest {

    private static final String NAME = "scheduled task";
    private static final String POLICY_PATH = "gateway-solution/policy.xml";

    private TemporaryFolder rootProjectDir;
    private JsonTools jsonTools;
    @Mock
    private FileUtils fileUtils;

    @BeforeEach
    void setUp(final TemporaryFolder temporaryFolder) {
        jsonTools = new JsonTools(fileUtils);
        rootProjectDir = temporaryFolder;
    }

    @Test
    void loadOneTimeYaml() throws IOException {
        String yaml = NAME + ":\n" +
                "  policy: \"" + POLICY_PATH + "\"\n" +
                "  isOneNode: false\n" +
                "  jobType: \"One time\"\n" +
                "  jobStatus: \"Scheduled\"\n" +
                "  executionDate: \"2018-11-16T21:40:21.326Z\"\n" +
                "  shouldExecuteOnCreate: false\n" +
                "  properties:\n" +
                "    idProvider: \"0000000000000000fffffffffffffffe\"\n" +
                "    userId: \"00000000000000000000000000000003\"";
        load(yaml, "yml", false, true);
    }

    @Test
    void loadOneTimeJson() throws IOException {
        String json = "{\n" +
                "  \"" + NAME + "\" : {\n" +
                "    \"policy\" : \"" + POLICY_PATH + "\",\n" +
                "    \"isOneNode\" : false,\n" +
                "    \"jobType\" : \"One time\",\n" +
                "    \"jobStatus\" : \"Scheduled\",\n" +
                "    \"executionDate\" : \"2018-11-16T21:40:21.326Z\",\n" +
                "    \"shouldExecuteOnCreate\" : false,\n" +
                "    \"properties\" : {\n" +
                "      \"idProvider\" : \"0000000000000000fffffffffffffffe\",\n" +
                "      \"userId\" : \"00000000000000000000000000000003\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        load(json, "json", false, true);
    }

    @Test
    void loadRecurringJson() throws IOException {
        String json = "{\n" +
                "  \"" + NAME + "\" : {\n" +
                "    \"policy\" : \"" + POLICY_PATH + "\",\n" +
                "    \"isOneNode\" : false,\n" +
                "    \"jobType\" : \"Recurring\",\n" +
                "    \"jobStatus\" : \"Disabled\",\n" +
                "    \"cronExpression\" : \"* * * * * ?\",\n" +
                "    \"shouldExecuteOnCreate\" : false,\n" +
                "    \"properties\" : {\n" +
                "      \"idProvider\" : \"0000000000000000fffffffffffffffe\",\n" +
                "      \"userId\" : \"00000000000000000000000000000003\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        load(json, "json", false, false);
    }

    @Test
    void loadRecurringYaml() throws IOException {
        String json = NAME + ":\n" +
                "  policy: \"" + POLICY_PATH + "\"\n" +
                "  isOneNode: false\n" +
                "  jobType: \"Recurring\"\n" +
                "  jobStatus: \"Disabled\"\n" +
                "  cronExpression: \"* * * * * ?\"\n" +
                "  shouldExecuteOnCreate: false\n" +
                "  properties:\n" +
                "    idProvider: \"0000000000000000fffffffffffffffe\"\n" +
                "    userId: \"00000000000000000000000000000003\"";
        load(json, "yml", false, false);
    }

    @Test
    void loadMalformedYaml() throws IOException {
        String json = NAME + ":\n" +
                "  policy: \"" + POLICY_PATH + "\"\n" +
                "  isOneNode: false\n" +
                "  jobType: \"Recurring\"\n" +
                "  jobStatus: \"Disabled\"\n" +
                "  cronExpression: \"* * * * * ?\"\n" +
                "  shouldExecuteOnCreate: false\n" +
                "  properties:\n" +
                "    - idProvider: \"0000000000000000fffffffffffffffe\"\n" +
                "    - userId: \"00000000000000000000000000000003\"";
        load(json, "yml", true, false);
    }

    @Test
    void loadMalformedJson() throws IOException {
        String json = "{\n" +
                "  \"" + NAME + "\" : {\n" +
                "    \"policy\" : \"" + POLICY_PATH + "\",\n" +
                "    \"isOneNode\" : false,\n" +
                "    \"jobType\" : Recurring,\n" +
                "    \"jobStatus\" : \"Disabled\",\n" +
                "    \"cronExpression\" : \"* * * * * ?\",\n" +
                "    \"shouldExecuteOnCreate\" : false,\n" +
                "    \"properties\" : {\n" +
                "      \"idProvider\" : \"0000000000000000fffffffffffffffe\",\n" +
                "      \"userId\" : \"00000000000000000000000000000003\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        load(json, "json", true, false);
    }

    private void load(String content, String fileTyoe, boolean expectException, boolean isOneTime) throws IOException {
        ScheduledTaskLoader loader = new ScheduledTaskLoader(jsonTools);
        final File configFolder = rootProjectDir.createDirectory("config");
        final File scheduledTaskFile = new File(configFolder, "scheduled-tasks." + fileTyoe);
        Files.touch(scheduledTaskFile);

        when(fileUtils.getInputStream(any(File.class))).thenReturn(new ByteArrayInputStream(content.getBytes()));

        final Bundle bundle = new Bundle();
        if (expectException) {
            assertThrows(JsonToolsException.class, () -> load(loader, bundle, rootProjectDir));
            return;
        } else {
            load(loader, bundle, rootProjectDir);
        }
        check(bundle, isOneTime);
    }

    private static void load(ScheduledTaskLoader loader, Bundle bundle, TemporaryFolder rootProjectDir) {
        loader.load(bundle, rootProjectDir.getRoot());
    }

    private static void check(Bundle bundle, boolean isOneTime) {
        assertFalse(bundle.getScheduledTasks().isEmpty(), "No scheduled tasks loaded");
        assertEquals(1, bundle.getScheduledTasks().size(), () -> "Expected 1 scheduled task, found " + bundle.getEncasses().size());
        assertNotNull(bundle.getScheduledTasks().get(NAME), NAME + " not found");

        ScheduledTask scheduledTask = bundle.getScheduledTasks().get(NAME);
        assertEquals(POLICY_PATH, scheduledTask.getPolicy());
        assertFalse(scheduledTask.getIsOneNode());
        if (isOneTime) {
            assertEquals("One time", scheduledTask.getJobType());
            assertEquals("Scheduled", scheduledTask.getJobStatus());
            assertEquals("2018-11-16T21:40:21.326Z", scheduledTask.getExecutionDate());
            assertNull(scheduledTask.getCronExpression());
        } else {
            assertEquals("Recurring", scheduledTask.getJobType());
            assertEquals("Disabled", scheduledTask.getJobStatus());
            assertEquals("* * * * * ?", scheduledTask.getCronExpression());
            assertNull(scheduledTask.getExecutionDate());
        }
        assertFalse(scheduledTask.getShouldExecuteOnCreate());
        assertEquals(2, scheduledTask.getProperties().size());
        assertTrue(scheduledTask.getProperties().containsKey("idProvider"));
        assertTrue(scheduledTask.getProperties().containsKey("userId"));
    }

}