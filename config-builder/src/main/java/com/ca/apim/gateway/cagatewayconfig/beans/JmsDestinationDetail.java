/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import static java.util.Arrays.stream;

public class JmsDestinationDetail {

    private ReplyType replyType;
    private String replyToQueueName;
    private boolean useRequestCorrelationId;

    JmsDestinationDetail() {
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
}
