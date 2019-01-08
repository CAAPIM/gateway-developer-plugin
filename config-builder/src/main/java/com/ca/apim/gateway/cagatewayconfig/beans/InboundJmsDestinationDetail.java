/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import static java.util.Arrays.stream;

public class InboundJmsDestinationDetail extends JmsDestinationDetail {
    
    private AcknowledgeType acknowledgeType;
    private ServiceResolutionSettings serviceResolutionSettings;
    private String failureQueueName;
    private Integer numOfConsumerConnections;
    private Integer maxMessageSizeBytes = -1; // -1 = Do not override,  0 = Unlimited.
    
    public InboundJmsDestinationDetail() {
        super();
    }

    public AcknowledgeType getAcknowledgeType() {
        return acknowledgeType;
    }

    public void setAcknowledgeType(AcknowledgeType acknowledgeType) {
        this.acknowledgeType = acknowledgeType;
    }

    public ServiceResolutionSettings getServiceResolutionSettings() {
        return serviceResolutionSettings;
    }

    public void setServiceResolutionSettings(ServiceResolutionSettings serviceResolutionSettings) {
        this.serviceResolutionSettings = serviceResolutionSettings;
    }

    public String getFailureQueueName() {
        return failureQueueName;
    }

    public void setFailureQueueName(String failureQueueName) {
        this.failureQueueName = failureQueueName;
    }
    
    public Integer getNumOfConsumerConnections() {
        return numOfConsumerConnections;
    }

    public void setNumOfConsumerConnections(Integer numOfConsumerConnections) {
        this.numOfConsumerConnections = numOfConsumerConnections;
    }

    public Integer getMaxMessageSizeBytes() {
        return maxMessageSizeBytes;
    }

    public void setMaxMessageSizeBytes(Integer maxMessageSizeBytes) {
        this.maxMessageSizeBytes = maxMessageSizeBytes;
    }

    public static class ServiceResolutionSettings {
        private String serviceRef;
        private String soapActionMessagePropertyName;
        private ContentTypeSource contentTypeSource;
        private String contentType;
        
        public ServiceResolutionSettings() {
            // default constructor.
        }
        
        public String getServiceRef() {
            return serviceRef;
        }

        public void setServiceRef(String serviceRef) {
            this.serviceRef = serviceRef;
        }

        public String getSoapActionMessagePropertyName() {
            return soapActionMessagePropertyName;
        }

        public void setSoapActionMessagePropertyName(String soapActionMessagePropertyName) {
            this.soapActionMessagePropertyName = soapActionMessagePropertyName;
        }

        public ContentTypeSource getContentTypeSource() {
            return contentTypeSource;
        }

        public void setContentTypeSource(ContentTypeSource contentTypeSource) {
            this.contentTypeSource = contentTypeSource;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }
    
    public enum AcknowledgeType {
        ON_TAKE("AUTOMATIC"),
        ON_COMPLETION("ON_COMPLETION");

        private String type;

        AcknowledgeType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public static AcknowledgeType fromType(String type) {
            return stream(values()).filter(c -> c.type.equals(type)).findFirst().orElse(null);
        }
    }
    
    public enum ContentTypeSource {
        NONE("NONE"),
        FREE_FORM("FREE_FORM"),
        JMS_PROPERTY("JMS_PROPERTY");

        private String source;

        ContentTypeSource(String source) {
            this.source = source;
        }

        public String getSource() {
            return source;
        }

        public static ContentTypeSource fromType(String source) {
            return stream(values()).filter(c -> c.source.equals(source)).findFirst().orElse(null);
        }
    }
}