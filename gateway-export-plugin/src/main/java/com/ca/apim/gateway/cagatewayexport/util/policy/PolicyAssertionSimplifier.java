/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.util.xml.DocumentParseException;

/**
 * Implementations of this class can modify policy XML structure to make it more readable and user-friendly.
 */
public interface PolicyAssertionSimplifier {

    /**
     * Simplify the policy XML assertion element.
     *
     * @param context context containing required data for simplification process
     * @throws DocumentParseException if there is any issue to read policy contents
     */
    void simplifyAssertionElement(PolicySimplifierContext context) throws DocumentParseException;

    /**
     * @return the XML tag name for the assertion handled by this simplifier
     */
    String getAssertionTagName();
}
