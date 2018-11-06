/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;


import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.ScheduledTask;
import com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.util.List;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.junit.jupiter.api.Assertions.*;

class ScheduledTaskEntityBuilderTest {
    private static final IdGenerator ID_GENERATOR = new IdGenerator();
    private static final String TEST_SCHEDULED_TASK = "TestEncass";
    private static final String TEST_POLICY_PATH= "test/policy.xml";
    private static final String TEST_POLICY_ID = "PolicyID";
    private static final String RECURRING_JOB_TYPE = "Recurring";
    private static final String ONE_TIME_JOB = "One time";
    private static final String SAMPLE_EXECUTION_DATE = "2020-11-16T21:40:21.326Z";
    private static final String SCHEDULED_JOB_STATUS = "Scheduled";
    private static final String PER_HOUR_CRON_EXPRESSION = "0 0 */1 * * ?";

    @Test
    void buildFromEmptyBundle_noScheduledTask() {
        ScheduledTaskEntityBuilder builder = new ScheduledTaskEntityBuilder(ID_GENERATOR);
        final List<Entity> entities = builder.build(new Bundle(), BundleType.DEPLOYMENT, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertTrue(entities.isEmpty());
    }

    @Test
    void buildWithNoPolicy() {
        final Bundle bundle = new Bundle();
        assertThrows(EntityBuilderException.class, () -> buildBundleWithScheduledTask(bundle, BundleType.DEPLOYMENT, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_SCHEDULED_TASK, false));
    }

    @Test
    void buildDeploymentWithOneTimeScheduledTask() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_POLICY_PATH);
        buildBundleWithScheduledTask(bundle, BundleType.DEPLOYMENT, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_SCHEDULED_TASK, true);
    }

    @Test
    void buildEnvironmentWithOneTimeScheduledTask() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_POLICY_PATH);
        buildBundleWithScheduledTask(bundle, BundleType.ENVIRONMENT, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_SCHEDULED_TASK, true);
    }

    @Test
    void buildDeploymentWithRecurringScheduledTask() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_POLICY_PATH);
        buildBundleWithScheduledTask(bundle, BundleType.DEPLOYMENT, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_SCHEDULED_TASK, false);
    }

    @Test
    void buildEnvironmentWithRecurringScheduledTask() {
        final Bundle bundle = new Bundle();
        putPolicy(bundle, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_POLICY_PATH);
        buildBundleWithScheduledTask(bundle, BundleType.ENVIRONMENT, TEST_POLICY_PATH, TEST_POLICY_ID, TEST_SCHEDULED_TASK, false);
    }

    private static void buildBundleWithScheduledTask(Bundle bundle, BundleType deployment, String policyPath, String policyId, String scheduledTaskName, boolean isOneTime) {
        ScheduledTaskEntityBuilder builder = new ScheduledTaskEntityBuilder(ID_GENERATOR);
        ScheduledTask scheduledTask = buildTestScheduledTask(policyPath, isOneTime);
        bundle.putAllScheduledTasks(ImmutableMap.of(scheduledTaskName, scheduledTask));

        final List<Entity> entities = builder.build(bundle, deployment, DocumentTools.INSTANCE.getDocumentBuilder().newDocument());

        assertFalse(entities.isEmpty());
        assertEquals(1, entities.size());

        Entity entity = entities.get(0);
        assertEquals(scheduledTaskName, entity.getName());
        assertNotNull(entity.getId());
        assertNotNull(entity.getXml());
        assertEquals(EntityTypes.SCHEDULED_TASK_TYPE, entity.getType());

        Element xml = entity.getXml();
        assertEquals(SCHEDULED_TASK, xml.getTagName());
        assertNotNull(getSingleChildElement(xml, NAME));
        assertEquals(scheduledTaskName, getSingleChildElementTextContent(xml, NAME));
        assertNotNull(getSingleChildElement(xml, POLICY_REFERENCE));
        assertEquals(policyId, getSingleChildElement(xml, POLICY_REFERENCE).getAttribute(ATTRIBUTE_ID));
        assertEquals("true", getSingleChildElementTextContent(xml, ONE_NODE));
        if (isOneTime) {
            assertEquals(ONE_TIME_JOB, getSingleChildElementTextContent(xml, JOB_TYPE));
            assertEquals(SAMPLE_EXECUTION_DATE, getSingleChildElementTextContent(xml, EXECUTION_DATE));
            assertNull(getSingleChildElement(xml, CRON_EXPRESSION, true));
        } else {
            assertEquals(RECURRING_JOB_TYPE, getSingleChildElementTextContent(xml, JOB_TYPE));
            assertEquals(PER_HOUR_CRON_EXPRESSION, getSingleChildElementTextContent(xml, CRON_EXPRESSION));
            assertNull(getSingleChildElement(xml, EXECUTION_DATE, true));
        }
        assertEquals(SCHEDULED_JOB_STATUS, getSingleChildElementTextContent(xml, JOB_STATUS));
        assertTrue(mapPropertiesElements(getSingleChildElement(xml, PROPERTIES), PROPERTIES).containsKey("test"));
        
    }

    private static ScheduledTask buildTestScheduledTask(String policyPath, boolean isOneTime) {
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.setPolicy(policyPath);
        scheduledTask.setJobStatus(SCHEDULED_JOB_STATUS);
        scheduledTask.setOneNode(true);
        if (isOneTime) {
            scheduledTask.setJobType(ONE_TIME_JOB);
            scheduledTask.setExecutionDate(SAMPLE_EXECUTION_DATE);
        } else {
            scheduledTask.setJobType(RECURRING_JOB_TYPE);
            scheduledTask.setCronExpression(PER_HOUR_CRON_EXPRESSION);
        }
        scheduledTask.setShouldExecuteOnCreate(false);
        scheduledTask.setProperties(ImmutableMap.of("test","testValue"));
        return scheduledTask;
    }

    private static void putPolicy(Bundle bundle, String name, String id, String path) {
        Policy policy = new Policy();
        policy.setName(name);
        policy.setId(id);
        bundle.getPolicies().put(path, policy);
    }

}