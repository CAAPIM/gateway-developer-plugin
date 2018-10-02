package com.ca.apim.gateway.cagatewayconfig;

import java.util.Base64;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BundleDetemplatizer {

    private final Map<String, String> environmentVariables;

    public BundleDetemplatizer(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public CharSequence detemplatizeBundleString(CharSequence bundleString) {
        //prefer to use string replacement instead of loading and parsing the bundle. This should perform faster and we are only replacing a limited amount of the bundle so it should be OK to do so.

        //Replaces variables in set context variable assertions
        bundleString = replaceVariableInBundle(bundleString,
                "L7p:Base64Expression ENV_PARAM_NAME=\\\"ENV\\.(.+?)\\\"",
                v -> "L7p:Base64Expression stringValue=\"" + Base64.getEncoder().encodeToString(v.getBytes()) + "\"");

        //Replaces servie property variables
        bundleString = replaceVariableInBundle(bundleString,
                "l7:StringValue>SERVICE_PROPERTY_ENV\\.(.+?)<",
                v -> "l7:StringValue>" + v + "<").toString();
        return bundleString;
    }

    private StringBuffer replaceVariableInBundle(CharSequence bundle, String variableFinderRegex, Function<String, String> replacementFunction) {
        Pattern setVariablePattern;
        Matcher setVariableMatcher;
        setVariablePattern = Pattern.compile(variableFinderRegex);
        setVariableMatcher = setVariablePattern.matcher(bundle);

        StringBuffer replacedBundle = new StringBuffer();
        while (setVariableMatcher.find()) {
            String varName = setVariableMatcher.group(1);
            String value = environmentVariables.get(varName);
            if (value == null) {
                throw new BundleDetemplatizeException("Missing environment value for property: " + varName);
            }
            setVariableMatcher.appendReplacement(replacedBundle, replacementFunction.apply(value));
        }
        setVariableMatcher.appendTail(replacedBundle);
        return replacedBundle;
    }
}
