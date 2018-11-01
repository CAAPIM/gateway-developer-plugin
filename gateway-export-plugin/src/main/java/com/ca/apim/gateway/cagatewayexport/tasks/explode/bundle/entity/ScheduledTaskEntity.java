/*
 * Copyright (c) 2018. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;

import javax.inject.Named;
import java.util.Map;

@Named("SCHEDULED_TASK")
public class ScheduledTaskEntity implements Entity {
    private final String name;
    private final String id;
    private final String policyId;
    private final boolean isOneNode;
    private final String jobType;
    private final String jobStatus;
    private final String executionDate;
    private final String cronExpression;
    private final boolean shouldExecuteOnCreate;
    private final Map<String, Object> properties;
    private String policyPath;

    private ScheduledTaskEntity(Builder builder) {
        this.name = builder.name;
        this.id = builder.id;
        this.policyId = builder.policyId;
        this.isOneNode = builder.isOneNode;
        this.jobType = builder.jobType;
        this.jobStatus = builder.jobStatus;
        this.executionDate = builder.executionDate;
        this.cronExpression = builder.cronExpression;
        this.shouldExecuteOnCreate = builder.shouldExecuteOnCreate;
        this.properties = builder.properties;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getPolicyId() {
        return policyId;
    }

    public boolean isOneNode() {
        return isOneNode;
    }

    public String getJobType() {
        return jobType;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public boolean isShouldExecuteOnCreate() {
        return shouldExecuteOnCreate;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getPolicyPath() {
        return policyPath;
    }

    public ScheduledTaskEntity setPolicyPath(String policyPath) {
        this.policyPath = policyPath;
        return this;
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

        public ScheduledTaskEntity build() {
            return new ScheduledTaskEntity(this);
        }
    }
}
