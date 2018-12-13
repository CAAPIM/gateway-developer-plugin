/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.bundle.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.AcknowledgeType;
import com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.ContentTypeSource;
import com.ca.apim.gateway.cagatewayconfig.beans.InboundJmsDestinationDetail.ServiceResolutionSettings;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestination;
import com.ca.apim.gateway.cagatewayconfig.beans.JmsDestinationDetail.ReplyType;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.ConnectionPoolingSettings;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.MessageFormat;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.PoolingType;
import com.ca.apim.gateway.cagatewayconfig.beans.OutboundJmsDestinationDetail.SessionPoolingSettings;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import org.w3c.dom.Element;

import javax.inject.Singleton;

import java.util.Map;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BuilderUtils.mapPropertiesElements;
import static com.ca.apim.gateway.cagatewayconfig.util.gateway.BundleElementNames.*;
import static com.ca.apim.gateway.cagatewayconfig.util.properties.PropertyConstants.*;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElement;
import static com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentUtils.getSingleChildElementTextContent;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

@Singleton
public class JmsDestinationLoader implements BundleEntityLoader {
    
    @Override
    public void load(Bundle bundle, Element element) {
        
        final Element jmsDestinationEle = getSingleChildElement(getSingleChildElement(element, RESOURCE), JMS_DESTINATION);
        final String id = jmsDestinationEle.getAttribute(ATTRIBUTE_ID);

        final Element jmsDestinationDetailEle = getSingleChildElement(jmsDestinationEle, JMS_DESTINATION_DETAIL);
        final String name = getSingleChildElementTextContent(jmsDestinationDetailEle, NAME);
        final boolean isInbound = toBoolean(getSingleChildElementTextContent(jmsDestinationDetailEle, INBOUND));
        final boolean isEnabled = toBoolean(getSingleChildElementTextContent(jmsDestinationDetailEle, ENABLED));
        final boolean isTemplate = toBoolean(getSingleChildElementTextContent(jmsDestinationDetailEle, TEMPLATE));
        final Map<String, Object> jmsDestinationDetailProps = mapPropertiesElements(getSingleChildElement(jmsDestinationDetailEle, PROPERTIES, false), PROPERTIES);
        
        final Element jmsConnectionEle = getSingleChildElement(jmsDestinationEle, JMS_CONNECTION);
        final String providerType = getSingleChildElementTextContent(jmsConnectionEle, JMS_PROVIDER_TYPE);

        final Map<String, Object> jmsConnectionProps = mapPropertiesElements(getSingleChildElement(jmsConnectionEle, PROPERTIES, false), PROPERTIES);
        final String initialContextFactoryClassName = (String) jmsConnectionProps.remove(JNDI_INITIAL_CONTEXT_FACTORY_CLASSNAME);
        final String jndiUrl = (String) jmsConnectionProps.remove(JNDI_PROVIDER_URL);

        final Map<String, Object> contextPropertiesTemplateProps = mapPropertiesElements(getSingleChildElement(jmsConnectionEle, CONTEXT_PROPERTIES_TEMPLATE, false), CONTEXT_PROPERTIES_TEMPLATE);
        final String jndiUsername = (String) contextPropertiesTemplateProps.remove(JNDI_USERNAME);
        final String jndiPassword = (String) contextPropertiesTemplateProps.remove(JNDI_PASSWORD);
        
        final Map<String, Object> jndiProperties = contextPropertiesTemplateProps.entrySet().stream()
                .filter(map -> 
                        !map.getKey().startsWith("com.l7tech.server.jms.prop.") && 
                        !"com.l7tech.server.jms.soapAction.msgPropName".equals(map.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final String destinationType = (String) jmsDestinationDetailProps.remove(DESTINATION_TYPE);
        final String connectionFactoryName = (String) jmsConnectionProps.remove(CONNECTION_FACTORY_NAME);
        final String destinationName = getSingleChildElementTextContent(jmsDestinationDetailEle, JMS_DESTINATION_NAME);
        final String destinationUsername = (String) jmsDestinationDetailProps.remove(PROPERTY_USERNAME);
        final String destinationPassword = (String) jmsDestinationDetailProps.remove(PROPERTY_PASSWORD);
        
        JmsDestination jmsDestination = new JmsDestination();
        jmsDestination.setId(id);
        jmsDestination.setName(name);
        jmsDestination.setProviderType(providerType);
        jmsDestination.setInitialContextFactoryClassName(initialContextFactoryClassName);
        jmsDestination.setJndiUrl(jndiUrl);
        jmsDestination.setJndiUsername(jndiUsername);
        jmsDestination.setJndiPassword(jndiPassword);
        jmsDestination.setJndiProperties(jndiProperties);
        
        jmsDestination.setDestinationType(JmsDestination.DestinationType.fromType(destinationType));
        jmsDestination.setConnectionFactoryName(connectionFactoryName);
        jmsDestination.setDestinationName(destinationName);
        jmsDestination.setDestinationUsername(destinationUsername);
        jmsDestination.setDestinationPassword(destinationPassword);

        if (isInbound) {
            jmsDestination.setInboundDetail(this.loadInboundDetail(isEnabled, jmsDestinationDetailProps, contextPropertiesTemplateProps));
        } else {
            jmsDestination.setOutboundDetail(this.loadOutboundDetail(isTemplate, jmsDestinationDetailProps, contextPropertiesTemplateProps));
        }
        
        if (!contextPropertiesTemplateProps.isEmpty()) {
            // Any remaining items in contextPropertiesTemplateProps is copied to 
            // additional properties.
            // Items remaining in contextPropertiesTemplateProps should be settings for
            // none generic JMS providers.
            jmsDestination.setAdditionalProperties(contextPropertiesTemplateProps);
        }
        
        bundle.getJmsDestinations().put(name, jmsDestination);
    }

    private InboundJmsDestinationDetail loadInboundDetail(
            boolean isEnabled,
            Map<String, Object> jmsDestinationDetailProps,
            Map<String, Object> contextPropertiesTemplateProps) {
        final ServiceResolutionSettings serviceResolutionSettings = new ServiceResolutionSettings();
        serviceResolutionSettings.setServiceRef((String) contextPropertiesTemplateProps.remove(HARDWIRED_SERVICE_ID));
        serviceResolutionSettings.setSoapActionMessagePropertyName((String) contextPropertiesTemplateProps.remove(SOAP_ACTION_MSG_PROP_NAME));
        serviceResolutionSettings.setContentTypeSource(ContentTypeSource.fromType((String) contextPropertiesTemplateProps.remove(CONTENT_TYPE_SOURCE)));
        serviceResolutionSettings.setContentType((String) contextPropertiesTemplateProps.remove(CONTENT_TYPE_VALUE)); 
        
        final InboundJmsDestinationDetail inboundDetail = new InboundJmsDestinationDetail();
        inboundDetail.setAcknowledgeType(AcknowledgeType.fromType((String) jmsDestinationDetailProps.remove(INBOUND_ACKNOWLEDGEMENT_TYPE)));
        inboundDetail.setReplyType(ReplyType.fromType((String) jmsDestinationDetailProps.remove(REPLY_TYPE)));
        inboundDetail.setReplyToQueueName((String) jmsDestinationDetailProps.remove(REPLY_QUEUE_NAME));
        inboundDetail.setUseRequestCorrelationId(toBoolean((String) jmsDestinationDetailProps.remove(USE_REQUEST_CORRELATION_ID)));
        inboundDetail.setServiceResolutionSettings(serviceResolutionSettings);
        inboundDetail.setFailureQueueName((String) jmsDestinationDetailProps.remove(INBOUND_FAILURE_QUEUE_NAME));
        inboundDetail.setIsEnabled(isEnabled);
        inboundDetail.setNumOfConsumerConnections((Integer) contextPropertiesTemplateProps.remove(DEDICATED_CONSUMER_CONNECTION_SIZE));
        inboundDetail.setMaxMessageSizeBytes((Integer) jmsDestinationDetailProps.remove(INBOUND_MAX_SIZE));
        return inboundDetail;
    }
    
    private OutboundJmsDestinationDetail loadOutboundDetail(
            boolean isTemplate,
            Map<String, Object> jmsDestinationDetailProps,
            Map<String, Object> contextPropertiesTemplateProps) {
        PoolingType poolingType;
        ConnectionPoolingSettings connectionPoolingSettings = null;
        SessionPoolingSettings sessionPoolingSettings = null;

        final boolean isConnectionPool = toBoolean((String) contextPropertiesTemplateProps.remove(CONNECTION_POOL_ENABLED));
        if (isConnectionPool) {
            poolingType = PoolingType.CONNECTION;
            connectionPoolingSettings = new ConnectionPoolingSettings(
                    (Integer) contextPropertiesTemplateProps.remove(CONNECTION_POOL_SIZE),
                    (Integer) contextPropertiesTemplateProps.remove(CONNECTION_POOL_MIN_IDLE),
                    (Integer) contextPropertiesTemplateProps.remove(CONNECTION_POOL_MAX_WAIT));
        } else {
            poolingType = PoolingType.SESSION;
            sessionPoolingSettings = new SessionPoolingSettings(
                    (Integer) contextPropertiesTemplateProps.remove(SESSION_POOL_SIZE),
                    (Integer) contextPropertiesTemplateProps.remove(SESSION_POOL_MAX_IDLE),
                    (Integer) contextPropertiesTemplateProps.remove(SESSION_POOL_MAX_WAIT));
        }
        
        final OutboundJmsDestinationDetail outboundDetail = new OutboundJmsDestinationDetail();
        outboundDetail.setIsTemplate(isTemplate);
        outboundDetail.setReplyType(ReplyType.fromType((String) jmsDestinationDetailProps.remove(REPLY_TYPE)));
        outboundDetail.setReplyToQueueName((String) jmsDestinationDetailProps.remove(REPLY_QUEUE_NAME));
        outboundDetail.setUseRequestCorrelationId(toBoolean((String) jmsDestinationDetailProps.remove(USE_REQUEST_CORRELATION_ID)));
        outboundDetail.setMessageFormat(MessageFormat.fromFormat((String) jmsDestinationDetailProps.remove(OUTBOUND_MESSAGE_TYPE)));
        outboundDetail.setPoolingType(poolingType);
        outboundDetail.setSessionPoolingSettings(sessionPoolingSettings);
        outboundDetail.setConnectionPoolingSettings(connectionPoolingSettings);
        return outboundDetail;
    }
    
    @Override
    public String getEntityType() {
        return EntityTypes.JMS_DESTINATION_TYPE;
    }
}
