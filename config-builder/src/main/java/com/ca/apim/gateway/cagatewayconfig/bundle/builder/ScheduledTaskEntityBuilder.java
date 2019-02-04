/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.builder;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import com.ca.apim.gateway.cagatewayconfig.beans.ScheduledTask;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.bundle.builder.EntityBuilder.BundleType.ENVIRONMENT;
import static com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes.SCHEDULED_TASK_TYPE;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.buildAndAppendPropertiesElement;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttribute;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithAttributesAndChildren;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.createElementWithTextContent;
import static java.util.Collections.emptyList;

@Singleton
public class ScheduledTaskEntityBuilder implements EntityBuilder {
    private static final Integer ORDER = 1400;
    private final IdGenerator idGenerator;

    @Inject
    ScheduledTaskEntityBuilder(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public List<Entity> build(Bundle bundle, BundleType bundleType, Document document) {
        // no sched task has to be added to environment bundle
        if (bundleType == ENVIRONMENT) {
            return emptyList();
        }

        return bundle.getScheduledTasks().entrySet().stream().map(scheduledTaskEntry ->
                buildScheduledTaskEntity(bundle, scheduledTaskEntry.getKey(), scheduledTaskEntry.getValue(), document)
        ).collect(Collectors.toList());
    }

    private Entity buildScheduledTaskEntity(Bundle bundle, String name, ScheduledTask scheduledTask, Document document) {
        Policy policy = bundle.getPolicies().get(scheduledTask.getPolicy());
        if (policy == null) {
            throw new EntityBuilderException("Could not find policy for encass. Policy Path: " + scheduledTask.getPolicy());
        }
        final String id = idGenerator.generate();
        Element schedTaskElement = createElementWithAttributesAndChildren(
                document,
                SCHEDULED_TASK,
                ImmutableMap.of(ATTRIBUTE_ID, id),
                createElementWithTextContent(document, NAME, name),
                createElementWithAttribute(document, POLICY_REFERENCE, ATTRIBUTE_ID, policy.getId()),
                createElementWithTextContent(document, ONE_NODE, scheduledTask.getIsOneNode()),
                createElementWithTextContent(document, JOB_TYPE, scheduledTask.getJobType()),
                createElementWithTextContent(document, JOB_STATUS, scheduledTask.getJobStatus()),
                scheduledTask.getJobType().equals("One time") ?
                        createElementWithTextContent(document, EXECUTION_DATE, scheduledTask.getExecutionDate()) :
                        createElementWithTextContent(document, CRON_EXPRESSION, scheduledTask.getCronExpression()),
                createElementWithTextContent(document, EXECUTE_ON_CREATE, scheduledTask.getShouldExecuteOnCreate())
        );
        if (scheduledTask.getProperties() != null) {
            buildAndAppendPropertiesElement(scheduledTask.getProperties(), document, schedTaskElement);
        }

        return EntityBuilderHelper.getEntityWithNameMapping(SCHEDULED_TASK_TYPE, name, id, schedTaskElement);
    }

    @Override
    public @NotNull Integer getOrder() {
        return ORDER;
    }
}
