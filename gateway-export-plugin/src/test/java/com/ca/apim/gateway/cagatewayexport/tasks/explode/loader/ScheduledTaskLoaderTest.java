/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;


class ScheduledTaskLoaderTest {
//    private ScheduledTaskLoader loader = new ScheduledTaskLoader();
//
//    @Test
//    void loadOneTimeScheduledTask() {
//        final ScheduledTaskEntity entity = loader.load(createScheduledTaskXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), true));
//
//        assertNotNull(entity);
//        assertEquals("id", entity.getId());
//        assertEquals("Test", entity.getName());
//        assertFalse(entity.getIsOneNode());
//        assertEquals("One time", entity.getJobType());
//        assertEquals("someDate", entity.getExecutionDate());
//        assertNull(entity.getCronExpression());
//        assertEquals("Scheduled", entity.getJobStatus());
//        assertFalse(entity.getShouldExecuteOnCreate());
//        assertEquals("testValue", entity.getProperties().get("testProp"));
//    }
//
//    @Test
//    void loadRecurringScheduledTask() {
//        final ScheduledTaskEntity entity = loader.load(createScheduledTaskXml(DocumentTools.INSTANCE.getDocumentBuilder().newDocument(), false));
//
//        assertNotNull(entity);
//        assertEquals("id", entity.getId());
//        assertEquals("Test", entity.getName());
//        assertFalse(entity.getIsOneNode());
//        assertEquals("Recurring", entity.getJobType());
//        assertEquals("Some Expression", entity.getCronExpression());
//        assertNull(entity.getExecutionDate());
//        assertEquals("Scheduled", entity.getJobStatus());
//        assertFalse(entity.getShouldExecuteOnCreate());
//        assertEquals("testValue", entity.getProperties().get("testProp"));
//    }
//
//    @Test
//    void entityClass() {
//        assertEquals(ScheduledTaskEntity.class, loader.entityClass());
//    }
//
//    private static Element createScheduledTaskXml(Document document, boolean isOneTime) {
//        Element scheduledTaskElem = createElementWithAttributesAndChildren(
//                document,
//                SCHEDULED_TASK,
//                ImmutableMap.of(ATTRIBUTE_ID, "id"),
//                createElementWithTextContent(document, NAME, "Test"),
//                createElementWithTextContent(document, ONE_NODE, false),
//                isOneTime ? createElementWithTextContent(document, JOB_TYPE, "One time") : createElementWithTextContent(document, JOB_TYPE, "Recurring"),
//                createElementWithTextContent(document, JOB_STATUS, "Scheduled"),
//                createElementWithTextContent(document, EXECUTE_ON_CREATE, false),
//                isOneTime? createElementWithTextContent(document, EXECUTION_DATE, "someDate") :  createElementWithTextContent(document, CRON_EXPRESSION, "Some Expression")
//        );
//        buildAndAppendPropertiesElement(ImmutableMap.of("testProp", "testValue"), document, scheduledTaskElem);
//
//        return createElementWithChildren(
//                document,
//                ITEM,
//                createElementWithChildren(
//                        document,
//                        RESOURCE,
//                        scheduledTaskElem
//                )
//        );
//    }
}