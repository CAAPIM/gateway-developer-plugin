/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.writer.beans;

import java.util.List;

public class PolicyBackedService {
    private String interfaceName;
    private List<PolicyBackedServiceOperation> operations;

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public List<PolicyBackedServiceOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<PolicyBackedServiceOperation> operations) {
        this.operations = operations;
    }
}