/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.fasterxml.jackson.annotation.JsonTypeName;

import static java.util.Arrays.stream;

public class OutboundJmsDestinationDetail {
    
    private boolean isTemplate;
    private ReplyType replyType;
    private String replyToQueueName;
    private boolean useRequestCorrelationId;
    private MessageFormat messageFormat;
    private PoolingType poolingType;
    private SessionPoolingSettings sessionPoolingSettings;
    private ConnectionPoolingSettings connectionPoolingSettings;

    public OutboundJmsDestinationDetail() {
    }
    
    public OutboundJmsDestinationDetail(
            boolean isTemplate,
            ReplyType replyType,
            String replyToQueueName,
            boolean useRequestCorrelationId,
            MessageFormat messageFormat,
            PoolingType poolingType,
            SessionPoolingSettings sessionPoolingSettings,
            ConnectionPoolingSettings connectionPoolingSettings) {
        this.isTemplate = isTemplate;
        this.replyType = replyType;
        this.replyToQueueName = replyToQueueName;
        this.useRequestCorrelationId = useRequestCorrelationId;
        this.messageFormat = messageFormat;
        this.poolingType = poolingType;
        this.sessionPoolingSettings = sessionPoolingSettings;
        this.connectionPoolingSettings = connectionPoolingSettings;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public ReplyType getReplyType() {
        return replyType;
    }

    public void setReplyType(ReplyType replyType) {
        this.replyType = replyType;
    }

    public String getReplyToQueueName() {
        return replyToQueueName;
    }

    public void setReplyToQueueName(String replyToQueueName) {
        this.replyToQueueName = replyToQueueName;
    }

    public boolean useRequestCorrelationId() {
        return useRequestCorrelationId;
    }

    public void setUseRequestCorrelationId(boolean useRequestCorrelationId) {
        this.useRequestCorrelationId = useRequestCorrelationId;
    }

    public MessageFormat getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(MessageFormat messageFormat) {
        this.messageFormat = messageFormat;
    }

    public PoolingType getPoolingType() {
        return poolingType;
    }

    public void setPoolingType(PoolingType poolingType) {
        this.poolingType = poolingType;
    }

    public SessionPoolingSettings getSessionPoolingSettings() {
        return sessionPoolingSettings;
    }

    public void setSessionPoolingSettings(SessionPoolingSettings sessionPoolingSettings) {
        this.sessionPoolingSettings = sessionPoolingSettings;
    }

    public ConnectionPoolingSettings getConnectionPoolingSettings() {
        return connectionPoolingSettings;
    }

    public void setConnectionPoolingSettings(ConnectionPoolingSettings connectionPoolingSettings) {
        this.connectionPoolingSettings = connectionPoolingSettings;
    }

    public static class SessionPoolingSettings {
        private Integer size;
        private Integer maxIdle;
        private Integer maxWaitMs;

        public SessionPoolingSettings(Integer size, Integer maxIdle, Integer maxWaitMs) {
            this.size = size;
            this.maxIdle = maxIdle;
            this.maxWaitMs = maxWaitMs;
        }
        
        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Integer getMaxIdle() {
            return maxIdle;
        }

        public void setMaxIdle(Integer maxIdle) {
            this.maxIdle = maxIdle;
        }

        public Integer getMaxWaitMs() {
            return maxWaitMs;
        }

        public void setMaxWaitMs(Integer maxWaitMs) {
            this.maxWaitMs = maxWaitMs;
        }
    }
    
    public static class ConnectionPoolingSettings {
        private Integer size;
        private Integer minIdle;
        private Integer maxWaitMs;

        public ConnectionPoolingSettings() {
        }
        
        public ConnectionPoolingSettings(Integer size, Integer minIdle, Integer maxWaitMs) {
            this.size = size;
            this.minIdle = minIdle;
            this.maxWaitMs = maxWaitMs;
        }
        
        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Integer getMinIdle() {
            return minIdle;
        }

        public void setMinIdle(Integer minIdle) {
            this.minIdle = minIdle;
        }

        public Integer getMaxWaitMs() {
            return maxWaitMs;
        }

        public void setMaxWaitMs(Integer maxWaitMs) {
            this.maxWaitMs = maxWaitMs;
        }
    }
    
    public enum ReplyType {
        AUTOMATIC("AUTOMATIC"),
        NO_REPLY("NO_REPLY"),
        SPECIFIED_QUEUE("REPLY_TO_OTHER");
        
        private String type;

        ReplyType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public static ReplyType fromType(String type) {
            return stream(values()).filter(c -> c.type.equals(type)).findFirst().orElse(null);
        }
    }

    public enum MessageFormat {
        AUTOMATIC("AUTOMATIC"),
        BYTES("ALWAYS_BINARY"),
        TEXT("ALWAYS_TEXT");

        private String format;

        MessageFormat(String format) {
            this.format = format;
        }

        public String getType() {
            return format;
        }

        public static MessageFormat fromFormat(String format) {
            return stream(values()).filter(c -> c.format.equals(format)).findFirst().orElse(null);
        }
    }

    public enum PoolingType {
        SESSION,
        CONNECTION
    }
}
