/*
 * Copyright (c) 2018 CA. All rights reserved.
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.entity;

import com.ca.apim.gateway.cagatewayexport.tasks.explode.bundle.Entity;
import org.w3c.dom.Element;

public class EncassEntity implements Entity {
    private final String name;
    private final String id;
    private final String guid;
    private final String policyId;
    private final Element xml;

    public EncassEntity(final String name, final String id, final String guid, Element xml, String policyId) {
        this.name = name;
        this.id = id;
        this.guid = guid;
        this.xml = xml;
        this.policyId = policyId;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    @Override
    public Element getXml() {
        return xml;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getPolicyId() {
        return policyId;
    }

}
