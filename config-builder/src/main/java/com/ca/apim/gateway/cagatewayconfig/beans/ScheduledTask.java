/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.inject.Named;
import java.util.Map;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Named("SCHEDULED_TASK")
@ConfigurationFile(name = "scheduled-tasks", type = JSON_YAML)
public class ScheduledTask extends GatewayEntity {
    private String policy;
    private boolean isOneNode;
    private String jobType;
    private String jobStatus;
    private String executionDate;
    private String cronExpression;
    private boolean shouldExecuteOnCreate;
    private Map<String, Object> properties;

    public ScheduledTask() {}

    private ScheduledTask(Builder builder) {
        setName(builder.name);
        setId(builder.id);
        setPolicy(builder.policyId);
        this.isOneNode = builder.isOneNode;
        this.jobType = builder.jobType;
        this.jobStatus = builder.jobStatus;
        this.executionDate = builder.executionDate;
        this.cronExpression = builder.cronExpression;
        this.shouldExecuteOnCreate = builder.shouldExecuteOnCreate;
        this.properties = builder.properties;
    }

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

    public static class Builder {
        private String name;
        private String id;
        private String policyId;
        private boolean isOneNode;
        private String jobType;
        private String jobStatus;
        private String executionDate;
        private String cronExpression;
        private boolean shouldExecuteOnCreate;
        Map<String, Object> properties;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder policyId(String policyId) {
            this.policyId = policyId;
            return this;
        }

        public Builder oneNode(boolean oneNode) {
            isOneNode = oneNode;
            return this;
        }

        public Builder jobType(String jobType) {
            this.jobType = jobType;
            return this;
        }

        public Builder jobStatus(String jobStatus) {
            this.jobStatus = jobStatus;
            return this;
        }

        public Builder executionDate(String executionDate) {
            this.executionDate = executionDate;
            return this;
        }

        public Builder cronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }

        public Builder shouldExecuteOnCreate(boolean shouldExecuteOnCreate) {
            this.shouldExecuteOnCreate = shouldExecuteOnCreate;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public ScheduledTask build() {
            return new ScheduledTask(this);
        }
    }
}
