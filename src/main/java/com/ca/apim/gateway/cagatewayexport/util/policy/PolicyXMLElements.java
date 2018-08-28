/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

/**
 * Utility class to hold element names for Policy XMLs (not bundles)
 */
class PolicyXMLElements {

    static final String STRING_VALUE = "stringValue";
    static final String INCLUDE = "L7p:Include";
    static final String ENCAPSULATED = "L7p:Encapsulated";
    static final String SET_VARIABLE = "L7p:SetVariable";
    static final String HARDCODED_RESPONSE = "L7p:HardcodedResponse";
    static final String BASE_64_RESPONSE_BODY = "L7p:Base64ResponseBody";
    static final String RESPONSE_BODY = "L7p:ResponseBody";
    static final String BASE_64_EXPRESSION = "L7p:Base64Expression";
    static final String VARIABLE_TO_SET = "L7p:VariableToSet";
    static final String EXPRESSION = "L7p:Expression";
    static final String ENCAPSULATED_ASSERTION_CONFIG_GUID = "L7p:EncapsulatedAssertionConfigGuid";
    static final String ENCAPSULATED_ASSERTION_CONFIG_NAME = "L7p:EncapsulatedAssertionConfigName";
    static final String POLICY_GUID = "L7p:PolicyGuid";

    private PolicyXMLElements() {

    }
}
