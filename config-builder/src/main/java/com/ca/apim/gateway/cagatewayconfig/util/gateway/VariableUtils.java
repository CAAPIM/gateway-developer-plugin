/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.util.gateway;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Utility methods to handle values with variable reference expressions (${something}).
 */
public class VariableUtils {

    private static final String REGEX_PREFIX = "(?:\\$\\{)";
    private static final String REGEX_SUFFIX = "(?:})";
    private static final Pattern regexPattern = Pattern.compile(REGEX_PREFIX + "([^\\$\\{\\}]+?)" + REGEX_SUFFIX);

    /**
     * Extract the variable referenced from an expression like ${variableName}.
     *
     * @param expression expression to evaluate
     * @return the variable from the expression or null if the expression is null or malformed
     */
    public static String extractVariableName(String expression) {
        if (isEmpty(expression)) {
            return null;
        }

        Matcher matcher = regexPattern.matcher(expression);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private VariableUtils() {}
}
