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
    public static final String POLICY_GUID = "L7p:PolicyGuid";
    public static final String NO_OP_IF_CONFIG_MISSING = "L7p:NoOpIfConfigMissing";
    public static final String AUTHENTICATION = "L7p:Authentication";
    public static final String ID_PROV_OID = "L7p:IdentityProviderOid";
    public static final String ID_PROV_NAME = "L7p:IdentityProviderName";
    public static final String TARGET = "L7p:Target";

    private PolicyXMLElements() {

    }
}
