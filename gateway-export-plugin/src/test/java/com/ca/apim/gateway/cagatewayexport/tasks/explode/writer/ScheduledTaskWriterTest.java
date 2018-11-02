/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ScheduledTask;
import com.ca.apim.gateway.cagatewayconfig.util.file.DocumentFileUtils;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ScheduledTaskEntity;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScheduledTaskWriterTest {
    private ScheduledTaskWriter writer = new ScheduledTaskWriter(DocumentFileUtils.INSTANCE, JsonTools.INSTANCE);


    @Test
    void testGetBean() {
        ScheduledTaskEntity entity = new ScheduledTaskEntity.Builder()
                .name("test")
                .jobType("Recurring")
                .jobStatus("scheduled")
                .cronExpression("something")
                .oneNode(false)
                .shouldExecuteOnCreate(false)
                .properties(ImmutableMap.of("prop1", "value1"))
                .build();
        entity.setPolicyPath("somepath");

        final ScheduledTask bean = writer.getScheduledTaskBean(entity);
        assertNotNull(bean);
        assertEquals(entity.getJobType(), bean.getJobType());
        assertEquals(entity.getJobStatus(), bean.getJobStatus());
        assertEquals(entity.getCronExpression(), bean.getCronExpression());
        assertEquals(entity.getPolicyPath(), bean.getPolicy());
        assertEquals(entity.getExecutionDate(), bean.getExecutionDate());
        assertEquals(entity.getIsOneNode(), bean.getIsOneNode());
        assertEquals(entity.getShouldExecuteOnCreate(), bean.getShouldExecuteOnCreate());
        assertNull(entity.getExecutionDate());
        assertNotNull(bean.getProperties());
        assertFalse(bean.getProperties().isEmpty());
        assertEquals(1, bean.getProperties().size());
        assertEquals("value1", bean.getProperties().get("prop1"));
    }
}