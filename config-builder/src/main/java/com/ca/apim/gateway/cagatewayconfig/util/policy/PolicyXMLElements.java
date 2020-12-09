/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.policy;

/**
 * Utility class to hold element names for Policy XMLs (not bundles)
 */
public class PolicyXMLElements {

    public static final String GOID_VALUE = "goidValue";
    public static final String STRING_VALUE = "stringValue";
    public static final String BOOLEAN_VALUE = "booleanValue";
    public static final String GOID_ARRAY_VALUE = "goidArrayValue";
    public static final String INCLUDE = "L7p:Include";
    public static final String ENCAPSULATED = "L7p:Encapsulated";
    public static final String SET_VARIABLE = "L7p:SetVariable";
    public static final String HARDCODED_RESPONSE = "L7p:HardcodedResponse";
    public static final String BASE_64_RESPONSE_BODY = "L7p:Base64ResponseBody";
    public static final String RESPONSE_BODY = "L7p:ResponseBody";
    public static final String BASE_64_EXPRESSION = "L7p:Base64Expression";
    public static final String VARIABLE_TO_SET = "L7p:VariableToSet";
    public static final String EXPRESSION = "L7p:Expression";
    public static final String ENCAPSULATED_ASSERTION_CONFIG_GUID = "L7p:EncapsulatedAssertionConfigGuid";
    public static final String ENCAPSULATED_ASSERTION_CONFIG_NAME = "L7p:EncapsulatedAssertionConfigName";
    public static final String COMMENT_ASSERTION = "L7p:CommentAssertion";
    public static final String COMMENT = "L7p:Comment";
    public static final String API_PORTAL_ENCASS_INTEGRATION = "L7p:ApiPortalEncassIntegration";
    public static final String API_PORTAL_INTEGRATION = "L7p:ApiPortalIntegration";
    public static final String API_PORTAL_INTEGRATION_FLAG = "L7p:PortalManagedApiFlag";
    public static final String API_PORTAL_INTEGRATION_FLAG_SERVICE = "L7p:ApiPortalManagedServiceAssertion";
    public static final String API_PORTAL_INTEGRATION_VARIABLE_PREFIX = "L7p:VariablePrefix";
    public static final String API_PORTAL_INTEGRATION_API_ID = "L7p:ApiId";
    public static final String API_PORTAL_INTEGRATION_API_GROUP = "L7p:ApiGroup";

    public static final String POLICY_GUID = "L7p:PolicyGuid";
    public static final String NO_OP_IF_CONFIG_MISSING = "L7p:NoOpIfConfigMissing";
    public static final String AUTHENTICATION = "L7p:Authentication";
    public static final String SPECIFIC_USER = "L7p:SpecificUser";
    public static final String ID_PROV_OID = "L7p:IdentityProviderOid";
    public static final String ID_PROV_NAME = "L7p:IdentityProviderName";
    public static final String TARGET = "L7p:Target";
    public static final String JMS_ROUTING_ASSERTION = "L7p:JmsRoutingAssertion";
    public static final String JMS_ENDPOINT_OID = "L7p:EndpointOid";
    public static final String JMS_ENDPOINT_NAME = "L7p:EndpointName";
    public static final String HTTP_ROUTING_ASSERTION  = "L7p:HttpRoutingAssertion";
    public static final String TLS_TRUSTED_CERT_IDS = "L7p:TlsTrustedCertGoids";
    public static final String TLS_TRUSTED_CERT_NAMES = "L7p:TlsTrustedCertNames";
    public static final String MQ_ROUTING_ASSERTION = "L7p:MqNativeRouting";
    public static final String ACTIVE_CONNECTOR_ID = "L7p:SsgActiveConnectorId";
    public static final String ACTIVE_CONNECTOR_GOID = "L7p:SsgActiveConnectorGoid";
    public static final String ACTIVE_CONNECTOR_NAME = "L7p:SsgActiveConnectorName";

    public static final String HTTP2_ROUTING_ASSERTION  = "L7p:Http2Routing";
    public static final String HTTP2_CLIENT_CONFIG_GOID = "L7p:Http2ClientConfigGoid";
    public static final String HTTP2_CLIENT_CONFIG_NAME = "L7p:Http2ClientConfigName";

    public static final String CASSANDRA_QUERY_ASSERTION  = "L7p:CassandraQuery";
    public static final String CASSANDRA_CONNECTION_NAME  = "L7p:ConnectionName";

    public static final String JDBC_QUERY_ASSERTION  = "L7p:JdbcQuery";
    public static final String JDBC_CONNECTION_NAME  = "L7p:ConnectionName";



    public static final String ITEM = "L7p:item";
    public static final String ENABLED = "L7p:Enabled";

    private PolicyXMLElements() {

    }
}
