/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.properties;

/**
 * Constants related to properties.
 */
@SuppressWarnings("squid:S2068") // sonarcloud believes this is a hardcoded password
public class PropertyConstants {

    public static final String EXTENSION_CONFIG_FILE = "gateway-developer-plugin-extension.properties";

    public static final String PREFIX_GATEWAY = "gateway.";
    public static final String PREFIX_ENV = "ENV.";
    public static final String PREFIX_PROPERTY = "property.";

    // Listen Port
    public static final String USE_EXTENDED_FTP_COMMAND_SET = "useExtendedFtpCommandSet";

    //Trusted Cert property constants
    public static final String VERIFY_HOSTNAME = "verifyHostname";
    public static final String TRUSTED_FOR_SSL = "trustedForSsl";
    public static final String TRUSTED_AS_SAML_ATTESTING_ENTITY = "trustedAsSamlAttestingEntity";
    public static final String TRUST_ANCHOR = "trustAnchor";
    public static final String REVOCATION_CHECKING_ENABLED = "revocationCheckingEnabled";
    public static final String TRUSTING_SIGNING_CLIENT_CERTS = "trustedForSigningClientCerts";
    public static final String TRUSTED_SIGNING_SERVER_CERTS = "trustedForSigningServerCerts";
    public static final String TRUSTED_AS_SAML_ISSUER = "trustedAsSamlIssuer";

    //Encass property constants
    public static final String POLICY_GUID_PROP = "policyGuid";
    public static final String PALETTE_FOLDER = "paletteFolder";
    public static final String PALETTE_ICON_RESOURCE_NAME = "paletteIconResourceName";
    public static final String ALLOW_TRACING = "allowTracing";
    public static final String PASS_METRICS_TO_PARENT = "passMetricsToParent";
    public static final String DESCRIPTION = "description";
    public static final String DEFAULT_PALETTE_FOLDER_LOCATION = "internalAssertions";

    // Property names
    public static final String PROPERTY_USER = "user";
    public static final String PROPERTY_USERNAME = "username";
    public static final String PROPERTY_PASSWORD = "password";
    public static final String PROPERTY_MIN_POOL_SIZE = "minimumPoolSize";
    public static final String PROPERTY_MAX_POOL_SIZE = "maximumPoolSize";
    public static final String PROPERTY_TAG = "tag";
    public static final String PROPERTY_SUBTAG = "subtag";

    public static final String KEY_VALUE_SOAP = "soap";
    public static final String KEY_VALUE_SOAP_VERSION = "soapVersion";
    public static final String KEY_VALUE_WSS_PROCESSING_ENABLED = "wssProcessingEnabled";
    public static final String TAG_VALUE_POLICY = "policy";
    public static final String TAG_VALUE_WSDL = "wsdl";

    // JMS Destination property constants
    public static final String JNDI_INITIAL_CONTEXT_FACTORY_CLASSNAME = "jndi.initialContextFactoryClassname";
    public static final String JNDI_PROVIDER_URL = "jndi.providerUrl";
    public static final String JNDI_USERNAME = "java.naming.security.principal";
    public static final String JNDI_PASSWORD = "java.naming.security.credentials";

    public static final String DESTINATION_TYPE = "type";
    public static final String CONNECTION_FACTORY_NAME = "queue.connectionFactoryName";
    
    public static final String REPLY_TYPE = "replyType";
    public static final String REPLY_QUEUE_NAME = "replyToQueueName";
    public static final String USE_REQUEST_CORRELATION_ID = "useRequestCorrelationId";

    public static final String JMS_PROPS_PREFIX = "com.l7tech.server.jms.prop.";
    public static final String TIBCO_JMS_PREFIX = "com.tibco.tibjms.";
    
    // JMS Destination - inbound specific
    public static final String INBOUND_ACKNOWLEDGEMENT_TYPE = "inbound.acknowledgementType";
    public static final String IS_HARDWIRED_SERVICE = JMS_PROPS_PREFIX + "hardwired.service.bool";
    public static final String HARDWIRED_SERVICE_ID = JMS_PROPS_PREFIX + "hardwired.service.id";
    public static final String SOAP_ACTION_MSG_PROP_NAME = "com.l7tech.server.jms.soapAction.msgPropName";
    public static final String CONTENT_TYPE_SOURCE = JMS_PROPS_PREFIX + "contentType.source";
    public static final String CONTENT_TYPE_VALUE = JMS_PROPS_PREFIX + "contentType.value";
    public static final String INBOUND_FAILURE_QUEUE_NAME = "inbound.failureQueueName";
    public static final String IS_DEDICATED_CONSUMER_CONNECTION = JMS_PROPS_PREFIX + "dedicated.consumer.bool";
    public static final String DEDICATED_CONSUMER_CONNECTION_SIZE = JMS_PROPS_PREFIX + "dedicated.consumer.size";
    public static final String INBOUND_MAX_SIZE = "inbound.maximumSize";

    public static final String JNDI_CLIENT_AUT_KEYSTORE_ID = JMS_PROPS_PREFIX + "jndi.ssgKeystoreId";
    public static final String JNDI_CLIENT_AUT_KEYSTORE_ALIAS = JMS_PROPS_PREFIX + "jndi.ssgKeyAlias";
    public static final String JNDI_CLIENT_AUT_AUTH_IDENTITY = TIBCO_JMS_PREFIX + "naming.ssl_identity";
    public static final String JNDI_CLIENT_AUT_AUTH_PASSWORD = TIBCO_JMS_PREFIX + "naming.ssl_password";

    public static final String DESTINATION_CLIENT_AUTH_KEYSTORE_ID = JMS_PROPS_PREFIX + "queue.ssgKeystoreId";
    public static final String DESTINATION_CLIENT_AUTH_KEYSTORE_ALIAS = JMS_PROPS_PREFIX + "queue.ssgKeyAlias";
    public static final String DESTINATION_CLIENT_AUTH_IDENTITY = TIBCO_JMS_PREFIX + "ssl.identity";
    public static final String DESTINATION_CLIENT_AUTH_PASSWORD = TIBCO_JMS_PREFIX + "ssl.password";
    
    // JMS Destination - outbound specific
    public static final String OUTBOUND_MESSAGE_TYPE = "outbound.MessageType";
    public static final String CONNECTION_POOL_ENABLED = JMS_PROPS_PREFIX + "connection.pool.enable";
    public static final String CONNECTION_POOL_SIZE = JMS_PROPS_PREFIX + "connection.pool.size";
    public static final String CONNECTION_POOL_MIN_IDLE = JMS_PROPS_PREFIX + "connection.min.idle";
    public static final String CONNECTION_POOL_MAX_WAIT = JMS_PROPS_PREFIX + "connection.pool.max.wait";
    public static final String SESSION_POOL_SIZE = JMS_PROPS_PREFIX + "session.pool.size";
    public static final String SESSION_POOL_MAX_IDLE = JMS_PROPS_PREFIX + "max.session.idle";
    public static final String SESSION_POOL_MAX_WAIT = JMS_PROPS_PREFIX + "session.pool.max.wait";
    
    private PropertyConstants() { }
}
