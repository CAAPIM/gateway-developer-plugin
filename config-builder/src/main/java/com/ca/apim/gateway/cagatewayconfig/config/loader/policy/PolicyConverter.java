/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import org.w3c.dom.Element;

import java.io.InputStream;

/**
 * Policy converters enable representing policy in multiple formats.
 */
public interface PolicyConverter {
    /**
     * The file extension that this policy converter uses. For example `.xml` or `.assertion.js`
     *
     * @return The file extension that this policy converter uses.
     */
    String getPolicyTypeExtension();

    /**
     * Removes the policy type extension from a file name or path if it contains the extension.
     *
     * @param name The name or path to remove the extension from
     * @return The name or path without the policy extension
     */
    default String removeExtension(String name) {
        return name.endsWith(getPolicyTypeExtension()) ? name.substring(0, name.length() - getPolicyTypeExtension().length()) : name;
    }

    /**
     * Returns the policy xml given the policyString in the representation that this converter can convert
     *
     * @param policy       The policy to create the policy xml for
     * @param policyString The policy string to convert to xml
     * @return The policy as valid policy xml
     */
    String getPolicyXML(Policy policy, String policyString);

    /**
     * Tests if this converter is able to convert from the given policy element
     *
     * @param name   The name of the policy
     * @param policy The policy element
     * @return true if this converter is able to convert the given policy, false otherwise
     */
    boolean canConvert(String name, Element policy);

    /**
     * Converts the given policy and returns an input stream to stream the conversion result.
     *
     * @param policy The policy to convert
     * @return The resulting conversion in an inputstream
     */
    InputStream convertFromPolicyElement(Element policy);
}
