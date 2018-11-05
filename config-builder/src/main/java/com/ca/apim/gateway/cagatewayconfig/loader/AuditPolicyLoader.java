/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.loader;

import com.ca.apim.gateway.cagatewayconfig.beans.PolicyType;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuditPolicyLoader extends PolicyLoaderBase {

    private static final String FILE_NAME = "audit-policies";

    @Inject
    AuditPolicyLoader(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    PolicyType getPolicyType() {
        return PolicyType.INTERNAL;
    }

    @Override
    public String getEntityType() {
        return "AUDIT_POLICY";
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }
}
