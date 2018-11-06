/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;


import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ScheduledTask;
import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentTools;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class ScheduledTaskLoaderTest {
    private ScheduledTaskLoader loader = new ScheduledTaskLoader();

    @Test
    void loadOneTimeScheduledTask() {
        Bundle bundle = new Bundle();
        loader.load(bundle, createScheduledTaskXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true));
        final ScheduledTask entity = bundle.getScheduledTasks().get("Test");

        assertNotNull(entity);
        assertEquals("id", entity.getId());
        assertEquals("Test", entity.getName());
        assertFalse(entity.getIsOneNode());
        assertEquals("One time", entity.getJobType());
        assertEquals("someDate", entity.getExecutionDate());
        assertNull(entity.getCronExpression());
        assertEquals("Scheduled", entity.getJobStatus());
        assertFalse(entity.getShouldExecuteOnCreate());
        assertEquals("testValue", entity.getProperties().get("testProp"));
    }

    @Test
    void loadRecurringScheduledTask() {
        Bundle bundle = new Bundle();
        loader.load(bundle, createScheduledTaskXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), false));
        final ScheduledTask entity = bundle.getScheduledTasks().get("Test");

        assertNotNull(entity);
        assertEquals("id", entity.getId());
        assertEquals("Test", entity.getName());
        assertFalse(entity.getIsOneNode());
        assertEquals("Recurring", entity.getJobType());
        assertEquals("Some Expression", entity.getCronExpression());
        assertNull(entity.getExecutionDate());
        assertEquals("Scheduled", entity.getJobStatus());
        assertFalse(entity.getShouldExecuteOnCreate());
        assertEquals("testValue", entity.getProperties().get("testProp"));
    }

    private static Element createScheduledTaskXml(Document document, boolean isOneTime) {
        Element scheduledTaskElem = createElementWithAttributesAndChildren(
                document,
                SCHEDULED_TASK,
                ImmutableMap.of(ATTRIBUTE_ID, "id"),
                createElementWithTextContent(document, NAME, "Test"),
                createElementWithTextContent(document, ONE_NODE, false),
                isOneTime ? createElementWithTextContent(document, JOB_TYPE, "One time") : createElementWithTextContent(document, JOB_TYPE, "Recurring"),
                createElementWithTextContent(document, JOB_STATUS, "Scheduled"),
                createElementWithTextContent(document, EXECUTE_ON_CREATE, false),
                isOneTime? createElementWithTextContent(document, EXECUTION_DATE, "someDate") :  createElementWithTextContent(document, CRON_EXPRESSION, "Some Expression")
        );
        buildAndAppendPropertiesElement(ImmutableMap.of("testProp", "testValue"), document, scheduledTaskElem);

        return createElementWithChildren(
                document,
                ITEM,
                createElementWithChildren(
                        document,
                        RESOURCE,
                        scheduledTaskElem
                )
        );
    }
}