/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.util.policy;

import com.ca.apim.gateway.cagatewayconfig.beans.Bundle;
import org.w3c.dom.Element;

/**
 * Context providing the data for the policy simplification process.
 */
public class PolicySimplifierContext {

    private String policyName;
    private Bundle bundle;
    private Bundle resultantBundle;
    private Element assertionElement;

    PolicySimplifierContext(String policyName, Bundle bundle, Bundle resultantBundle) {
        this.policyName = policyName;
        this.bundle = bundle;
        this.resultantBundle = resultantBundle;
    }

    public String getPolicyName() {
        return policyName;
    }

    public Element getAssertionElement() {
        return assertionElement;
    }

    PolicySimplifierContext withAssertionElement(Element assertionElement) {
        this.assertionElement = assertionElement;
        return this;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public Bundle getResultantBundle() {
        return resultantBundle;
    }
}
