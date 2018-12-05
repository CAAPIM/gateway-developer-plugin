/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.ScheduledTask;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.*;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

@Singleton
public class ScheduledTaskLoader implements BundleEntityLoader {
    @Override
    public void load(Bundle bundle, Element element) {
        final Element scheduledTask = getSingleChildElement(getSingleChildElement(element, RESOURCE), SCHEDULED_TASK);

        final String policyId = getSingleChildElementAttribute(scheduledTask, POLICY_REFERENCE, ATTRIBUTE_ID);
        final String name = getSingleChildElementTextContent(scheduledTask, NAME);
        final boolean isOneNode = toBoolean(getSingleChildElementTextContent(scheduledTask, ONE_NODE));
        final String jobType = getSingleChildElementTextContent(scheduledTask, JOB_TYPE);
        final String jobStatus = getSingleChildElementTextContent(scheduledTask, JOB_STATUS);
        final String executionDate = getSingleChildElementTextContent(scheduledTask, EXECUTION_DATE);
        final String cronExpression = getSingleChildElementTextContent(scheduledTask, CRON_EXPRESSION);
        final boolean shouldExecuteOnCreate = toBoolean(getSingleChildElementTextContent(scheduledTask, EXECUTE_ON_CREATE));
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(scheduledTask, PROPERTIES, true), PROPERTIES);

        ScheduledTask task = new ScheduledTask();
        task.setId(scheduledTask.getAttribute(ATTRIBUTE_ID));
        task.setPolicy(policyId);
        task.setName(name);
        task.setOneNode(isOneNode);
        task.setJobType(jobType);
        task.setJobStatus(jobStatus);
        task.setExecutionDate(executionDate);
        task.setCronExpression(cronExpression);
        task.setShouldExecuteOnCreate(shouldExecuteOnCreate);
        task.setProperties(properties);

        bundle.getScheduledTasks().put(name, task);
    }

    @Override
    public String getEntityType() {
        return EntityTypes.SCHEDULED_TASK_TYPE;
    }
}
