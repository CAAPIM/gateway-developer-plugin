/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.config.loader.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Policy;
import org.w3c.dom.Element;

import java.io.InputStream;

public interface PolicyConverter {
    String getPolicyTypeExtension();

    default String removeExtension(String name) {
        return name.substring(0, name.length() - getPolicyTypeExtension().length());
    }

    String getPolicyXML(Policy policy, String policyString);

    boolean canConvertible(String name, Element policy);

    InputStream convertFromPolicy(Element policy);
}
