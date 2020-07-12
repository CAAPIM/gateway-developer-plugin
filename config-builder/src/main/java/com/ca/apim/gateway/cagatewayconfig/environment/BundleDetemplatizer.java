/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.environment;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import com.ca.apim.gateway.cagatewayconfig.util.environment.EnvironmentConfigurationUtils;

import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_ID;
import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;
import static java.util.stream.Collectors.toMap;

class BundleDetemplatizer {

    private final Bundle bundle;

    BundleDetemplatizer(Bundle bundle) {
        this.bundle = bundle;
    }

    CharSequence detemplatizeBundleString(CharSequence bundleString) {
        //prefer to use string replacement instead of loading and parsing the bundle. This should perform faster and we are only replacing a limited amount of the bundle so it should be OK to do so.
        Map<String, String> contextVariableEnvironmentVariables = bundle.getContextVariableEnvironmentProperties().entrySet().stream().collect(toMap(Entry::getKey, e -> e.getValue().getValue()));
        Map<String, String> serviceEnvironmentVariables = bundle.getServiceEnvironmentProperties().entrySet().stream().collect(toMap(Entry::getKey, e -> e.getValue().getValue()));

        //Replaces variables in set context variable assertions
        bundleString = replaceVariableInBundle(bundleString, contextVariableEnvironmentVariables,
                "L7p:Base64Expression ENV_PARAM_NAME=\\\"ENV\\.(.+?)\\\"",
                v -> "L7p:Base64Expression stringValue=\"" + Base64.getEncoder().encodeToString(v.getBytes()) + "\"");

        //Replaces service property variables
        bundleString = replaceVariableInBundle(bundleString, serviceEnvironmentVariables,
                "l7:StringValue>SERVICE_PROPERTY_ENV\\.(.+?)<",
                v -> "l7:StringValue>" + v + "<").toString();
        return bundleString;
    }

    private StringBuffer replaceVariableInBundle(CharSequence bundle, Map<String, String> mapToCheck, String variableFinderRegex, UnaryOperator<String> replacementFunction) {
        return this.replaceVariableInBundle(bundle, mapToCheck, variableFinderRegex, (varName, value) -> replacementFunction.apply(value));
    }

    private StringBuffer replaceVariableInBundle(CharSequence bundle, Map<String, String> mapToCheck, String variableFinderRegex, BinaryOperator<String> replacementFunction) {
        Pattern setVariablePattern;
        Matcher setVariableMatcher;
        setVariablePattern = Pattern.compile(variableFinderRegex);
        setVariableMatcher = setVariablePattern.matcher(bundle);

        StringBuffer replacedBundle = new StringBuffer();
        while (setVariableMatcher.find()) {
            String varName = setVariableMatcher.group(1);
            String value = mapToCheck.get(EnvironmentConfigurationUtils.extractEntityName(varName));
            if (value == null) {
                throw new BundleDetemplatizeException("Missing environment value for property: " + varName);
            }
            setVariableMatcher.appendReplacement(replacedBundle, replacementFunction.apply(varName, value));
        }
        setVariableMatcher.appendTail(replacedBundle);
        return replacedBundle;
    }

}
