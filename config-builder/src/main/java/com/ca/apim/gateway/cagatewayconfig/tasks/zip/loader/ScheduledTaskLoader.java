/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.ScheduledTask;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class ScheduledTaskLoader extends EntityLoaderBase<ScheduledTask> {
    private static final String FILE_NAME = "scheduled-tasks";

    @Inject
    ScheduledTaskLoader(final JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    protected Class<ScheduledTask> getBeanClass() {
        return ScheduledTask.class;
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }

    @Override
    protected void putToBundle(Bundle bundle, @NotNull Map<String, ScheduledTask> entitiesMap) {
        bundle.putAllScheduledTasks(entitiesMap);
    }

    @Override
    public String getEntityType() {
        return "SCHEDULED_TASK";
    }
}
