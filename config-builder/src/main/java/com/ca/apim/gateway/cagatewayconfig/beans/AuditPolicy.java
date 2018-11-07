/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;

import javax.inject.Named;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;

@Named("AUDIT_POLICY")
@ConfigurationFile(name = "audit-policies", type = JSON_YAML)
public class AuditPolicy extends Policy {
    // Empty, just need to exist to avoid a specific writer
}
