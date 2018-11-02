/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public class ScheduledTask {
    private String policy;
    private boolean isOneNode;
    private String jobType;
    private String jobStatus;
    private String executionDate;
    private String cronExpression;
    private boolean shouldExecuteOnCreate;
    private Map<String, Object> properties;

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public boolean getIsOneNode() {
        return isOneNode;
    }

    public void setOneNode(boolean oneNode) {
        isOneNode = oneNode;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(String executionDate) {
        this.executionDate = executionDate;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean getShouldExecuteOnCreate() {
        return shouldExecuteOnCreate;
    }

    public void setShouldExecuteOnCreate(boolean shouldExecuteOnCreate) {
        this.shouldExecuteOnCreate = shouldExecuteOnCreate;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
