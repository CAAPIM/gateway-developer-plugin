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
    public static final String API_PORTAL_ENCASS_INTEGRATION = "L7p:ApiPortalEncassIntegration";
    public static final String POLICY_GUID = "L7p:PolicyGuid";
    public static final String NO_OP_IF_CONFIG_MISSING = "L7p:NoOpIfConfigMissing";
    public static final String AUTHENTICATION = "L7p:Authentication";
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
    public static final String ITEM = "L7p:item";

    private PolicyXMLElements() {

    }
}
