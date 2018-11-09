/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayconfig.beans;

import com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile;

import javax.inject.Named;
import java.util.Set;

import static com.ca.apim.gateway.cagatewayconfig.config.spec.ConfigurationFile.FileType.JSON_YAML;

@Named("POLICY_BACKED_SERVICE")
@ConfigurationFile(name = "policy-backed-services", type = JSON_YAML)
public class PolicyBackedService extends GatewayEntity {
    private String interfaceName;
    private Set<PolicyBackedServiceOperation> operations;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Set<PolicyBackedServiceOperation> getOperations() {
        return operations;
    }

    public void setOperations(Set<PolicyBackedServiceOperation> operations) {
        this.operations = operations;
    }
}