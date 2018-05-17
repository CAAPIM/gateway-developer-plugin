/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import org.w3c.dom.Element;

import java.util.Map;

public class PolicyBackedServiceEntity implements Entity {
    private final String name;
    private final String id;
    private final Map<String, String> operations;
    private final Element xml;
    private final String interfaceName;

    public PolicyBackedServiceEntity(final String name, final String id, String interfaceName, Element xml, final Map<String, String> operations) {
        this.name = name;
        this.id = id;
        this.interfaceName = interfaceName;
        this.xml = xml;
        this.operations = operations;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public Map<String, String> getOperations() {
        return operations;
    }

}
