/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;
import com.ca.apim.gateway.cagatewayconfig.config.spec.EnvironmentType;
import com.ca.apim.gateway.cagatewayconfig.util.IdGenerator;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import javax.inject.Named;
import java.io.File;

import static com.ca.apim.gateway.cagatewayconfig.beans.PolicyType.INTERNAL;
import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;

@Named("AUDIT_POLICY")
@ConfigurationFile(name = "audit-policies", type = JSON_YAML)
@EnvironmentType("AUDIT_POLICY")
public class AuditPolicy extends Policy {

    @Override
    public void postLoad(String entityKey, Bundle bundle, @Nullable File rootFolder, IdGenerator idGenerator) {
        super.postLoad(entityKey, bundle, rootFolder, idGenerator);

        setPolicyType(INTERNAL);
        bundle.putAllPolicies(ImmutableMap.of(this.getPath(), this));
        checkRepeatedTags(bundle, INTERNAL);
    }
}
