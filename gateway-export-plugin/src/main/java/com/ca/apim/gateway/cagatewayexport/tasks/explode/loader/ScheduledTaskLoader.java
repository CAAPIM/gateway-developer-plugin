/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.loader;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity.ScheduledTaskEntity;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static com.ca.apim.gateway.cagatewayexport.tasks.explode.loader.EntityLoaderHelper.mapPropertiesElements;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

@Singleton
public class ScheduledTaskLoader implements EntityLoader<ScheduledTaskEntity>{
    @Override
    public ScheduledTaskEntity load(Element element) {
        final Element scheduledTask = getSingleChildElement(getSingleChildElement(element, RESOURCE), SCHEDULED_TASK);

        final String policyId = getSingleChildElementAttribute(scheduledTask, POLICY_REFERENCE, ATTRIBUTE_ID);
        final String name = getSingleChildElementTextContent(scheduledTask, NAME);
        final boolean isOneNode = toBoolean(getSingleChildElementTextContent(scheduledTask, ONE_NODE));
        final String jobType = getSingleChildElementTextContent(scheduledTask, JOB_TYPE);
        final String jobStatus = getSingleChildElementTextContent(scheduledTask, JOB_STATUS);
        final String executionDate = getSingleChildElementTextContent(scheduledTask, EXECUTION_DATE);
        final String cronExpression = getSingleChildElementTextContent(scheduledTask, CRON_EXPRESSION);
        final boolean shouldExecuteOnCreate = toBoolean(getSingleChildElementTextContent(scheduledTask, EXECUTE_ON_CREATE));
        final Map<String, Object> properties = mapPropertiesElements(getSingleChildElement(scheduledTask, PROPERTIES), PROPERTIES);

        return new ScheduledTaskEntity.Builder()
                .id(scheduledTask.getAttribute(ATTRIBUTE_ID))
                .policyId(policyId)
                .name(name)
                .oneNode(isOneNode)
                .jobType(jobType)
                .jobStatus(jobStatus)
                .executionDate(executionDate)
                .cronExpression(cronExpression)
                .shouldExecuteOnCreate(shouldExecuteOnCreate)
                .properties(properties)
                .build();
    }

    @Override
    public Class<ScheduledTaskEntity> entityClass() {
        return ScheduledTaskEntity.class;
    }
}
