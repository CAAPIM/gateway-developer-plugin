/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.tasks.zip.loader;

import com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType;
import com.ca.apim.gateway.cagatewayconfig.util.json.JsonTools;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.ca.apim.gateway.cagatewayconfig.tasks.zip.beans.PolicyType.GLOBAL;

@Singleton
public class GlobalPolicyLoader extends PolicyLoaderBase {

    private static final String FILE_NAME = "global-policies";

    @Inject
    GlobalPolicyLoader(JsonTools jsonTools) {
        super(jsonTools);
    }

    @Override
    PolicyType getPolicyType() {
        return GLOBAL;
    }

    @Override
    public String getEntityType() {
        return "GLOBAL_POLICY";
    }

    @Override
    protected String getFileName() {
        return FILE_NAME;
    }
}
