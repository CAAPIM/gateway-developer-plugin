/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;

import java.util.Base64;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_ID;
import static com.ca.apim.gateway.cagatewayconfig.beans.IdentityProvider.INTERNAL_IDP_NAME;
import static com.ca.apim.gateway.cagatewayconfig.util.policy.PolicyXMLElements.*;

public class BundleDetemplatizer {

    private final Bundle bundle;

    public BundleDetemplatizer(Bundle bundle) {
        this.bundle = bundle;
    }

    public CharSequence detemplatizeBundleString(CharSequence bundleString) {
        //prefer to use string replacement instead of loading and parsing the bundle. This should perform faster and we are only replacing a limited amount of the bundle so it should be OK to do so.
        Map<String, String> environmentVariables = bundle.getEnvironmentProperties();

        //Replaces variables in set context variable assertions
        bundleString = replaceVariableInBundle(bundleString, environmentVariables,
                "L7p:Base64Expression ENV_PARAM_NAME=\\\"ENV\\.(.+?)\\\"",
                v -> "L7p:Base64Expression stringValue=\"" + Base64.getEncoder().encodeToString(v.getBytes()) + "\"");

        //Replaces Id prov name with goid in authentication assertions
        bundleString = replaceVariableInBundle(bundleString, createIdProviderNameGoid(bundle),
                ID_PROV_NAME + " " + STRING_VALUE + "=\\\"(.+?)\\\"",
                v -> ID_PROV_OID + " " + GOID_VALUE + "=\"" + v + "\"");

        //Replaces service property variables
        bundleString = replaceVariableInBundle(bundleString, environmentVariables,
                "l7:StringValue>SERVICE_PROPERTY_ENV\\.(.+?)<",
                v -> "l7:StringValue>" + v + "<").toString();
        return bundleString;
    }

    private StringBuffer replaceVariableInBundle(CharSequence bundle, Map<String, String> mapToCheck, String variableFinderRegex, Function<String, String> replacementFunction) {
        Pattern setVariablePattern;
        Matcher setVariableMatcher;
        setVariablePattern = Pattern.compile(variableFinderRegex);
        setVariableMatcher = setVariablePattern.matcher(bundle);

        StringBuffer replacedBundle = new StringBuffer();
        while (setVariableMatcher.find()) {
            String varName = setVariableMatcher.group(1);
            String value = mapToCheck.get(varName);
            if (value == null) {
                throw new BundleDetemplatizeException("Missing environment value for property: " + varName);
            }
            setVariableMatcher.appendReplacement(replacedBundle, replacementFunction.apply(value));
        }
        setVariableMatcher.appendTail(replacedBundle);
        return replacedBundle;
    }

    private Map<String, String> createIdProviderNameGoid(Bundle bundle) {
        Map<String, String> idProviders = bundle.getIdentityProviders().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().getId()));
        idProviders.put(INTERNAL_IDP_NAME, INTERNAL_IDP_ID);
        return idProviders;
    }
}
